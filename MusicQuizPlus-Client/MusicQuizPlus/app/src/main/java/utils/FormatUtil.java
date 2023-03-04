package utils;

// SUMMARY
// Static methods for formatting strings

import android.text.Html;

import java.text.DecimalFormat;

public class FormatUtil {
    public static String removeHtml(String str) {
        return Html.fromHtml(str).toString();
    }
    public static String formatNumberWithComma(int num) {
        String numString = String.valueOf(num);

        if (num < 1000) {
            return numString;
        }

        return new DecimalFormat("#,###").format(num);
    }
}
