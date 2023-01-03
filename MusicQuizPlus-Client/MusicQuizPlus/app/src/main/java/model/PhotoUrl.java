package model;

import java.net.URI;

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
