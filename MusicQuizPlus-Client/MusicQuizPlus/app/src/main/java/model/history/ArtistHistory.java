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

    public boolean addTrackId(String key, Track track, int total) {
        if (albums == null) {
            albums = new HashMap<>();
        }

        if (!albums.containsKey(track.getAlbumId())) {
            albums.put(track.getAlbumId(), new TopicHistory(total));
        }

        if (albums.get(track.getAlbumId()).isTrackIdsNull()) {
            albums.get(track.getAlbumId()).setTrackIds(new HashMap<>());
        }

        if (albums.get(track.getAlbumId()).getTrackIds().containsValue(track.getId())) {
            return false;
        }
        return albums.get(track.getAlbumId()).addTrackId(key, track.getId());
    }
}
