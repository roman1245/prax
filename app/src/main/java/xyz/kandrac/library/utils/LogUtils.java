package xyz.kandrac.library.utils;

import android.util.Log;

import xyz.kandrac.library.BuildConfig;

public final class LogUtils {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable exception) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, exception);
        }
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void w(String tag, String message, Exception exception) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, exception);
        }
    }
}
