package model.item;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

import model.PhotoUrl;
import model.type.AlbumType;
import service.FirebaseService;

// SUMMARY
// The Album item model stores album information

public class Album {

    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private List<String> artistNames;
    private List<String> artistIds;
    private AlbumType type;
    private List<String> trackIds;
    private boolean trackIdsKnown;
    private int followers;
    private boolean followersKnown;
    private String year;

    // Excluded from database
    private List<Track> tracks;

    public Album(String id, String name, List<PhotoUrl> photoUrl, List<String> artistNames,
                 List<String> artistIds, AlbumType type, List<String> trackIds, boolean trackIdsKnown,
                 int followers, boolean followersKnown, String year) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.artistNames = artistNames;
        this.artistIds = artistIds;
        this.type = type;
        this.trackIds = trackIds;
        this.trackIdsKnown = trackIdsKnown;
        this.followers = followers;
        this.followersKnown = followersKnown;
        this.year = year;
    }

    public Album() {

    }

    public List<String> getArtistNames() {
        return artistNames;
    }

    public List<String> getArtistIds() {
        return artistIds;
    }

    public AlbumType getType() {
        return type;
    }

    public List<String> getTrackIds() {
        return trackIds;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public boolean isFollowersKnown() {
        return followersKnown;
    }

    public void setFollowersKnown(boolean followersKnown) {
        this.followersKnown = followersKnown;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PhotoUrl> getPhotoUrl() {
        return photoUrl;
    }

    public boolean isTrackIdsKnown() {
        return trackIdsKnown;
    }

    public void setTrackIdsKnown(boolean trackIdsKnown) {
        this.trackIdsKnown = trackIdsKnown;
    }

    public String getYear() {
        return year;
    }

    @Exclude
    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public void initCollection(DatabaseReference db) {
        initTracks(db);
    }

    private void initTracks(DatabaseReference db) {
        tracks = new ArrayList<>();
        for (String trackId : trackIds) {
            tracks.add(FirebaseService.checkDatabase(db, "Tracks", trackId, Track.class));
        }
    }
}
