package utils;

import android.util.Log;

import java.util.List;

import model.ValidationObject;

public class ValidationUtil {

    public static boolean nullCheck(List<ValidationObject> vObjects, String TAG, String formattedMethodName) {
        boolean result = false;
        for (ValidationObject obj : vObjects) {
            if (obj.object == null) {
                if (result == false) {
                    String message = String.format("%s %s provided was null", formattedMethodName, obj.cls.getSimpleName());
                    switch (obj.severity) {
                        case NONE:
                            Log.i(TAG, message);
                            break;
                        case LOW:
                            Log.w(TAG, message);
                            break;
                        case HIGH:
                            Log.e(TAG, message);
                            result = true;
                            break;
                    }
                    result = true;
                }
            }
        }
        return result;
    }
}

