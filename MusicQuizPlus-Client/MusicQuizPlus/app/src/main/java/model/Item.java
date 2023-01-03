package model;

import java.net.URI;

import javax.annotation.Nullable;

public abstract class Item {
    private final String _id;  // Spotify ID
    private final String _name;
    private PhotoUrl[] _photoUrl;

    public Item(String id, String name, PhotoUrl[] photoUrl) {
        _id = id;
        _name = name;
        _photoUrl = photoUrl;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public PhotoUrl getPhotoUrl(short index) {
        return _photoUrl[index];
    }

    public PhotoUrl getPhotoUrl() {
        return _photoUrl[0];
    }
}
