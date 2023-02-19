package model.item;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import model.PhotoUrl;
import service.ItemService;

// SUMMARY
// The Track model stores track information

public class Track implements Serializable {
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

    // Excluded from database
    private List<PhotoUrl> photoUrl;

    public Track(String id,
                 String name,
                 String albumId,
                 String albumName,
                 boolean albumKnown,
                 String artistId,
                 Map<String, String> artistsMap,
                 int popularity,
                 boolean popularityKnown,
                 String previewUrl,
                 String year,
                 boolean playable,
                 List<PhotoUrl> photoUrl) {
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.albumName = albumName;
        this.albumKnown = albumKnown;
        this.artistId = artistId;
        this.artistsMap = artistsMap;
        this.year = year;
        this.playable = playable;
        playlistIds = new HashMap<>();
        this.popularity = popularity;
        this.popularityKnown = popularityKnown;
        if (previewUrl == null) {
            previewUrlKnown = false;
        } else {
            this.previewUrl = previewUrl;
            previewUrlKnown = true;
        }
        this.photoUrl = photoUrl;
    }

    // Used for generated quizzes' questions
    public Track(String id, String albumId) {
        this.id = id;
        this.albumId = albumId;
    }

    public Track() {

    }

    //#region Accessors
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public boolean isAlbumKnown() {
        return albumKnown;
    }

    public String getArtistId() {
        return artistId;
    }

    public Map<String, String> getArtistsMap() {
        return artistsMap;
    }

    public Map<String, String> getPlaylistIds() {
        return playlistIds;
    }

    public int getPopularity() {
        return popularity;
    }

    public boolean isPopularityKnown() {
        return popularityKnown;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public boolean isPreviewUrlKnown() {
        return previewUrlKnown;
    }

    public String getYear() {
        return year;
    }

    public boolean isPlayable() {
        return playable;
    }

    @Exclude
    public String getArtistName() {
        return artistsMap.get(artistId);
    }

    @Exclude
    public String getFeaturedArtistName() {
        if (artistsMap.size() != 2) {
            return null;
        }
        for (Map.Entry<String, String> entry : artistsMap.entrySet()) {
            if (!entry.getKey().equals(artistId)) {
                return entry.getValue();
            }
        }
        return null;
    }
    @Exclude
    public List<PhotoUrl> getPhotoUrl() { return photoUrl; }
    //#endregion

    //#region Mutators
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setAlbumKnown(boolean albumKnown) {
        this.albumKnown = albumKnown;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public void setArtistsMap(Map<String, String> artistsMap) {
        this.artistsMap = artistsMap;
    }

    public void setPlaylistIds(Map<String, String> playlistIds) {
        this.playlistIds = playlistIds;
    }

    public void setPopularity(int popularity) {
        popularityKnown = true;
        this.popularity = popularity;
    }

    public void setPopularityKnown(boolean popularityKnown) {
        this.popularityKnown = popularityKnown;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setPreviewUrlKnown(boolean previewUrlKnown) {
        this.previewUrlKnown = previewUrlKnown;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public boolean addPlaylistId(String key, String playlistId) {
        if (playlistIds.containsValue(playlistId)) {
            return false;
        }
        playlistIds.put(key, playlistId);
        return true;
    }
    //#endregion
}
