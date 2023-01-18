package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;
import model.type.AlbumType;

// SUMMARY
// The Album item model stores album information

public class Album extends Item {

    private final List<String> artistNames;
    private final List<String> artistIds;
    private final AlbumType type;
    private final List<String> trackIds;

    public Album(String id, String name, List<PhotoUrl> photoUrl, List<String> artistNames,
                 List<String> artistIds, AlbumType type, List<String> trackIds) {
        super(id, name, photoUrl);
        this.artistNames = artistNames;
        this.artistIds = artistIds;
        this.type = type;
        this.trackIds = trackIds;
    }

    public List<String> getArtistNames() {
        return artistNames;
    }

    public List<String> getArtistIds() {
        return artistIds;
    }

    public AlbumType getType() {
        return type;
    }

    public List<String> getTrackIds() {
        return trackIds;
    }
}
