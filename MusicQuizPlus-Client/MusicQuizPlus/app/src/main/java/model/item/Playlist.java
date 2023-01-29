package model.item;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.PhotoUrl;
import service.FirebaseService;
import utils.LogUtil;

// SUMMARY
// The Playlist model stores playlist information

public class Playlist implements Serializable {
    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String owner;
    private String description;
    private List<String> trackIds;
    private int followers;
    private boolean followersKnown;
    private int averagePopularity;

    // Excluded from Database
    private Map<Integer, Track> tracks;

    private final String TAG = "Playlist.java";

    public Playlist(String id, String name, List<PhotoUrl> photoUrl, String owner, String description) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.description = description;
        followers = 0;
        followersKnown = false;
        trackIds = new ArrayList<>();
        tracks = new HashMap<>();
    }

    public Playlist() {

    }

    //#region Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public List<PhotoUrl> getPhotoUrl() {
        return photoUrl;
    }
    public String getOwner() {
        return owner;
    }
    public String getDescription() {
        return description;
    }
    public List<String> getTrackIds() {
        if (trackIds == null) {
            trackIds = new ArrayList<>();
        }
        return trackIds;
    }
    public int getFollowers() { return followers; }
    public boolean isFollowersKnown() { return followersKnown; }
    public int getAveragePopularity() { return averagePopularity; }
    @Exclude
    public Map<Integer, Track> getTracks() { return tracks; }
    @Exclude
    public List<Track> getTracksListFromMap() {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < this.tracks.size(); i++) {
            tracks.add(this.tracks.get(i));
        }
        return tracks;
    }
    //#endregion

    //#region Setters
    public void setFollowers(int followers) { this.followers = followers; }
    public void setFollowersKnown(boolean followersKnown) { this.followersKnown = followersKnown; }
    public void setAveragePopularity(int avg) { averagePopularity = avg; }
    //#endregion

    public void addTrackId(String trackId) { trackIds.add(trackId); }

    public void putTrack(int i, Track track) { tracks.put(i, track); }

    public void initCollection(DatabaseReference db) { initTracks(db); }

    private void initTracks(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initTracks");
        Map<Integer, List<String>> data = new HashMap<>();
        tracks = new HashMap<>();
        int nThreads = (trackIds.size() >= 10) ? trackIds.size() / 10 : trackIds.size() / 2;
        int remainder = trackIds.size() % 10;
        if (remainder > 0) {
            nThreads++;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        int start = 0;
        int end = 10;

        for (int i = 0; i < nThreads; i++) {
            data.put(i, trackIds.subList(start, end));
            start += 10;
            if (i == nThreads - 2) {
                end = (10 * (nThreads - 1)) + remainder;
            } else {
                end += 10;
            }
        }

        for (int i = 0; i < nThreads; i++) {
            int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    int j = 0;
                    for (String trackId : data.get(finalI)) {
                        Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
                        if (track != null) {
                            tracks.put((finalI * 10 + j), track);
                            j++;
                        } else {
                            log.w(String.format("%s is missing from the database.", trackId));
                        }
                    }
                }
            });

        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.e(e.getMessage());
        }
    }
}
