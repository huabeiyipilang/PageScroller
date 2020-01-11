package xyz.penghaonan.pagescrollersample.features.pulldownrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;

import xyz.penghaonan.pagescroller.PageScroller;
import xyz.penghaonan.pagescroller.container.ScrollFrameLayout;
import xyz.penghaonan.pagescroller.scroller.ListViewScroller;
import xyz.penghaonan.pagescroller.scroller.Scroller;
import xyz.penghaonan.pagescroller.scroller.ValueScroller;
import xyz.penghaonan.pagescroller.utils.PSLogger;
import xyz.penghaonan.pagescrollersample.R;
import xyz.penghaonan.pagescrollersample.simulation.NetUtils;

public class PullDownRefreshLayout extends ScrollFrameLayout {

    public static final int STATE_IDLE = 0;
    public static final int STATE_PULL_DOWN = 1;
    public static final int STATE_PULL_DOWN_ENOUGH = 2;
    public static final int STATE_REFRESH = 3;
    public static final int STATE_REFRESH_FINISH = 4;

    private TextView refreshTextView;
    private ListView listView;
    private View bannerView;

    private ValueScroller bannerScroller;

    private int state = STATE_IDLE;
    private boolean hasTouchedBeforeResponse;

    public PullDownRefreshLayout(Context context) {
        this(context, null);
    }

    public PullDownRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                assignViews();
                bannerView.setY(-bannerView.getMeasuredHeight());
                initScroller();
            }
        });
    }

    private void assignViews() {
        bannerView = findViewById(R.id.bannerView);
        refreshTextView = findViewById(R.id.refreshTextView);
        listView = findViewById(R.id.listView);
    }

    private void setState(int state) {
        PSLogger.i("setState > state:" + state);
        if (this.state == state) {
            return;
        }
        this.state = state;
        switch (state) {
            case STATE_IDLE:
                refreshTextView.setText("刷新成功");
                break;
            case STATE_PULL_DOWN:
                refreshTextView.setText("下拉刷新");
                break;
            case STATE_PULL_DOWN_ENOUGH:
                refreshTextView.setText("松手刷新");
                break;
            case STATE_REFRESH:
                refreshTextView.setText("正在刷新");
                break;
            case STATE_REFRESH_FINISH:
                refreshTextView.setText("刷新成功");
                break;
        }
    }

    /**
     * 初始化scroller
     */
    private void initScroller() {
        // 纵向位移灵敏度
        pageScroller.setSlopXScale(2f);

        bannerScroller = new ValueScroller(bannerView.getY(), 0) {
            @Override
            protected float getCurrentValue() {
                return bannerView.getY();
            }

            @Override
            protected void setCurrentValue(float value) {
                bannerView.setY(value);
                float listViewY = value - getMinValue();
                listView.setY(listViewY);
                if (pageScroller.getState() == PageScroller.STATE_TOUCH_SCROLL || pageScroller.getState() == PageScroller.STATE_FLING) {
                    if (listViewY < refreshTextView.getHeight()) {
                        setState(STATE_PULL_DOWN);
                    } else {
                        setState(STATE_PULL_DOWN_ENOUGH);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(int scrollState) {
                int lastState = state;
                if (scrollState == PageScroller.STATE_IDLE) {
                    if (state == STATE_PULL_DOWN_ENOUGH) {
                        setState(STATE_REFRESH);
                        scrollTo(getMinValue() + refreshTextView.getHeight(), 200);
                        NetUtils.request(new NetUtils.Callback() {
                            @Override
                            public void onResponse() {
                                if (pageScroller.getState() != PageScroller.STATE_IDLE) {
                                    PSLogger.i("onScrollStateChanged > page state:" + pageScroller.getState());
                                    return;
                                }
                                setState(STATE_REFRESH_FINISH);
                                postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getCurrentValue() <= getMinValue()) {
                                            setState(STATE_IDLE);
                                        } else {
                                            bannerScroller.scrollTo(bannerScroller.getMinValue(), 200, new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    setState(STATE_IDLE);
                                                }
                                            });
                                        }
                                    }
                                }, 200);
                            }
                        });
                    } else {
                        setState(STATE_IDLE);
                        scrollTo(getMinValue(), 200);
                    }
                }
                switch (scrollState) {
                    case PageScroller.STATE_IDLE:
                        break;
                    case PageScroller.STATE_TOUCH_SCROLL:
                        if (lastState == STATE_REFRESH) {
                            hasTouchedBeforeResponse = true;
                        }
                        setState(STATE_PULL_DOWN);
                        break;
                    case PageScroller.STATE_FLING:
                    case PageScroller.STATE_TOUCH_DOWN:
                    case PageScroller.STATE_TOUCH_UP:
                        break;
                }
                setScale((scrollState == PageScroller.STATE_FLING) ? 1f : 0.5f);
            }
        };
        // 下拉刷新距离是手指移动距离的0.5倍
        bannerScroller.setScale(0.5f);
        // listview元素向下滑动时，触顶停止滑动
        bannerScroller.setBreaks(Scroller.BREAK_FLING_DOWN);
        bannerScroller.setPriority(1);
        pageScroller.addScroller(bannerScroller);

        Scroller listViewScroller = new ListViewScroller(listView);
        listViewScroller.setPriority(2);
        pageScroller.addScroller(listViewScroller);
    }
}
