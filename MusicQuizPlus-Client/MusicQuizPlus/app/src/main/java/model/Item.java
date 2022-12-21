package model;

import java.net.URI;

public abstract class Item {
    private final String _id;  // Spotify ID
    private final String _name;
    private final URI _photoUrl;

    public Item(String id, String name, URI photoUrl) {
        _id = id;
        _name = name;
        _photoUrl = photoUrl;
    }

    public Item (Item item) {
        _id = item.getId();
        _name = item.getName();
        _photoUrl = item.getPhotoUrl();
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public URI getPhotoUrl() {
        return _photoUrl;
    }
}
