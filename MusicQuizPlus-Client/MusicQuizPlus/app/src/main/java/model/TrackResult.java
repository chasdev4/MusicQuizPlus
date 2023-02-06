package model;

import java.util.List;

import model.item.Album;

public class TrackResult {
    private String name;
    private String id;
    private String artistName;
    private List<Album> titleMatch;
    private List<Album> suggested;

    public TrackResult(String name, String id, String artistName, List<Album> titleMatch, List<Album> suggested) {
        this.name = name;
        this.id = id;
        this.artistName = artistName;
        this.titleMatch = titleMatch;
        this.suggested = suggested;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getArtistName() {
        return artistName;
    }

    public List<Album> getTitleMatch() {
        return titleMatch;
    }

    public List<Album> getSuggested() {
        return suggested;
    }
}
