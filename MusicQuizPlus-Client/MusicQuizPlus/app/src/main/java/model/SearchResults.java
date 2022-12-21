package model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;

enum ResultType {
    ALBUM,
    ARTIST,
    PLAYLIST,
    TRACK
}

public class SearchResults {
    private List<Album> _albums;
    private List<Artist> _artists;
    private List<Playlist> _playlists;
    private List<Track> _tracks;

    public SearchResults() {
        _albums = new ArrayList<Album>();
        _artists = new ArrayList<Artist>();
        _playlists = new ArrayList<Playlist>();
        _tracks = new ArrayList<Track>();
    }

    public void addToResults(Item item, ResultType type) {
        switch (type) {
            case ALBUM:
                _albums.add(new Album(item));
                break;
            case ARTIST:
                _artists.add(new Artist(item));
                break;
            case PLAYLIST:
                _playlists.add(new Playlist(item));
                break;
            case TRACK:
                _tracks.add(new Track(item));
                break;
        }
    }

    public List<Item> getResults() {
        // TODO: Return all results
        return new ArrayList<Item>();
    }

    public List<Album> getAlbums() {
        return _albums;
    }

    public List<Artist> getArtists() {
        return _artists;
    }
    public List<Playlist> getPlaylists() {
        return _playlists;
    }
    public List<Track> getTracks() {
        return _tracks;
    }
}
