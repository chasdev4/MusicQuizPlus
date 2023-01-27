package utils;

import android.util.Log;

import java.util.List;

import model.ValidationObject;

public class ValidationUtil {

    public static boolean nullCheck(List<ValidationObject> vObjects, LogUtil log) {
        boolean result = false;
        for (ValidationObject obj : vObjects) {
            if (obj.object == null) {
                if (result == false) {
                    String message = String.format("%s provided was null", obj.cls.getSimpleName());
                    switch (obj.severity) {
                        case NONE:
                            log.i(message);
                            break;
                        case LOW:
                            log.w(message);
                            break;
                        case HIGH:
                            log.e(message);
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

