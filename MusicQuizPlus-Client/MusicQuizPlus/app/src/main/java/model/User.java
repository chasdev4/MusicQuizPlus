package model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.history.ArtistHistory;
import model.history.GeneratedQuizHistory;
import model.history.TopicHistory;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import service.FirebaseService;
import utils.LogUtil;

// SUMMARY
// The User model stores information tied to the current user

public class User implements Serializable {
    private int level;
    private int xp;
    private Difficulty difficulty;
    private Map<String, String> albumIds;
    private Map<String, String> artistIds;
    private Map<String, String> playlistIds;
    private List<String> historyIds;
    private Map<String, TopicHistory> playlistHistory;
    private Map<String, ArtistHistory> artistHistory;
    private Map<String, Map<String, String>> generatedQuizHistory;

    // Excluded from Database
    private Map<String, Playlist> playlists;
    private Map<String, Artist> artists;
    private LinkedList<Track> history;

    private final static String TAG = "User.java";
    private final static int HISTORY_LIMIT = 50;

    public User() {
        level = 1;
        xp = 0;
        difficulty = Difficulty.EASY;
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        playlistIds = new HashMap<>();
        historyIds = new LinkedList<>();
    }

    //#region Accessors
    public int getLevel() {
        return level;
    }
    public int getXp() {
        return xp;
    }
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public Map<String, String> getAlbumIds() {
        return albumIds;
    }
    public Map<String, String> getArtistIds() {
        return artistIds;
    }
    public Map<String, String> getPlaylistIds() {
        return playlistIds;
    }
    public List<String> getHistoryIds() {
        return historyIds;
    }
    public Map<String, TopicHistory> getPlaylistHistory() {
        return playlistHistory;
    }
    public Map<String, ArtistHistory> getArtistHistory() {
        return artistHistory;
    }
    public Map<String, Map<String, String>> getGeneratedQuizHistory() { return generatedQuizHistory; }

    @Exclude
    public Map<String, Playlist> getPlaylists() {
        return playlists;
    }
    @Exclude
    public Map<String, Artist> getArtists() {
        return artists;
    }
    @Exclude
    public LinkedList<Track> getHistory() {
        return history;
    }
    @Exclude
    public Playlist getPlaylist(String playlistId) {
        for (Map.Entry<String, Playlist> playlist : playlists.entrySet()) {
            if (playlist.getValue().getId().equals(playlistId)) {
                return playlist.getValue();
            }
        }
        return null;
    }
    //#endregion

