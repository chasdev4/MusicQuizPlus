package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private Map<String, String> albumIds;
    private Map<String, String>artistIds;
    private Map<String, String> playlistIds;
    private int level;
    private int xp;

    public User(Map<String, String> albumIds, Map<String, String> artistIds, Map<String, String> playlistIds, int level, int xp) {
        this.albumIds = albumIds;
        this.artistIds = artistIds;
        this.playlistIds = playlistIds;
        this.level = level;
        this.xp = xp;
    }

    public User() {
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        playlistIds = new HashMap<>();
        level = 1;
        xp = 0;
    }

    public Map<String, String> getAlbumIds() {
        return albumIds;
    }

    public boolean addAlbumId(String key, String albumId) {
        if (albumIds.containsValue(albumId)) {
            return false;
        }

        albumIds.put(key, albumId);
        return true;
    }

    public Map<String, String> getArtistIds() {
        return artistIds;
    }

    public boolean addArtistId(String key, String artistId) {
        if (artistIds.containsValue(artistId)) {
            return false;
        }
        artistIds.put(key, artistId);
        return true;
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

    public String removePlaylistId(String playlistId) {
        String key = "";
        for (Map.Entry<String, String> entry : playlistIds.entrySet()) {
            if (entry.getValue() == playlistId) {
                key = entry.getKey();
            }
        }

        return playlistIds.remove(key);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}
