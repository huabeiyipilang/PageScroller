package xyz.penghaonan.pagescroller.utils;

import android.os.Build;
import android.view.View;
import android.widget.ListView;

/**
 * Copy from {@link androidx.core.widget.ListViewCompat}
 */
public final class ListViewCompat {

    public static void scrollListBy(ListView listView, int y) {
        if (Build.VERSION.SDK_INT >= 19) {
            // Call the framework version directly
            listView.scrollListBy(y);
        } else {
            // provide backport on earlier versions
            final int firstPosition = listView.getFirstVisiblePosition();
            if (firstPosition == ListView.INVALID_POSITION) {
                return;
            }

            final View firstView = listView.getChildAt(0);
            if (firstView == null) {
                return;
            }

            final int newTop = firstView.getTop() - y;
            listView.setSelectionFromTop(firstPosition, newTop);
        }
    }

    public static boolean canScrollList(ListView listView, int direction) {
        if (Build.VERSION.SDK_INT >= 19) {
            // Call the framework version directly
            return listView.canScrollList(direction);
        } else {
            // provide backport on earlier versions
            final int childCount = listView.getChildCount();
            if (childCount == 0) {
                return false;
            }

            final int firstPosition = listView.getFirstVisiblePosition();
            if (direction > 0) {
                final int lastBottom = listView.getChildAt(childCount - 1).getBottom();
                final int lastPosition = firstPosition + childCount;
                return lastPosition < listView.getCount()
                        || (lastBottom > listView.getHeight() - listView.getListPaddingBottom());
            } else {
                final int firstTop = listView.getChildAt(0).getTop();
                return firstPosition > 0 || firstTop < listView.getListPaddingTop();
            }
        }
    }

    private ListViewCompat() {
    }
}
