package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;

// SUMMARY
// The Playlist item model stores playlist information

public class Playlist extends Item {

    private final String _owner;

    private final String _description;

    public Playlist(String id, String name, List<PhotoUrl> photoUrl, String owner, String description) {
        super(id, name, photoUrl);
        _owner = owner;
        _description = description;
    }

    public String get_description() {
        return _description;
    }

    public String get_owner() {
        return _owner;
    }
}
