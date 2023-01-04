package model;

// SUMMARY
// The Item model is an abstract class that is the parent and a framework for item models

import java.util.List;

public abstract class Item {
    private final String _id;  // Spotify ID
    private final String _name;

    private List<PhotoUrl> _photoUrl;

    public Item(String id, String name, List<PhotoUrl> photoUrl) {
        _id = id;
        _name = name;
        _photoUrl = photoUrl;
    }

    public String get_id() {
        return _id;
    }

    public String get_name() {
        return _name;
    }

    public List<PhotoUrl> get_photoUrl() {
        return _photoUrl;
    }
}
