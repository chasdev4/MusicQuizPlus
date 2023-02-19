package model;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.SearchFilter;

public class SearchResult <T> {
    private final SearchFilter type;
    private final T object;


    public SearchResult(SearchFilter type, T object) {
        this.type = type;
        this.object = object;
    }

    public SearchFilter getType() {
        return type;
    }

    public Artist getArtist() {
        if (type != SearchFilter.ARTIST) {
            return null;
        }
        return (Artist) object;
    }

    public Album getAlbum() {
        if (type != SearchFilter.ALBUM) {
            return null;
        }
        return (Album) object;
    }

    public Track getTrack() {
        if (type != SearchFilter.SONG) {
            return null;
        }
        return (Track) object;
    }

    public Playlist getPlaylist() {
        if (type != SearchFilter.PLAYLIST) {
            return null;
        }
        return (Playlist) object;
    }
}
