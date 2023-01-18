package model;

// SUMMARY
// The Item model is an abstract class that is the parent and a framework for item models

import java.util.List;

public abstract class Item {
    private final String id;  // Spotify ID
    private final String name;
    private List<PhotoUrl> photoUrl;

    public Item(String id, String name, List<PhotoUrl> photoUrl) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PhotoUrl> getPhotoUrl() {
        return photoUrl;
    }
}
