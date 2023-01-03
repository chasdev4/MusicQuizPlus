package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;

// SUMMARY
// The Album item model stores album information

public class Album extends Item {

    private final List<String> _artistNames;
    private final List<String> _artistIds;

    public Album(String id, String name, List<PhotoUrl> photoUrl, List<String> artistNames,
                 List<String> artistIds) {
        super(id, name, photoUrl);
        _artistNames = artistNames;
        _artistIds = artistIds;
    }

    public List<String> get_artistNames() {
        return _artistNames;
    }

    public List<String> get_artistIds() {
        return _artistIds;
    }
}
