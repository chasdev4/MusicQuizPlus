package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private List<String> albumIds;
    private List<String> artistIds;
    private List<String> playlistIds;
    private int level;
    private int xp;

    public User(List<String> albumIds, List<String> artistIds, List<String> playlistIds, int level, int xp) {
        this.albumIds = albumIds;
        this.artistIds = artistIds;
        this.playlistIds = playlistIds;
        this.level = level;
        this.xp = xp;
    }

    public User() {
        albumIds = new ArrayList<>();
        artistIds = new ArrayList<>();
        playlistIds = new ArrayList<>();
        level = 1;
        xp = 0;
    }

    public List<String> getAlbumIds() {
        return albumIds;
    }

    public void addAlbumId(String albumId) {
        albumIds.add(albumId);
    }

    public List<String> getArtistIds() {
        return artistIds;
    }

    public void addArtistId(String artistId) {
        artistIds.add(artistId);
    }

    public List<String> getPlaylistIds() {
        return playlistIds;
    }

    public void addPlaylistId(String playlistId) {
        playlistIds.add(playlistId);
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
