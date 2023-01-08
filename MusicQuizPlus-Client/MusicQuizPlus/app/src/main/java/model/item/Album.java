package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;
import model.type.AlbumType;

// SUMMARY
// The Album item model stores album information

public class Album extends Item {

    private final List<String> _artistNames;
    private final List<String> _artistIds;
    private final AlbumType type;

    public Album(String id, String name, List<PhotoUrl> photoUrl, List<String> artistNames,
                 List<String> artistIds, AlbumType type) {
        super(id, name, photoUrl);
        _artistNames = artistNames;
        _artistIds = artistIds;
        this.type = type;
    }

    public List<String> get_artistNames() {
        return _artistNames;
    }

    public List<String> get_artistIds() {
        return _artistIds;
    }

    public AlbumType getType() {
        return type;
    }
}
