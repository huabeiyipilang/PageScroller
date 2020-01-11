package xyz.penghaonan.pagescroller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xyz.penghaonan.pagescroller.container.PSContainer;
import xyz.penghaonan.pagescroller.scroller.Scroller;
import xyz.penghaonan.pagescroller.utils.PSLogger;

public class PageScroller {

    public static final int STATE_IDLE = 0;
    public static final int STATE_TOUCH_DOWN = 1;
    public static final int STATE_TOUCH_SCROLL = 2;
    public static final int STATE_TOUCH_UP = 3;
    public static final int STATE_FLING = 4;

    private List<View> forbiddenViews = new LinkedList<>();

    private final List<Scroller> scrollers = new LinkedList<>();

    // 在拦截事件之后，需要发送cancel事件，中断其他view的事件。
    // 理论上应该在container view和activity中直接掉super.dispatchTouchEvent
    // 此处为了减少使用复杂度，在内部过滤。
    private MotionEvent cancelEvent;
    // 状态
    private int state = STATE_IDLE;
    // 纵向slop参数
    private float slopXScale = 1f;
    private float slopYScale = 1f;

    private Handler handler;
    private boolean enable = true;
    private int touchSlop;
    private boolean dragMode;
    private float startX, startY;
    private float lastY;
    private int maxV, minV;
    private boolean lastDispatchTouchEventState;
    private boolean disableUntilNextDown;
    private Context context;
    private Comparator<Scroller> childScrollerComparator = new Comparator<Scroller>() {
        @Override
        public int compare(Scroller o1, Scroller o2) {
            if (o1.priority() == o2.priority()) {
                return 1;
            }
            return o1.priority() - o2.priority();
        }
    };
    private FlingHelper flingHelper;
    private PSContainer container;
    private Scroller currentScroller;

    public PageScroller(View containerView) {
        container = PSContainer.Factory.create(containerView);
        init();
    }

    public PageScroller(Activity activity) {
        container = PSContainer.Factory.create(activity);
        init();
    }

    private void init() {
        context = container.getContext();
        handler = new Handler(Looper.getMainLooper());
        flingHelper = new FlingHelper();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        maxV = configuration.getScaledMaximumFlingVelocity();
        minV = configuration.getScaledMinimumFlingVelocity();
    }

    /**
     * 横向位移灵敏度, 灵敏度于数值成反比。
     *
     * @param slopXScale > 0， 正常值为1
     */
    public void setSlopXScale(float slopXScale) {
        this.slopXScale = slopXScale;
    }

    /**
     * 纵向位移灵敏度, 灵敏度于数值成反比。
     *
     * @param slopYScale > 0， 正常值为1
     */
    public void setSlopYScale(float slopYScale) {
        this.slopYScale = slopYScale;
    }

    public void addScroller(Scroller scroller) {
        if (scroller == null) {
            return;
        }
        synchronized (scrollers) {
            Iterator<Scroller> iterator = scrollers.iterator();
            while (iterator.hasNext()) {
                Scroller childScroller = iterator.next();
                if (childScroller.priority() == scroller.priority()) {
                    iterator.remove();
                }
            }
            scrollers.add(scroller);
            Collections.sort(scrollers, childScrollerComparator);
        }
    }

    public void removeScroller(Scroller scroller) {
        if (scroller == null) {
            return;
        }
        synchronized (scrollers) {
            Iterator<Scroller> iterator = scrollers.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == scroller) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public List<Scroller> getScrollers() {
        return new LinkedList<>(scrollers);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void addForbidView(View view) {
        forbiddenViews.add(view);
    }

    public int getState() {
        return state;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event == cancelEvent) {
            cancelEvent = null;
            return false;
        }
        if (!enable) {
            lastDispatchTouchEventState = false;
            return false;
        }
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        boolean consume = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Rect rect = new Rect();
                setState(STATE_TOUCH_DOWN);
                for (View view : forbiddenViews) {
                    view.getGlobalVisibleRect(rect);
                    boolean contains = rect.contains((int) event.getRawX(), (int) currentY);
                    if (contains) {
                        disableUntilNextDown = true;
                        lastDispatchTouchEventState = false;
                        return false;
                    }
                }
                disableUntilNextDown = false;
                startX = currentX;
                startY = currentY;
                lastY = startY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!disableUntilNextDown) {
                    float moveX = currentX - startX;
                    float moveY = currentY - startY;
                    if (!dragMode && Math.abs(moveX) > touchSlop * slopXScale) {
                        // 横向位移先>slop
                        disableUntilNextDown = true;
                        return false;
                    }
                    if (!dragMode && Math.abs(moveY) > touchSlop * slopYScale) {
                        dragMode = true;
                    }
                    if (dragMode) {
                        setState(STATE_TOUCH_SCROLL);
                        float deltaY = currentY - lastY;
                        dispatchDeltaY(false, deltaY);
                        lastY = currentY;
                    }
                }
                consume = dragMode;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setState(STATE_TOUCH_UP);
                if (dragMode) {
                    dragMode = false;
                    consume = true;
                }
                break;
        }

        flingHelper.onDispatchTouchEvent(event);

