package model;

import java.util.List;

import model.item.Album;

public class TrackResult {
    private String name;
    private String id;
    private String artistName;
    private List<Album> titleMatch;
    private List<Album> suggested;
    private String imageUrl;
    private boolean currentPageOne;

    public TrackResult(String name, String id, String artistName, List<Album> titleMatch, List<Album> suggested, String imageUrl) {
        this.name = name;
        this.id = id;
        this.artistName = artistName;
        this.titleMatch = titleMatch;
        this.suggested = suggested;
        this.imageUrl = imageUrl;
        currentPageOne = true;
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

    public boolean isCurrentPageOne(){ return currentPageOne; }

    public void changeTab() {
        currentPageOne = !currentPageOne;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
