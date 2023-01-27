package model;

import android.util.Log;

import java.io.Serializable;
import java.net.URI;

import utils.LogUtil;

// SUMMARY
// The Photo URL model stores a URI to a web-hosted image and it's dimensions.
// Images retrieved from the API that have null width or height will be set to 0.

public class PhotoUrl implements Serializable {
    private String url;
    private double width;
    private double height;

    private final static String TAG = "PhotoUrl.java";

    public PhotoUrl(String url, double width, double height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public PhotoUrl() {

    }

    public PhotoUrl(String url, String width, String height) {
        this.url = url;
        validateDimensions(width, height);
    }

    private void validateDimensions(String strWidth, String strHeight) {
        LogUtil log = new LogUtil(TAG, "validateDimensions");
        if (strWidth == null || strHeight == null) {
            log.w(String.format("Width or Height of image was null. Setting Width and Height to 0 for image: %s", url));
            width = 0;
            height = 0;
            return;
        }
        width = Double.parseDouble(strWidth);
        height = Double.parseDouble(strHeight);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }
}
