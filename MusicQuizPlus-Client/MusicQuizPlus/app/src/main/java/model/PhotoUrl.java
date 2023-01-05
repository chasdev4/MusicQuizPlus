package model;

import java.io.Serializable;
import java.net.URI;

// SUMMARY
// The Photo URL model stores a URI to a web-hosted image and it's dimensions.
// Images retrieved from the API that have null width or height will be set to 0.

public class PhotoUrl implements Serializable {

    private final URI uri;
    private final String url;
    private final double width;
    private final double height;

    public PhotoUrl(URI uri, double width, double height) {
        this.uri = uri;
        url = uri.toString();
        this.width = width;
        this.height = height;
    }

    public PhotoUrl(String url, double width, double height) {
        this.uri = URI.create(url);
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public URI getUri() {
        return uri;
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
