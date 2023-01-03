package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;

// SUMMARY
// The Track item model stores track information

public class Track extends Item {

    private final String _albumName;
    private final String _albumId;
    private final List<String> _artistNames;
    private final List<String> _artistIds;

    public Track(String id, String name, List<PhotoUrl> photoUrl, String albumName, String albumId,
                 List<String> artistNames, List<String> artistIds) {
        super(id, name, photoUrl);
        _albumName = albumName;
        _albumId = albumId;
        _artistNames = artistNames;
        _artistIds = artistIds;
    }

    public String get_albumName() {
        return _albumName;
    }

    public String get_albumId() {
        return _albumId;
    }

    public List<String> get_artistNames() {
        return _artistNames;
    }

    public List<String> get_artistIds() {
        return _artistIds;
    }
}
