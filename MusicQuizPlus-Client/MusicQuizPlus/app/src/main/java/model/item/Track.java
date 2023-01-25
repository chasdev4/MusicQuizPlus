package model.item;

import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// SUMMARY
// The Track model stores track information

public class Track {
    private String id;
    private String name;
    private String albumId;
    private String albumName;
    private String artistId;                    // The primary artist
    private Map<String, String> artistsMap;     // All artists
    private Map<String, String> playlistIds;
    private int popularity;
    private boolean popularityKnown;
    private String previewUrl;
    private boolean previewUrlKnown;
    private boolean albumKnown;
    private String year;
    private boolean playable;

    public Track(String id,
                 String name,
                 String albumId,
                 String albumName,
                 String artistId,
                 Map<String, String> artistsMap,
                 int popularity,
                 boolean popularityKnown,
                 String previewUrl,
                 boolean albumKnown,
                 String year,
                 boolean playable) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistsMap = artistsMap;
        this.albumKnown = albumKnown;
        this.year = year;
        this.playable = playable;
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

    public void setPlaylistIds(Map<String, String> playlistIds) {
        this.playlistIds = playlistIds;
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

    public String getAlbumName() {
        return albumName;
    }

    public boolean isPlayable() {
        return playable;
    }

    public Map<String, String> getArtistsMap() {
        return artistsMap;
    }

    public String getYear() {
        return year;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    @Exclude
    public String getArtistName() {
        return artistsMap.get(artistId);
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setAlbumKnown(boolean albumKnown) {
        this.albumKnown = albumKnown;
    }

    public void setArtistsMap(Map<String, String> artistsMap) {
        this.artistsMap = artistsMap;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
