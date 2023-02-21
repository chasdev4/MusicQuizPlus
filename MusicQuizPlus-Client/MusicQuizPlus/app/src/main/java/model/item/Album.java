package model.item;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.PhotoUrl;
import model.type.AlbumType;
import service.FirebaseService;

// SUMMARY
// The Album item model stores album information

public class Album implements Serializable {

    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String artistId;
    private Map<String, String> artistsMap;
    private AlbumType type;
    private List<String> trackIds;
    private boolean trackIdsKnown;
    private int followers;
    private boolean followersKnown;
    private String year;

    // Excluded from database
    private List<Track> tracks;

    public Album(String id, String name, List<PhotoUrl> photoUrl, String artistId,
                 Map<String, String> artistsMap, AlbumType type, List<String> trackIds,
                 boolean trackIdsKnown, int followers, boolean followersKnown, String year) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.artistId = artistId;
        this.artistsMap = artistsMap;
        this.type = type;
        this.trackIds = trackIds;
        this.trackIdsKnown = trackIdsKnown;
        this.followers = followers;
        this.followersKnown = followersKnown;
        this.year = year;
    }

    // Used for track result page
    public Album(String id, String name, List<PhotoUrl> photoUrl, String artistId, Map<String, String> artistsMa, List<Track> tracks) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.artistId = artistId;
        this.artistsMap = artistsMap;
        trackIds = new ArrayList<>();
        trackIdsKnown = false;
        followers = 0;
        followersKnown = false;
        this.tracks = tracks;
    }

    public Album() {

    }

    public AlbumType getType() {
        return type;
    }

    public List<String> getTrackIds() {
        return trackIds;
    }

    public void addTrackId(String trackId) {
        checkTrackIds();
        trackIds.add(trackId);
    }

    private void checkTrackIds() {
        if (trackIds == null) {
            trackIds = new ArrayList<>();
        }
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
            Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
            tracks.add(track);
        }
    }

    public void setTrackIds(List<String> trackIds) {
        this.trackIds = trackIds;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public Map<String, String> getArtistsMap() {
        return artistsMap;
    }

    public void setArtistsMap(Map<String, String> artistsMap) {
        this.artistsMap = artistsMap;
    }
}
