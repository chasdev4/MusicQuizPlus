package model.item;

import java.util.List;

import model.PhotoUrl;

// SUMMARY
// The Track model stores track information

public class Track {

    private final String id;
    private final String name;
    private final String albumId;
    private final List<String> artistIds;

    public Track(String id, String name, String albumId, List<String> artistIds) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.artistIds = artistIds;
    }

    public String getAlbumId() {
        return albumId;
    }

    public List<String> getArtistIds() {
        return artistIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
