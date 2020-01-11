package xyz.penghaonan.pagescroller.scroller;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import xyz.penghaonan.pagescroller.PageScroller;

import static xyz.penghaonan.pagescroller.PageScroller.STATE_TOUCH_SCROLL;

public abstract class ValueScroller extends Scroller {

    private float minValue, maxValue;
    private ValueAnimator valueAnimator;
    private float scale = 1f;

    public ValueScroller(float minValue, float maxValue) {
        updateMaxMinValue(minValue, maxValue);
    }

    public void updateMaxMinValue(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setEnable(maxValue > minValue);
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean canConsumeY(float deltaY) {
        deltaY = deltaY * scale;
        float currentValue = getCurrentValue();
        if (deltaY > 0) {
            // 下滑
            if (currentValue >= maxValue) {
                return false;
            }
        } else {
            // 上滑
            if (currentValue <= minValue) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float consumeY(float deltaY) {
        float originDeltaY = deltaY;
        deltaY = originDeltaY * scale;
        float currentValue = getCurrentValue();
        if (deltaY > 0) {
            // 下滑
            if (currentValue >= maxValue) {
                return originDeltaY;
            } else if (currentValue + deltaY > maxValue) {
                // 消费掉一部分
                float resumedDeltaY = maxValue - currentValue;
                setCurrentValue(maxValue);
                originDeltaY = originDeltaY - resumedDeltaY;
            } else {
                // 消费全部
                setCurrentValue(currentValue + deltaY);
                originDeltaY = 0;
            }
        } else {
            // 上滑
            if (currentValue <= minValue) {
                return originDeltaY;
            } else if (currentValue + deltaY < minValue) {
                float resumeDeltaY = minValue - currentValue;
                setCurrentValue(minValue);
                originDeltaY = originDeltaY - resumeDeltaY;
            } else {
                setCurrentValue(currentValue + deltaY);
                originDeltaY = 0;
            }
        }
        return originDeltaY;
    }

    public void scrollTo(float value, long duration) {
        scrollTo(value, duration, null);
    }

    public void scrollTo(float value, long duration, AnimatorListenerAdapter animListener) {
        if (getState() != PageScroller.STATE_IDLE) {
            if (animListener != null) {
                animListener.onAnimationEnd(null);
            }
            return;
        }
        final float targetVal;
        if (value > maxValue) {
            targetVal = maxValue;
        } else if (value < minValue) {
            targetVal = minValue;
        } else {
            targetVal = value;
        }
        stopAutoAnim();
        valueAnimator = ValueAnimator.ofFloat(getCurrentValue(), targetVal);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setCurrentValue((Float) animation.getAnimatedValue());
            }
        });
        if (animListener != null) {
            valueAnimator.addListener(animListener);
        }
        valueAnimator.start();
    }

    private void stopAutoAnim() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void onScrollStateChanged(int scrollState) {
        if (scrollState == STATE_TOUCH_SCROLL) {
            stopAutoAnim();
        }
    }

    abstract protected float getCurrentValue();

    abstract protected void setCurrentValue(float value);
}
