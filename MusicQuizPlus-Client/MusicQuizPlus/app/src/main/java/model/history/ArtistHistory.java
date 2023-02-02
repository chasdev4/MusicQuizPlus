package model.history;

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

    public boolean addTrackId(String key, Track track) {
        if (albums == null) {
            albums = new HashMap<>();
        }
        if (albums.get(track.getAlbumId()) == null) {
            albums.get(track.getAlbumId()).setTrackIds(new HashMap<>());
        }

        if (albums.get(track.getAlbumId()).getTrackIds().containsValue(track.getId())) {
            return false;
        }
        albums.get(track.getAlbumId()).getTrackIds().put(key, track.getId());
        return true;
    }
}
