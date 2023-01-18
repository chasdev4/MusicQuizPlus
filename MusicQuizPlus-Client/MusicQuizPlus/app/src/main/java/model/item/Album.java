package model.item;

import java.util.List;

import model.PhotoUrl;
import model.type.AlbumType;

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

    public Album(String id, String name, List<PhotoUrl> photoUrl, List<String> artistNames,
                 List<String> artistIds, AlbumType type, List<String> trackIds, boolean trackIdsKnown, int followers, boolean followersKnown) {
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
}
