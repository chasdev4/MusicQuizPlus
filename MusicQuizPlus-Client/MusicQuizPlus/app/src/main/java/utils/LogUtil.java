package utils;

import android.util.Log;

public class LogUtil {

    private String TAG;
    private String methodName;

    public LogUtil(String TAG, String methodName) {

        this.TAG = TAG;
        this.methodName = methodName;
    }

    private String messageFormat(String message) {
        return String.format("%s: %s", methodName, message);
    }

    public void e(String message) {
        Log.e(TAG, messageFormat(message));
    }

    public void i(String message) {
        Log.i(TAG, messageFormat(message));
    }

    public void d(String message) {
        Log.d(TAG, messageFormat(message));
    }

    public void v(String message) {
        Log.v(TAG, messageFormat(message));
    }

    public void w(String message) {
        Log.w(TAG, messageFormat(message));
    }

    public void w(String message, Throwable throwable) {
        Log.w(TAG, messageFormat(message), throwable);
    }

    public void wtf(String message) {
        Log.wtf(TAG, messageFormat(message));
    }

}
