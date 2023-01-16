package model.item;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.PhotoUrl;

// SUMMARY
// The Playlist model stores playlist information

public class Playlist implements Serializable {
    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String owner;
    private String description;
    private List<String> trackIds;
    private boolean trackIdsKnown;
    private int followers;
    private boolean followersKnown;

    // Excluded from Database
    private List<Track> tracks;

    public Playlist(String id, String name, List<PhotoUrl> photoUrl, String owner, String description,
                    boolean trackIdsKnown, int followers, boolean followersKnown) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.description = description;
        this.trackIdsKnown = trackIdsKnown;
        this.followers = followers;
        this.followersKnown = followersKnown;
        trackIds = new ArrayList<>();
        tracks = new ArrayList<>();
    }

    public Playlist() {

    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
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

    public List<String> getTrackIds() {
        return trackIds;
    }

    public void addTrackId(String trackId) {
        trackIds.add(trackId);
    }

    public boolean isTrackIdsKnown() {
        return trackIdsKnown;
    }

    public void setTrackIdsKnown(boolean trackIdsKnown) {
        this.trackIdsKnown = trackIdsKnown;
    }

    @Exclude
    public List<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
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
}
