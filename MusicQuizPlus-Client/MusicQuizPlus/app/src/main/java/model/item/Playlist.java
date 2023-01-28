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
import utils.ValidationUtil;

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
    private List<Track> tracks;

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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhotoUrl(List<PhotoUrl> photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getAveragePopularity() {
        return averagePopularity;
    }

    public void setAveragePopularity(int averagePopularity) {
        this.averagePopularity = averagePopularity;
    }

    public void initCollection(DatabaseReference db) {
        initTracks(db);
    }

    private void initTracks(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initTracks");
        tracks = new ArrayList<>();
        Map<Integer, List<String>> data = new HashMap<>();
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
            start+=10;
            if (i == nThreads -2) {
                end = (10 * (nThreads - 1)) + remainder;
            }
            else {
                end+=10;
            }
        }

        for (int i = 0; i < nThreads; i++) {
            int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (String trackId : data.get(finalI)) {
                        Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
                        if (track != null) {
                            tracks.add(track);
                        }
                        else {
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
