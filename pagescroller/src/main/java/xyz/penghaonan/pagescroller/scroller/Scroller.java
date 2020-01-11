package xyz.penghaonan.pagescroller.scroller;

import xyz.penghaonan.pagescroller.PageScroller;

public abstract class Scroller {

    public static final int BREAK_MOVE_UP = 0x1;
    public static final int BREAK_MOVE_DOWN = 0x2;
    public static final int BREAK_FLING_UP = 0x4;
    public static final int BREAK_FLING_DOWN = 0x8;

    private boolean enable = true;
    private int priority = 999;
    private int breaks;
    private boolean enableFling = true;
    private int state = PageScroller.STATE_IDLE;
    private boolean isInControlled;

    /**
     * @return 优先级
     */
    public int priority() {
        return priority;
    }

    /**
     * 滚动距离
     *
     * @param deltaY deltaY<0 列表上移， deltaY>0 列表下移
     * @return
     */
    abstract public float consumeY(float deltaY);

    /**
     * 是否能消费
     *
     * @param deltaY
     * @return
     */
    abstract public boolean canConsumeY(float deltaY);

    public void changeScrollState(int scrollState) {
        state = scrollState;
        onScrollStateChanged(state);
    }

    /**
     * 获取当前状态
     *
     * @return
     */
    public int getState() {
        return state;
    }

    /**
     * 滚动状态变化
     *
     * @param scrollState
     */
    public void onScrollStateChanged(int scrollState) {
    }

    public boolean isInControlled() {
        return isInControlled;
    }

    public void changeControlledState(boolean controlled) {
        isInControlled = controlled;
        onControlledChanged(isInControlled);
    }

    /**
     * 被控制状态
     *
     * @param controlled
     */
    public void onControlledChanged(boolean controlled) {
    }

    /**
     * 是否可滚动
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnableFling() {
        return enableFling;
    }

    public void setEnableFling(boolean enableFling) {
        this.enableFling = enableFling;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isBreak(int type) {
        return (breaks & type) == type;
    }

    public void setBreaks(int breaks) {
        this.breaks = breaks;
    }
}