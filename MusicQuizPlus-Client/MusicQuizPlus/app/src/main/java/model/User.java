package model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.concurrent.CountDownLatch;

import model.history.ArtistHistory;
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
    private String name;
    private String photoUrl;
    private int level;
    private int xp;
    private Settings settings;
    private Map<String, String> albumIds;
    private Map<String, String> artistIds;
    private Map<String, String> playlistIds;
    private List<String> historyIds;
    private Map<String, String> badgeIds;
    private Map<String, TopicHistory> playlistHistory;
    private Map<String, ArtistHistory> artistHistory;
    private Map<String, Map<String, String>> generatedQuizHistory;
    private int playlistQuizCount;
    private int artistQuizCount;

    //#region Excluded members
    private Map<String, Playlist> playlists;
    private Map<String, Artist> artists;
    private LinkedList<Track> history;

    private double xpToNextLevel;
    //#endregion

    private boolean completedCollection = false;
    private List<String> completedCollectionIDs;
    private int artistTrackCount;
    private boolean allSongsKnown;

    //#region Constants
    private final static String TAG = "User.java";
    private final static int HISTORY_LIMIT = 50;
    private final static int MIN_LEVEL = 1;
    private final static int MAX_LEVEL = 100;
    private final static Map<Integer, Integer> LEVELS = new HashMap<>();
    //#endregion

    public User() {
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        playlistIds = new HashMap<>();
        historyIds = new ArrayList<>();
        badgeIds = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        level = MIN_LEVEL;
        xp = 0;
        settings = new Settings();
        initLevels();
    }

    public User(FirebaseUser firebaseUser, Settings settings, Map<String, String> playlistIds) {
        name = firebaseUser.getDisplayName();
        photoUrl = firebaseUser.getPhotoUrl().toString();
        level = MIN_LEVEL;
        xp = 0;
        this.settings = settings;
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        this.playlistIds = playlistIds;
        historyIds = new ArrayList<>();
        badgeIds = new HashMap<>();
        playlistHistory = new HashMap<>();
        artistHistory = new HashMap<>();
        generatedQuizHistory = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        initLevels();
    }

    public User(User user) {
        name = user.name;
        photoUrl = user.photoUrl;
        albumIds = user.albumIds;
        artistIds = user.artistIds;
        playlistIds = user.playlistIds;
        historyIds = user.historyIds;
        badgeIds = user.badgeIds;
        playlistQuizCount = user.playlistQuizCount;
        artistQuizCount = user.artistQuizCount;
        level = user.level;
        xp = user.xp;
        settings = user.settings;
        initLevels();
    }

    public User(int difficulty, Map<String, String> playlistIds) {
        name = "";
        level = MIN_LEVEL;
        xp = 0;
        settings.setDifficulty(Difficulty.values()[difficulty]);
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        this.playlistIds = playlistIds;
        historyIds = new ArrayList<>();
        badgeIds = new HashMap<>();
        playlistHistory = new HashMap<>();
        artistHistory = new HashMap<>();
        generatedQuizHistory = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        initLevels();
    }

    //#region Accessors
    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public Settings getSettings() {
        return settings;
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

    public Map<String, String> getBadgeIds() {
        return badgeIds;
    }

    public Map<String, TopicHistory> getPlaylistHistory() {
        return playlistHistory;
    }

    public Map<String, ArtistHistory> getArtistHistory() {
        return artistHistory;
    }

    public Map<String, Map<String, String>> getGeneratedQuizHistory() {
        return generatedQuizHistory;
    }

    public int getArtistQuizCount() {
        return artistQuizCount;
    }

    public int getPlaylistQuizCount() {
        return playlistQuizCount;
    }

    @Exclude
    public Difficulty getDifficulty() {
        return settings.getDifficulty();
    }

    @Exclude
    public boolean getAllSongsKnown() {return allSongsKnown;}

    @Exclude
    public int getArtistTrackCount() { return artistTrackCount; }

    @Exclude
    public List<String> getCompletedCollectionIDs() { return completedCollectionIDs; }

    @Exclude
    public Map<String, Playlist> getPlaylists() {
        return playlists;
    }
    @Exclude
    public List<Playlist> getPlaylistsAsList() {
        List<Playlist> lists = new ArrayList<>();
        for (Map.Entry<String, Playlist> entry : playlists.entrySet()) {
            lists.add(entry.getValue());
        }
        return lists;
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

    @Exclude
    public Artist getArtist(String artistId) {
        for (Map.Entry<String, Artist> artist : artists.entrySet()) {
            if (artist.getValue().getId().equals(artistId)) {
                return artist.getValue();
            }
        }
        return null;
    }

    @Exclude
    public double getXpToNextLevel() {
        return xpToNextLevel;
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
        settings.setDifficulty(difficulty);
    }

    public void setPlaylistHistory(Map<String, TopicHistory> playlistHistory) {
        this.playlistHistory = playlistHistory;
    }

    public void setArtistHistory(Map<String, ArtistHistory> artistHistory) {
        this.artistHistory = artistHistory;
    }

    public void setGeneratedQuizHistory(Map<String, Map<String, String>> generatedQuizHistory) {
        this.generatedQuizHistory = generatedQuizHistory;
    }

    public void setPlaylistIds(Map<String, String> playlistIds) {
        this.playlistIds = playlistIds;
    }

    public void setArtistIds(Map<String, String> artistIds) {
        this.artistIds = artistIds;
    }

    public void setHistoryIds(List<String> historyIds) {
        this.historyIds = historyIds;
    }

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

    public String removeBadgeId(String badgeId) {
        String key = "";
        for (Map.Entry<String, String> entry : badgeIds.entrySet()) {
            if (entry.getValue().equals(badgeId)) {
                key = entry.getKey();
            }
        }

        badgeIds.remove(key);
        return key;
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

    public boolean addBadgeId(String key, String badgeId, boolean allowDuplicates) {
        if (allowDuplicates) {
            badgeIds.put(key, badgeId);
            return true;
        }

        if (!badgeIds.containsValue(badgeId)) {
            badgeIds.put(key, badgeId);
            return true;
        }

        return false;
    }

    //USED FOR DEBUGGING
    public void setArtistQuizCount(int artistQuizCount) {
        this.artistQuizCount = artistQuizCount;
    }

    //USED FOR DEBUGGING
    public void setPlaylistQuizCount(int playlistQuizCount) {
        this.playlistQuizCount = playlistQuizCount;
    }

    public void incrementArtistQuizCount() {
        artistQuizCount++;
    }

    public void incrementPlaylistQuizCount() {
        playlistQuizCount++;
    }

    private void initLevels() {
        int xp = 0;
        for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
            xp += (int) doEquation(i);
            LEVELS.put(i, xp);
        }
    }
    //#endregion

    //#region CRUD
    public boolean delete(FirebaseUser firebaseUser, DatabaseReference db) {
        if (firebaseUser == null) {
            return false;
        }
        return deleteUser(firebaseUser, db);
    }

    private boolean deleteUser(FirebaseUser firebaseUser, DatabaseReference db) {
        final boolean[] result = {false};

        CountDownLatch countDownLatch = new CountDownLatch(1);
        db.child("users").child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    result[0] = true;
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }
    //#endregion

    //#region Update History
    public void updateHistoryIds(DatabaseReference db, String uId, List<Track> tracks) {
        LinkedList<String> historyIds = new LinkedList<>();
        for (String id : this.historyIds) {
            historyIds.add(id);
        }

        for (int i = 0; i < tracks.size(); i++) {
            if (this.historyIds.contains(tracks.get(i).getId())) {
                history.remove(tracks.get(i));
                historyIds.remove(tracks.get(i).getId());
            }
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


    public boolean updatePlaylistHistory(DatabaseReference db, String uId, Playlist playlist, List<Track> tracks, int poolCount) {
        String topicId = playlist.getId();
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
            newEntry = true;
        }

        // If the user already knows the playlist
        if (playlistHistory.get(topicId).getCount() == playlistHistory.get(topicId).getTotal()) {
            return;
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
        } else {
            for (Map.Entry<String, String> entry : tracksMap.entrySet()) {
                playlistHistoryRef.child("trackIds").child(entry.getKey()).setValue(entry.getValue());
            }
            playlistHistoryRef.child("count").setValue(playlistHistory.get(topicId).getCount());
        }

        if (playlistHistory.get(topicId).getCount() == playlistHistory.get(topicId).getTotal()) {
            playlistHistoryRef.child("trackIds").removeValue();
            completedCollection = true;
        }

        return completedCollection;
    }

    public boolean updateArtistHistory(DatabaseReference db, String uId, Artist artist, List<Track> tracks, int poolCount) {
        DatabaseReference artistHistoryRef = db.child("users").child(uId).child("artistHistory").child(artist.getId());

        // If there is no artist history at all
        if (artistHistory == null || artistHistory.size() == 0) {
            artistHistory = new HashMap<>();
        }

        boolean newEntry = false;
        // If there is no Topic History
        if (artistHistory.get(artist.getId()) == null) {
            artistHistory.put(artist.getId(), new ArtistHistory());
            artistHistory.get(artist.getId()).setAlbums(new HashMap<>());
            artistHistory.get(artist.getId()).setAlbumsTotal(artist.getAlbumIds().size() + artist.getSingleIds().size() + artist.getCompilationIds().size());
            artistHistory.get(artist.getId()).setAlbumsCount(0);
            newEntry = true;
        }

        //If the user already knows all albums
        if (artistHistory.get(artist.getId()).getAlbumsCount() == artistHistory.get(artist.getId()).getAlbumsTotal()) {
            return;
        }

        // Convert the list to an albums map
        Map<String, TopicHistory> albumsMap = new HashMap<>();

        // Create album keys and default values
        for (Track track : tracks) {
            if (!albumsMap.containsKey(track.getAlbumId())) {
                albumsMap.put(track.getAlbumId(), new TopicHistory());
                albumsMap.get(track.getAlbumId()).setTrackIds(new HashMap<>());
                albumsMap.get(track.getAlbumId()).setTotal(artist.getAlbumTrackCount(track.getAlbumId()));
                albumsMap.get(track.getAlbumId()).setCount(0);
            }
        }

        // Add track ids to each album
        for (int i = 0; i < tracks.size(); i++) {
            String key = artistHistoryRef.child(tracks.get(i).getAlbumId()).push().getKey();
            boolean trackIdAdded = artistHistory.get(artist.getId()).addTrackId(key, tracks.get(i), albumsMap.get(tracks.get(i).getAlbumId()).getTotal());
            if (trackIdAdded) {
                albumsMap.get(tracks.get(i).getAlbumId()).getTrackIds().put(key, tracks.get(i).getId());
                albumsMap.get(tracks.get(i).getAlbumId()).incrementCount();
                if (albumsMap.get(tracks.get(i).getAlbumId()).getTotal() <= 0) {
                    albumsMap.get(tracks.get(i).getAlbumId()).setTotal(artist.getAlbum(tracks.get(i).getAlbumId()).getTrackIds().size());
                }
            }
        }

        // If there is no artist history for the current topic
        if (newEntry) {
            artistHistory.get(artist.getId()).setAlbums(albumsMap);
            artistHistoryRef.setValue(artistHistory.get(artist.getId()));
        }
        for (Map.Entry<String, TopicHistory> albumsMapEntry : albumsMap.entrySet()) {
            if (newEntry) {
                // Set the value since it's new
                if (albumsMapEntry.getValue().getCount() == albumsMapEntry.getValue().getTotal()) {
                    artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("count").setValue(albumsMapEntry.getValue().getCount());
                    artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("total").setValue(albumsMapEntry.getValue().getTotal());
                    artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("trackIds").removeValue();
                    artistHistoryRef.child("albumsCount").setValue(ServerValue.increment(1));
                    completedCollection = true;
                    completedCollectionIDs.add(albumsMapEntry.getKey());
                    artistTrackCount += albumsMapEntry.getValue().getCount();

                }

            } else if (artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getTotal()
                    != artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getCount()) {
                int count = albumsMapEntry.getValue().getCount()
                        + artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getCount();

                artistTrackCount += count;
                if (albumsMapEntry.getValue().getTotal() > count) {

                    for (Map.Entry<String, String> trackId : albumsMapEntry.getValue().getTrackIds().entrySet()) {
                        artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("trackIds")
                                .child(trackId.getKey()).setValue(trackId.getValue());
                    }
                    artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).setCount(count);
                    artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("count").setValue(count);
                    artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("total").setValue(albumsMapEntry.getValue().getTotal());
                    if (count == albumsMapEntry.getValue().getTotal()) {
                        artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("count").setValue(count);
                        artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("total").setValue(albumsMapEntry.getValue().getTotal());
                        artistHistoryRef.child("albums").child(albumsMapEntry.getKey()).child("trackIds").removeValue();
                        artistHistoryRef.child("albumsCount").setValue(ServerValue.increment(1));
                        completedCollection = true;
                        completedCollectionIDs.add(albumsMapEntry.getKey());
                    }

            }

        }

        return completedCollection;
    }

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
    //#endregion

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
        } else {
            log.i("No history to retrieve.");
        }
    }

    public void initArtistTrackCount()
    {
        for( Map.Entry<String, ArtistHistory> item : artistHistory.entrySet() )
        {
            if(item.getValue().getAlbumsTotal() == item.getValue().getAlbumsCount())
            {
                allSongsKnown = true;
            }

            for ( Map.Entry<String, TopicHistory> a :  item.getValue().getAlbums().entrySet())
            {
                artistTrackCount += a.getValue().getCount();
            }
        }
    }
    //#endregion

    //#region Progression
    public void addXP(DatabaseReference db, FirebaseUser firebaseUser, int xp) {
        if (xp <= 0) {
            return;
        }
        if (this.xp == LEVELS.get(MAX_LEVEL) && level == MAX_LEVEL) {
            return;
        }
        if (this.xp + xp >= LEVELS.get(MAX_LEVEL)) {
            this.xp = LEVELS.get(MAX_LEVEL);
        } else {
            this.xp += xp;
        }
        updateLevelAndDb(db, firebaseUser);
    }

    private void updateLevelAndDb(DatabaseReference db, FirebaseUser firebaseUser) {
        Map<String, Object> updates = new HashMap<>();
        if (xp >= LEVELS.get(level + 1)) {
            // Determine how many levels (could be more than one early game)
            int i = 1;
            while (i < LEVELS.size() && xp >= LEVELS.get(i + 1)) {
                i++;
            }
            level = i;

            updates.put("users/" + firebaseUser.getUid() + "/level/", level);
        }
        updates.put("users/" + firebaseUser.getUid() + "/xp", xp);
        db.updateChildren(updates);
    }

    private double doEquation(int level) {
        if (level == 0) {
            level = this.level;
        }
        return Math.log(level) * 2500;
    }
    //#endregion
}
