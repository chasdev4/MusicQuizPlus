package model.item;

import java.util.List;

// SUMMARY
// The Track model stores track information

public class Track {
    private String id;
    private String name;
    private String albumId;
    private List<String> artistIds;
    private int popularity;
    private boolean popularityKnown;
    private String previewUrl;
    private boolean previewUrlKnown;

    public Track(String id, String name, String albumId, List<String> artistIds, int popularity, boolean popularityKnown, String previewUrl) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.artistIds = artistIds;
        this.popularity = popularity;
        this.popularityKnown = popularityKnown;
        if (previewUrl == null) {
            previewUrlKnown = false;
        }
        else {
            this.previewUrl = previewUrl;
            previewUrlKnown = true;
        }
    }

    public Track() {

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

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        popularityKnown = true;
        this.popularity = popularity;
    }

    public boolean isPopularityKnown() {
        return popularityKnown;
    }

    public void setPopularityKnown(boolean popularityKnown) {
        this.popularityKnown = popularityKnown;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public boolean isPreviewUrlKnown() {
        return previewUrlKnown;
    }

    public void setPreviewUrlKnown(boolean previewUrlKnown) {
        this.previewUrlKnown = previewUrlKnown;
    }
}
