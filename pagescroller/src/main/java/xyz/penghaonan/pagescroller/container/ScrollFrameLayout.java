package xyz.penghaonan.pagescroller.container;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import xyz.penghaonan.pagescroller.PageScroller;

public class ScrollFrameLayout extends FrameLayout {

    protected PageScroller pageScroller;

    public ScrollFrameLayout(Context context) {
        this(context, null);
    }

    public ScrollFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        pageScroller = new PageScroller(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (pageScroller.dispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
