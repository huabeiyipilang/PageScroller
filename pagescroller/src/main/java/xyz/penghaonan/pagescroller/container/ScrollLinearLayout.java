package xyz.penghaonan.pagescroller.container;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import xyz.penghaonan.pagescroller.PageScroller;

public class ScrollLinearLayout extends LinearLayout {

    protected PageScroller scrollHelper;

    public ScrollLinearLayout(Context context) {
        this(context, null);
    }

    public ScrollLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        scrollHelper = new PageScroller(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (scrollHelper.dispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
