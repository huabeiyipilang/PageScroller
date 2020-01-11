package xyz.penghaonan.pagescroller.scroller;

import android.widget.AbsListView;
import android.widget.ListView;

import java.lang.reflect.Field;

import xyz.penghaonan.pagescroller.PageScroller;
import xyz.penghaonan.pagescroller.utils.PSLogger;
import xyz.penghaonan.pagescroller.utils.ListViewCompat;

public class ListViewScroller extends Scroller {

    private ListView listView;
    private AbsListView.OnScrollListener listener;

    public ListViewScroller(ListView listView) {
        this.listView = listView;
    }

    @Override
    public boolean canConsumeY(float deltaY) {
        float scrollDeltaY = -deltaY;
        return canScrollList(scrollDeltaY);
    }

    @Override
    public float consumeY(float deltaY) {
        float scrollDeltaY = -deltaY;
        // deltaY > 0, 列表内容向上
        PSLogger.i("ListViewScroller > consumeY > scrollDeltaY:" + scrollDeltaY);
        if (canScrollList(scrollDeltaY)) {
            scrollListBy(scrollDeltaY);
            return 0;
        } else {
            return deltaY;
        }
    }

    @Override
    public void onScrollStateChanged(int scrollState) {
        switch (scrollState) {
            case PageScroller.STATE_IDLE:
            case PageScroller.STATE_FLING:
                if (listener != null) {
                    listener.onScrollStateChanged(listView, scrollState);
                }
                break;
            case PageScroller.STATE_TOUCH_SCROLL:
                listener = hookListViewScrollListener(listView);
                if (listener != null) {
                    listener.onScrollStateChanged(listView, scrollState);
                }
                break;
        }
    }

    private boolean canScrollList(float deltaY) {
        int direction;
        if (deltaY < 0) {
            direction = -1;
        } else if (deltaY > 0) {
            direction = 1;
        } else {
            return false;
        }
        return ListViewCompat.canScrollList(listView, direction);
    }

    private void scrollListBy(float y) {
        PSLogger.i("ListViewScroller > scrollListBy > y:" + y);
        ListViewCompat.scrollListBy(listView, (int) y);
    }

    private AbsListView.OnScrollListener hookListViewScrollListener(ListView view) {
        if (view == null) {
            return null;
        }

        try {
            Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
            field.setAccessible(true);
            return (AbsListView.OnScrollListener) field.get(view);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
