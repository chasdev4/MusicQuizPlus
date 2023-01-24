package model;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import service.FirebaseService;

public class User implements Serializable {
    private Map<String, String> albumIds;
    private Map<String, String> artistIds;
    private Map<String, String> playlistIds;
    private Map<String, String> historyIds;
    private Map<String, QuizHistory> quizHistory;
    private int level;
    private int xp;
    private Difficulty difficulty;

    // Excluded from Database
    private Map<String, Artist> artists;    // Albums are saved to the artist, tracks are saved to albums
    private Map<String, Playlist> playlists;
    private Map<String, Track> history;

    private final static String TAG = "User.java";

    public User() {
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        playlistIds = new HashMap<>();
        historyIds = new HashMap<>();
        level = 1;
        xp = 0;
        difficulty = Difficulty.EASY;
    }

    public Map<String, String> getAlbumIds() {
        return albumIds;
    }

    public boolean addAlbumId(String key, String albumId) {
        if (albumIds.containsValue(albumId)) {
            return false;
        }

        albumIds.put(key, albumId);
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

    public Map<String, String> getArtistIds() {
        return artistIds;
    }

    public boolean addArtistId(String key, String artistId) {
        if (artistIds.containsValue(artistId)) {
            return false;
        }
        artistIds.put(key, artistId);
        return true;
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

    public Map<String, String> getPlaylistIds() {
        return playlistIds;
    }

    public boolean addPlaylistId(String key, String playlistId) {
        if (playlistIds.containsValue(playlistId)) {
            return false;
        }

        playlistIds.put(key, playlistId);
        return true;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Map<String, String> getHistoryIds() {
        return historyIds;
    }

    public boolean addToHistory(String key, String trackId) {
        if (historyIds.containsValue(trackId)) {
            return false;
        }

        historyIds.put(key, trackId);
        return true;
    }

    public String removeFromHistory(String trackId) {
        String key = "";
        for (Map.Entry<String, String> entry : historyIds.entrySet()) {
            if (entry.getValue().equals(trackId)) {
                key = entry.getKey();
            }
        }

        historyIds.remove(key);
        return key;
    }

    public Map<String, QuizHistory> getQuizHistory() {
        return quizHistory;
    }

    public void setQuizHistory(Map<String, QuizHistory> quizHistory) {
        this.quizHistory = quizHistory;
    }

    @Exclude
    public Map<String, Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Map<String, Playlist> playlists) {
        this.playlists = playlists;
    }

    public Playlist getPlaylist(String playlistId) {
        for (Map.Entry<String, Playlist> playlist : playlists.entrySet()) {
            if (playlist.getValue().getId().equals(playlistId)) {
                return playlist.getValue();
            }
        }
        return null;
    }

    @Exclude
    public Map<String, Artist> getArtists() {
        return artists;
    }

    public void setArtists(Map<String, Artist> artists) {
        this.artists = artists;
    }

    public Artist getArtist(String artistId) {
        for (Map.Entry<String, Artist> artist : artists.entrySet()) {
            if (artist.getValue().getId().equals(artistId)) {
                return artist.getValue();
            }
        }
        return null;
    }

    @Exclude
    public Map<String, Track> getHistory() {
        return history;
    }

    public void setHistory(Map<String, Track> history) {
        this.history = history;
    }

    public Track getHistoryItem(String trackId) {
        for (Map.Entry<String, Track> track : history.entrySet()) {
            if (track.getValue().getId().equals(trackId)) {
                return track.getValue();
            }
        }
        return null;
    }

    public void initCollections(DatabaseReference db) {
        initArtists(db);
        initPlaylists(db);
        initHistory(db);
    }

    private void initArtists(DatabaseReference db) {
        artists = new HashMap<>();
        for (Map.Entry<String, String> entry : artistIds.entrySet()) {
            artists.put(entry.getKey(), FirebaseService.checkDatabase(db, "artists", entry.getValue(), Artist.class));
        }
        Log.i(TAG, "Artists retrieved.");
    }
    private void initPlaylists(DatabaseReference db) {
        playlists = new HashMap<>();
        for (Map.Entry<String, String> entry : playlistIds.entrySet()) {
            playlists.put(entry.getKey(), FirebaseService.checkDatabase(db, "playlists", entry.getValue(), Playlist.class));
        }
        Log.i(TAG, "Playlists retrieved.");
    }
    private void initHistory(DatabaseReference db) {
        history = new HashMap<>();
        for (Map.Entry<String, String> entry : historyIds.entrySet()) {
            history.put(entry.getKey(), FirebaseService.checkDatabase(db, "tracks", entry.getValue(), Track.class));
        }
        Log.i(TAG, "History retrieved.");
    }


}
