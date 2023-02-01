package model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private Map<String, QuizHistory> quizHistory;                     // Map<topicId, QuizHistory>
    private Map<String, GeneratedQuizHistory> generatedQuizHistory;   // Map<topicId, GeneratedQuizHistory>

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
    public Map<String, QuizHistory> getQuizHistory() {
        return quizHistory;
    }
    public Map<String, GeneratedQuizHistory> getGeneratedQuizHistory() { return generatedQuizHistory; }

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
    public void setQuizHistory(Map<String, QuizHistory> quizHistory) { this.quizHistory = quizHistory; }
    public void setGeneratedQuizHistory(Map<String, GeneratedQuizHistory> generatedQuizHistory) { this.generatedQuizHistory = generatedQuizHistory; }

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

    public void updateHistoryIds(List<Track> tracks) {
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
    }
    public void updateQuizHistory(DatabaseReference db, FirebaseUser firebaseUser, String topicId, List<Track> history) {
        if (quizHistory == null) {
            quizHistory = new HashMap<>();
            quizHistory.put(topicId, new QuizHistory());
        }
        DatabaseReference quizHistoryRef = db.child("users").child(firebaseUser.getUid()).child("quizHistory").child(topicId);
        for (int i = 0; i < history.size(); i++) {
            String key = quizHistoryRef.push().getKey();
            boolean trackIdAdded = quizHistory.get(topicId).addTrackId(key, history.get(i).getId());
            if (trackIdAdded) {
                quizHistory.get(topicId).incrementCount();
                quizHistoryRef.child(key).setValue(new QuizHistory(history));
            }
        }
    }

    public void updateGeneratedQuizHistory(DatabaseReference db, FirebaseUser firebaseUser, String topicId, String quizId) {
        if (generatedQuizHistory == null || generatedQuizHistory.isEmpty()) {
            generatedQuizHistory = new HashMap<>();
            generatedQuizHistory.put(topicId, new GeneratedQuizHistory(new HashMap<>()));
        }
        DatabaseReference generatedQuizHistoryRef = db.child("users").child(firebaseUser.getUid())
                .child("generatedQuizHistory");
        String key = generatedQuizHistoryRef.child(topicId).push().getKey();
        if (generatedQuizHistory.get(topicId).isQuizIdsNull()) {
            generatedQuizHistory.get(topicId).setQuizIds(new HashMap<>());
        }
        generatedQuizHistory.get(topicId).getQuizIds().put(key, quizId);
        generatedQuizHistoryRef.child(topicId).child(key).setValue(generatedQuizHistory.get(topicId));
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