        // 取消操作
        if (consume && !lastDispatchTouchEventState) {
            cancelEvent = MotionEvent.obtain(event);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
            container.dispatchTouchEvent(cancelEvent);
        }
        lastDispatchTouchEventState = consume;
        return consume;
    }

    private void setState(final int state) {
        if (this.state == state) {
            return;
        }
        PSLogger.i("state:" + state);
        this.state = state;
        synchronized (scrollers) {
            for (final Scroller childScroller : scrollers) {
                childScroller.changeScrollState(state);
            }
        }
    }

    private void setCurrentScroller(Scroller scroller) {
        if (currentScroller == scroller) {
            return;
        }
        scroller.changeControlledState(true);
        if (currentScroller != null) {
            currentScroller.changeControlledState(false);
        }
        currentScroller = scroller;
    }

    /**
     * 当前fling距离
     * @return
     */
    public int getFlingY() {
        if (state == STATE_FLING) {
            return flingHelper.scroller.getCurrY();
        } else {
            return 0;
        }
    }

    /**
     * fling终点值
     * @return
     */
    public int getFlingFinalY() {
        if (state == STATE_FLING) {
            return flingHelper.scroller.getFinalY();
        } else {
            return 0;
        }
    }

    public void abortFling() {
        flingHelper.scroller.abortAnimation();
    }

    /**
     * 惯性滚动
     */
    private class FlingHelper {

        private VelocityTracker velocityTracker;
        private OverScroller scroller;

        private Runnable flingRunnable;

        public FlingHelper() {
            scroller = new OverScroller(context);
        }

        void onDispatchTouchEvent(MotionEvent event) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    scroller.forceFinished(true);
                    scroller.abortAnimation();
                    handler.removeCallbacks(flingRunnable);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
//                    log("dispatchTouchEvent > maxV:" + maxV + ", minV:" + minV);
                    velocityTracker.computeCurrentVelocity(1000, maxV);
                    int initialVelocity = (int) velocityTracker.getYVelocity();
//                    log("dispatchTouchEvent > initialVelocity:" + initialVelocity);
                    if (Math.abs(initialVelocity) > minV) {
                        scroller.fling(0, 0, 0, initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                        flingRunnable = new FlingRunnable();
                        handler.post(flingRunnable);
                    } else {
                        setState(STATE_IDLE);
                    }
                    velocityTracker.recycle();
                    velocityTracker = null;
                    break;
            }
        }

        private class FlingRunnable implements Runnable {
            float lastFlingY = 0;

            @Override
            public void run() {
                boolean fling = scroller.computeScrollOffset();
                log("dispatchDeltaY > fling:" + fling);
                if (fling) {
                    setState(STATE_FLING);
                    int currentFlingY = scroller.getCurrY();
                    float deltaY = currentFlingY - lastFlingY;
                    log("dispatchDeltaY > currentFlingY:" + currentFlingY + ", finalY:" + scroller.getFinalY() + ", deltaY:" + deltaY);
                    boolean consume = dispatchDeltaY(true, deltaY);
                    if (consume) {
                        lastFlingY = currentFlingY;
                        handler.post(this);
                    } else {
                        setState(STATE_IDLE);
                    }
                } else {
                    setState(STATE_IDLE);
                }
            }
        }
    }

    private boolean dispatchDeltaY(boolean isFling, float deltaY) {
        synchronized (scrollers) {
            if (deltaY < 0) {
                // 上滑
                for (int i = 0; i < scrollers.size(); i++) {
                    Scroller scroller = scrollers.get(i);

                    // 是否被下一个阻断
//                    if (i > 0) {
                    if (breakDispatch(isFling, true, scroller)) {
                        return false;
                    }
//                    }
                    if (scroller.isEnable()) {
                        if (isFling && scroller.canConsumeY(deltaY) && !scroller.isEnableFling()) {
                            // 对于fling不可用的scroller，跳过位移分发
                            return false;
                        }
                        if (scroller.canConsumeY(deltaY)) {
                            setCurrentScroller(scroller);
                            deltaY = scroller.consumeY(deltaY);
                            if (deltaY == 0) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else {
                // 下滑
                for (int i = scrollers.size() - 1; i >= 0; i--) {
                    Scroller scroller = scrollers.get(i);

                    // 是否被下一个阻断
//                    if (i < scrollers.size() - 1) {
                    if (breakDispatch(isFling, false, scroller)) {
                        return false;
                    }
//                    }
                    if (scroller.isEnable()) {
                        if (isFling && scroller.canConsumeY(deltaY) && !scroller.isEnableFling()) {
                            // 对于fling不可用的scroller，跳过位移分发
                            return false;
                        }
                        if (scroller.canConsumeY(deltaY)) {
                            setCurrentScroller(scroller);
                            deltaY = scroller.consumeY(deltaY);
                            if (deltaY == 0) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
    }

    private boolean breakDispatch(boolean isFling, boolean up, Scroller scroller) {
        if (isFling) {
            return scroller.isBreak(up ? Scroller.BREAK_FLING_UP : Scroller.BREAK_FLING_DOWN);
        } else {
            return scroller.isBreak(up ? Scroller.BREAK_MOVE_UP : Scroller.BREAK_MOVE_DOWN);
        }
    }

    private static void log(String log) {
        PSLogger.i(log);
    }

    public boolean isDragMode() {
        return dragMode;
    }
}