    //#region Mutators
    public void setLevel(int level) {
        this.level = level;
    }
    public void setXp(int xp) {
        this.xp = xp;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    public void setPlaylistHistory(Map<String, TopicHistory> playlistHistory) { this.playlistHistory = playlistHistory; }
    public void setArtistHistory(Map<String, ArtistHistory> artistHistory) { this.artistHistory = artistHistory; }
    public void setGeneratedQuizHistory(Map<String, Map<String, String>> generatedQuizHistory) { this.generatedQuizHistory = generatedQuizHistory; }

    public void setPlaylists(Map<String, Playlist> playlists) {
        this.playlists = playlists;
    }
    public void setArtists(Map<String, Artist> artists) {
        this.artists = artists;
    }
    public void setHistory(LinkedList<Track> history) {
        this.history = history;
    }

    public boolean addAlbumId(String key, String albumId) {
        if (albumIds.containsValue(albumId)) {
            return false;
        }

        albumIds.put(key, albumId);
        return true;
    }
    public boolean addArtistId(String key, String artistId) {
        if (artistIds.containsValue(artistId)) {
            return false;
        }
        artistIds.put(key, artistId);
        return true;
    }
    public boolean addPlaylistId(String key, String playlistId) {
        if (playlistIds.containsValue(playlistId)) {
            return false;
        }

        playlistIds.put(key, playlistId);
        return true;
    }

    public String removeAlbumId(String albumId) {
        String key = "";
        for (Map.Entry<String, String> entry : albumIds.entrySet()) {
            if (entry.getValue().equals(albumId)) {
                key = entry.getKey();
            }
        }

        albumIds.remove(key);
        return key;
    }
    public String removeArtistId(String artistId) {
        String key = "";
        for (Map.Entry<String, String> entry : artistIds.entrySet()) {
            if (entry.getValue().equals(artistId)) {
                key = entry.getKey();
            }
        }

        artistIds.remove(key);
        return key;
    }
    public String removePlaylistId(String playlistId) {
        String key = "";
        for (Map.Entry<String, String> entry : playlistIds.entrySet()) {
            if (entry.getValue().equals(playlistId)) {
                key = entry.getKey();
            }
        }

        playlistIds.remove(key);
        return key;
    }
    //#endregion

    public void updateHistoryIds(DatabaseReference db, String uId, List<Track> tracks) {
        LinkedList<String> historyIds = new LinkedList<>();
        for (String id : this.historyIds) {
            historyIds.add(id);
        }

        for (int i = 0; i < tracks.size(); i++) {
            if (history.size() == HISTORY_LIMIT) {
                history.removeFirst();
                historyIds.removeFirst();
            }
            history.addLast(tracks.get(i));
            historyIds.addLast(tracks.get(i).getId());
        }

        this.historyIds = new ArrayList<>();
        for (String id : historyIds) {
            this.historyIds.add(id);
        }
        db.child("users").child(uId).child("historyIds").setValue(historyIds);
    }

    public void updatePlaylistHistory(DatabaseReference db, String uId, String topicId, List<Track> tracks, int poolCount) {
        DatabaseReference playlistHistoryRef = db.child("users").child(uId).child("playlistHistory").child(topicId);

        // If there is no playlist history at all
        if (playlistHistory == null) {
            playlistHistory = new HashMap<>();
        }

        boolean newEntry = false;
        // If there is no Topic History
        if (playlistHistory.get(topicId) == null) {
            playlistHistory.put(topicId, new TopicHistory());
            playlistHistory.get(topicId).setTotal(poolCount);
            playlistHistory.get(topicId).setCount(tracks.size());
            newEntry = true;
        }

        // Convert the list to a map
        Map<String, String> tracksMap = new HashMap<>();

        for (int i = 0; i < tracks.size(); i++) {
            String key = playlistHistoryRef.push().getKey();

            boolean trackIdAdded = playlistHistory.get(topicId).addTrackId(key, tracks.get(i).getId());
            if (trackIdAdded) {
                tracksMap.put(key, tracks.get(i).getId());
                playlistHistory.get(topicId).incrementCount();
            }
        }

        // If there is no playlist history for the current topic
        if (newEntry) {
            // Set the value since it's new
            playlistHistoryRef.setValue(new TopicHistory(tracksMap, poolCount, tracks.size()));
        }
        else {
            for (Map.Entry<String, String> entry : tracksMap.entrySet()) {
                playlistHistoryRef.child("trackIds").child(entry.getKey()).setValue(entry.getValue());
            }
            playlistHistoryRef.child("count").setValue(playlistHistory.get(topicId).getCount());
        }
    }

    public void updateArtistHistory(DatabaseReference db, String uId, Artist artist, List<Track> tracks, int poolCount) {
        DatabaseReference artistHistoryRef = db.child("users").child(uId).child("artistHistory").child(artist.getId());

        // If there is no artist history at all
        if (artistHistory == null) {
            artistHistory = new HashMap<>();
        }

        // Convert the list to an albums map
        Map<String, TopicHistory> albumsMap = new HashMap<>();

        // Create album keys and default values
        for (Track track : tracks) {
            if (!albumsMap.containsKey(track.getAlbumId())) {
                albumsMap.put(track.getAlbumId(), new TopicHistory());
                albumsMap.get(track.getAlbumId()).setTotal(artist.getAlbumTrackCount(track.getAlbumId()));
                albumsMap.get(track.getAlbumId()).setCount(0);
            }
        }

        // Add track ids to each album
        for (int i = 0; i < tracks.size(); i++) {
            String key = artistHistoryRef.child(tracks.get(i).getAlbumId()).push().getKey();
            boolean trackIdAdded = artistHistory.get(artist.getId()).addTrackId(key, tracks.get(i));
            if (trackIdAdded) {
                albumsMap.get(tracks.get(i).getAlbumId()).getTrackIds().put(key, tracks.get(i).getId());
            }
        }

        // If there is no artist history for the current topic
        if (playlistHistory.get(artist.getId()) == null) {
            // Set the value since it's new
            artistHistoryRef.setValue(new ArtistHistory(albumsMap, poolCount, tracks.size()));
        }
        else {
            for (Map.Entry<String, TopicHistory> entry : albumsMap.entrySet()) {
                artistHistoryRef.child("albums").child(entry.getKey()).setValue(entry.getValue());
            }
        }
    }

//    public void updateQuizHistory(DatabaseReference db, String uId, String topicId, List<Track> tracks, int poolCount) {
//        if (quizHistory == null) {
//            quizHistory = new HashMap<>();
//            quizHistory.put(topicId, new TopicHistory(tracks));
//        }
//        DatabaseReference quizHistoryRef = db.child("users").child(uId).child("quizHistory").child(topicId);
//        for (int i = 0; i < tracks.size(); i++) {
//            String key = quizHistoryRef.push().getKey();
//            boolean trackIdAdded = quizHistory.get(topicId).addTrackId(key, tracks.get(i).getId());
//            if (trackIdAdded) {
//                quizHistory.get(topicId).incrementCount();
//                quizHistoryRef.child(key).setValue(new TopicHistory(tracks));
//            }
//        }
//    }
//
//    // Creates a new Quiz History of Topic Histories
//    private void createQuizHistory(DatabaseReference db, String uId, String topicId, List<Track> tracks, int poolCount) {
//        quizHistory = new HashMap<>();
//        quizHistory.put(topicId, new TopicHistory(tracks, poolCount, ));
//    }

    public void updateGeneratedQuizHistory(DatabaseReference db, String uId, String topicId, String quizId) {
        DatabaseReference generatedQuizHistoryRef = db.child("users").child(uId)
                .child("generatedQuizHistory");

        if (generatedQuizHistory == null || generatedQuizHistory.isEmpty()) {
            generatedQuizHistory = new HashMap<>();
            generatedQuizHistory.put(topicId, new HashMap<>());
        }

        String key = generatedQuizHistoryRef.child(topicId).push().getKey();
        generatedQuizHistory.get(topicId).put(key, quizId);
        generatedQuizHistoryRef.child(topicId).child(key).setValue(quizId);
    }

    //#region Collections initialization
    public void initCollections(DatabaseReference db) {
        initArtists(db);
        initPlaylists(db);
        initHistory(db);
    }

    private void initArtists(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initArtists");
        artists = new HashMap<>();
        for (Map.Entry<String, String> entry : artistIds.entrySet()) {
            artists.put(entry.getKey(), FirebaseService.checkDatabase(db, "artists", entry.getValue(), Artist.class));
            artists.get(entry.getKey()).initCollections(db, this);
        }
        log.i("Artists retrieved.");
    }
    private void initPlaylists(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initPlaylists");
        playlists = new HashMap<>();
        for (Map.Entry<String, String> entry : playlistIds.entrySet()) {
            playlists.put(entry.getKey(), FirebaseService.checkDatabase(db, "playlists", entry.getValue(), Playlist.class));
        }
        log.i("Playlists retrieved.");
    }
    private void initHistory(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initPlaylists");
        history = new LinkedList<>();
        for (String trackId : historyIds) {
            history.add(FirebaseService.checkDatabase(db, "tracks", trackId, Track.class));
        }
        if (history.size() > 0) {
            log.i("History retrieved.");
        }
        else {
            log.i("No history to retrieve.");
        }
    }
    //#endregion


}
