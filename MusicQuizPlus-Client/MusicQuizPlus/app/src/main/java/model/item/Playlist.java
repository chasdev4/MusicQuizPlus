package model.item;

import model.Item;
import model.PhotoUrl;

public class Playlist extends Item {

    private final String _owner;

    private final String _description;

    public Playlist(String id, String name, PhotoUrl[] photoUrl, String owner, String description) {
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
