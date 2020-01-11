package xyz.penghaonan.pagescrollersample.simulation;

import android.os.Handler;
import android.os.Looper;

public class NetUtils {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onResponse();
    }

    public static void request(final Callback callback) {
        request(1000, callback);
    }

    public static void request(final long duration, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse();
                        }
                    });
                }
            }
        }.start();
    }
}
