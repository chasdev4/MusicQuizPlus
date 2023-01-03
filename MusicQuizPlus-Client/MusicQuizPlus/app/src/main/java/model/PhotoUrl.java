package model;

import java.net.URI;

// SUMMARY
// The Photo URL model stores a URI to a web-hosted image and it's dimensions.
// Images retrieved from the API that have null width or height will be set to 0.

public class PhotoUrl {

    final URI url;
    final double width;
    final double height;

    public PhotoUrl(URI url, double width, double height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }
}
