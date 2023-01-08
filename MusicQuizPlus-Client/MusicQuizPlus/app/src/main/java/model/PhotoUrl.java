package model;

import java.io.Serializable;
import java.net.URI;

// SUMMARY
// The Photo URL model stores a URI to a web-hosted image and it's dimensions.
// Images retrieved from the API that have null width or height will be set to 0.

public class PhotoUrl implements Serializable {
    private final String url;
    private double width;
    private double height;

    public PhotoUrl(String url, double width, double height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public PhotoUrl(String url, String width, String height) {
        this.url = url;
        validateDimensions(width, height);
    }

    private void validateDimensions(String strWidth, String strHeight) {
        if (strWidth == null || strHeight == null) {
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
