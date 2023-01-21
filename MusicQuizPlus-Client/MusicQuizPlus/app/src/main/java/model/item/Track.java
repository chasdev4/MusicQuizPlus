package model.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// SUMMARY
// The Track model stores track information

public class Track {
    private String id;
    private String name;
    private String albumId;
    private List<String> artistIds;
    private Map<String, String> playlistIds;
    private int popularity;
    private boolean popularityKnown;
    private String previewUrl;
    private boolean previewUrlKnown;
    private boolean albumKnown;

    public Track(String id, String name, String albumId, List<String> artistIds, int popularity, boolean popularityKnown, String previewUrl, boolean albumKnown, String year) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.artistIds = artistIds;
        this.albumKnown = albumKnown;
        playlistIds = new HashMap<>();
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

    public Map<String, String> getPlaylistIds() {
        return playlistIds;
    }

    public boolean addPlaylistId(String key, String playlistId) {
        if (playlistIds.containsValue(playlistId)) {
            return false;
        }
        playlistIds.put(key, playlistId);
        return true;
    }

    public boolean isAlbumKnown() {
        return albumKnown;
    }
}
