package xyz.penghaonan.pagescroller.container;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public interface PSContainer {
    Context getContext();

    boolean dispatchTouchEvent(MotionEvent ev);

    class Factory {
        public static PSContainer create(View view) {
            return new ViewContainer(view);
        }

        public static PSContainer create(Activity activity) {
            return new ActivityContainer(activity);
        }
    }

    class ViewContainer implements PSContainer {

        private View container;

        ViewContainer(View container) {
            this.container = container;
            container.setClickable(true);
        }

        @Override
        public Context getContext() {
            if (container == null) {
                return null;
            }
            return container.getContext();
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (container == null) {
                return false;
            }
            return container.dispatchTouchEvent(ev);
        }
    }

    class ActivityContainer implements PSContainer {

        private Activity activity;

        ActivityContainer(Activity activity) {
            this.activity = activity;
        }

        @Override
        public Context getContext() {
            if (activity == null) {
                return null;
            }
            return activity;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (activity == null) {
                return false;
            }
            return activity.dispatchTouchEvent(ev);
        }
    }
}
