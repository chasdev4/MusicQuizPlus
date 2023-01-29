package utils;

// SUMMARY
// Static methods for formatting strings

import android.text.Html;

public class FormatUtil {
    public static String removeHtml(String str) {
        return Html.fromHtml(str).toString();
    }
}
