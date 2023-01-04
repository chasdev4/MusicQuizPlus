package model;

import java.io.Serializable;
import java.net.URI;

// SUMMARY
// The Photo URL model stores a URI to a web-hosted image and it's dimensions.
// Images retrieved from the API that have null width or height will be set to 0.

public class PhotoUrl implements Serializable {

    private final URI url;
    private final double width;
    private final double height;

    public PhotoUrl(URI url, double width, double height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public URI getUrl() {
        return url;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
