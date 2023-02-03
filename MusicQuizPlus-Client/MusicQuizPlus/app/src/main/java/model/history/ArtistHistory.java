package model.history;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import model.item.Track;

public class ArtistHistory {
    private Map<String, TopicHistory> albums;
    private int albumsTotal;
    private int albumsCount;

    public ArtistHistory(Map<String, TopicHistory> albums, int albumsTotal, int albumsCount) {
        this.albums = albums;
        this.albumsTotal = albumsTotal;
        this.albumsCount = albumsCount;
    }

    public ArtistHistory() {}

    public Map<String, TopicHistory> getAlbums() { return albums; }
    public int getAlbumsTotal() { return albumsTotal; }
    public int getAlbumsCount() { return albumsCount; }

    public void setAlbums(Map<String, TopicHistory> albums) { this.albums = albums; }
    public void setAlbumsTotal(int albumsTotal) { this.albumsTotal = albumsTotal; }
    public void setAlbumsCount(int albumsCount) { this.albumsCount = albumsCount; }

    public boolean addTrackId(String key, Track track) {
        if (albums == null || albums.size() == 0) {
            albums = new HashMap<>();
            albums.put(track.getAlbumId(), new TopicHistory());
            albums.get(track.getAlbumId()).setTrackIds(new HashMap<>());
        }

        if (albums.get(track.getAlbumId()).getTrackIds() == null) {
            albums.get(track.getAlbumId()).setTrackIds(new HashMap<>());
        }

        if (albums.get(track.getAlbumId()).getTrackIds().containsValue(track.getId())) {
            return false;
        }
        albums.get(track.getAlbumId()).getTrackIds().put(key, track.getId());
        return true;
    }
}
