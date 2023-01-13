package model.item;

import java.util.List;

// SUMMARY
// The Track model stores track information

public class Track {

    private final String id;
    private final String name;
    private final String albumId;
    private final List<String> artistIds;
    private short popularity;
    private boolean isPopularityKnown;

    public Track(String id, String name, String albumId, List<String> artistIds, short popularity, boolean isPopularityKnown) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.artistIds = artistIds;
        this.popularity = popularity;
        this.isPopularityKnown = isPopularityKnown;
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

    public short getPopularity() {
        return popularity;
    }

    public void setPopularity(short popularity) {
        isPopularityKnown = true;
        this.popularity = popularity;
    }

    public boolean isPopularitySet() {
        return isPopularityKnown;
    }
}
