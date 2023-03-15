package model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.musicquizplus.R;
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
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.AlbumType;
import model.type.BadgeType;
import model.type.Difficulty;
import model.type.Role;
import service.BadgeService;
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
    private List<HistoryEntry> historyIds;
    private Map<String, Badge> badges;
    private Map<String, TopicHistory> playlistHistory;
    private Map<String, ArtistHistory> artistHistory;
    private Map<String, Map<String, String>> generatedQuizHistory;
    private int playlistQuizCount;
    private int artistQuizCount;
    private int searchCount;

    //#region Excluded members
    private Map<String, Playlist> playlists;
    private Map<String, Artist> artists;
    private LinkedList<Track> history;
    private List<Badge> earnedBadges;
    //#endregion

    //#region Constants
    private final static String TAG = "User.java";
    private final static int HISTORY_LIMIT = 50;
    private final static int MIN_LEVEL = 1;
    private final static int MAX_LEVEL = 100;
    private final static long ONE_DAY = 24*60*60*1000;
    private final static Map<Role, Integer> SEARCH_LIMITS = new HashMap<>() {
        {
            put(Role.GUEST, 3); // 3 searches total
            put(Role.USER, 5);  // 5 a day
        }
    };
    private final static Map<Integer, Integer> LEVELS = new HashMap<>();
    //#endregion

    public User() {
        albumIds = new HashMap<>();
        artistIds = new HashMap<>();
        playlistIds = new HashMap<>();
        historyIds = new ArrayList<>();
        artists = new HashMap<>();
        playlists = new HashMap<>();
        history = new LinkedList<>();
        badges = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        searchCount = 0;
        level = MIN_LEVEL;
        xp = 0;
        settings = new Settings();
        earnedBadges = new ArrayList<>();
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
        history = new LinkedList<>();
        artists = new HashMap<>();
        playlists = new HashMap<>();
        badges = new HashMap<>();
        playlistHistory = new HashMap<>();
        artistHistory = new HashMap<>();
        generatedQuizHistory = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        searchCount = 0;
        earnedBadges = new ArrayList<>();
        initLevels();
    }

    public User(User user) {
        name = user.name;
        photoUrl = user.photoUrl;
        albumIds = user.albumIds;
        artistIds = user.artistIds;
        playlistIds = user.playlistIds;
        historyIds = user.historyIds;
        badges = user.badges;
        playlistQuizCount = user.playlistQuizCount;
        artistQuizCount = user.artistQuizCount;
        searchCount = user.searchCount;
        level = user.level;
        xp = user.xp;
        settings = user.settings;
        earnedBadges = new ArrayList<>();
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
        badges = new HashMap<>();
        playlistHistory = new HashMap<>();
        artistHistory = new HashMap<>();
        generatedQuizHistory = new HashMap<>();
        playlistQuizCount = 0;
        artistQuizCount = 0;
        searchCount = 0;
        earnedBadges = new ArrayList<>();
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

    public List<HistoryEntry> getHistoryIds() {
        return historyIds;
    }

    public Map<String, Badge> getBadges() {
        return badges;
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
    public Map<Integer, Integer> getLevels() { return LEVELS; }

    @Exclude
    public int getSearchCount() { return searchCount; }

    @Exclude
    private int getSearchLimit(Role role) { return SEARCH_LIMITS.get(role); }

    @Exclude
    public Difficulty getDifficulty() {
        return settings.getDifficulty();
    }

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
        if (level == 99) {
            return LEVELS.get(100);
        }
        for (int i = level; i < LEVELS.size() - level; i++) {
            if (xp > LEVELS.get(level)) {
                return LEVELS.get(level);
            }
        }
        return -1;

    }
    @Exclude
    public double getXpFromPreviousLevel() {
        if (level <= 1) {
            return LEVELS.get(1);
        }
        return LEVELS.get(level-1);
    }

    @Exclude
    public List<Badge> getEarnedBadges() {
        return earnedBadges;
    }
    @Exclude
    public List<Badge> getBadgesAsList() {
        List<Badge> data = new ArrayList<>();
        for (Map.Entry<String, Badge> badge : badges.entrySet()) {
            data.add(badge.getValue());
        }
        return data;
    }
    @Exclude
    public List<Artist> getArtistsAsList() {
        List<Artist> data = new ArrayList<>();
        for (Map.Entry<String, Artist> a : artists.entrySet()) {
            data.add(a.getValue());
        }
        return data;
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

    public void setHistoryIds(List<HistoryEntry> historyIds) {
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

    public void resetEarnedBadges() {
        earnedBadges = new ArrayList<>();
    }

    public void incrementSearchCount() { searchCount++; }

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

    public void setSearchCount(int count) {
        searchCount =count;
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
    public void updateHistoryIds(DatabaseReference db, String uId, List<Track> tracks, String sourceId) {
        LinkedList<String> historyIds = new LinkedList<>();

        for (HistoryEntry id : this.historyIds) {
            if(historyIds.size() == 50)
            {
                break;
            }
            historyIds.addLast(id.getId());
        }

        for (int i = 0; i < tracks.size(); i++) {
            if (this.historyIds.contains(tracks.get(i).getId())) {
                historyIds.remove(tracks.get(i).getId());
            }
            if (historyIds.size() == HISTORY_LIMIT) {
                historyIds.removeLast();
            }
            history.addFirst(tracks.get(i));
            historyIds.addFirst(tracks.get(i).getId());
        }

        this.historyIds = new ArrayList<>();
        for (String id : historyIds) {
            this.historyIds.add(new HistoryEntry(id, sourceId));
        }
        db.child("users").child(uId).child("historyIds").setValue(this.historyIds);
    }

    public void updatePlaylistHistory(DatabaseReference db, String uId, Playlist playlist, List<Track> tracks, int poolCount) {
        // Playlist quiz count and milestone badge logic
        playlistQuizCount++;
        BadgeType badgeType = null;
        if (playlistQuizCount <= 25) {
            switch (playlistQuizCount) {
                case 1:
                    badgeType = BadgeType.PLAYLIST_QUIZ_MILESTONE_1;
                    break;
                case 3:
                    badgeType = BadgeType.PLAYLIST_QUIZ_MILESTONE_3;
                    break;
                case 5:
                    badgeType = BadgeType.PLAYLIST_QUIZ_MILESTONE_5;
                    break;
                case 10:
                    badgeType = BadgeType.PLAYLIST_QUIZ_MILESTONE_10;
                    break;
                case 25:
                    badgeType = BadgeType.PLAYLIST_QUIZ_MILESTONE_25;
                    break;
            }
            if (badgeType != null) {
                earnedBadges.add(new Badge(badgeType));
            }
        } else if (playlistQuizCount % 50 == 0) {
            earnedBadges.add(new Badge(BadgeType.PLAYLIST_QUIZ_MILESTONE_50, playlistQuizCount));
        }

        db.child("users").child(uId).child("playlistQuizCount").setValue(playlistQuizCount);

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
            earnedBadges.add(new Badge(BadgeType.PLAYLIST_KNOWLEDGE, playlist.getName()));
        }
    }

    public void updateArtistHistory(DatabaseReference db, String uId, Artist artist, List<Track> tracks, int poolCount) {
        // Increment the artist quiz count at the very least
        artistQuizCount++;
        BadgeType badgeType = null;
        if (artistQuizCount <= 25) {
            switch (artistQuizCount) {
                case 1:
                    badgeType = BadgeType.ARTIST_QUIZ_MILESTONE_1;
                    break;
                case 3:
                    badgeType = BadgeType.ARTIST_QUIZ_MILESTONE_3;
                    break;
                case 5:
                    badgeType = BadgeType.ARTIST_QUIZ_MILESTONE_5;
                    break;
                case 10:
                    badgeType = BadgeType.ARTIST_QUIZ_MILESTONE_10;
                    break;
                case 25:
                    badgeType = BadgeType.ARTIST_QUIZ_MILESTONE_25;
                    break;
            }
            if (badgeType != null) {
                earnedBadges.add(new Badge(badgeType));
            }
        } else if (artistQuizCount % 50 == 0) {

            earnedBadges.add(new Badge(BadgeType.ARTIST_QUIZ_MILESTONE_50, artistQuizCount));
        }

        db.child("users").child(uId).child("artistQuizCount").setValue(artistQuizCount);

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
        int oldCount = artistHistory.get(artist.getId()).calculateTracksCount();

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
                    Album tempAlbum = artist.getAlbum(albumsMapEntry.getKey());
                    earnedBadges.add(new Badge(
                            (tempAlbum.getType() == AlbumType.ALBUM)
                                    ? BadgeType.STUDIO_ALBUM_KNOWLEDGE : BadgeType.OTHER_ALBUM_KNOWLEDGE,
                            albumsMapEntry.getKey(), tempAlbum.getName()));
                }

            } else if (artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getTotal()
                    != artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getCount()) {
                int count = albumsMapEntry.getValue().getCount()
                        + artistHistory.get(artist.getId()).getAlbums().get(albumsMapEntry.getKey()).getCount();
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
                        artistHistory.get(artist.getId()).incrementAlbumsCount();
                        Album tempAlbum = artist.getAlbum(albumsMapEntry.getKey());
                        earnedBadges.add(new Badge(
                                (tempAlbum.getType() == AlbumType.ALBUM) ? BadgeType.STUDIO_ALBUM_KNOWLEDGE : BadgeType.OTHER_ALBUM_KNOWLEDGE,
                                albumsMapEntry.getKey(), tempAlbum.getName()));
                    }

            }
        }
        int newCount = artistHistory.get(artist.getId()).calculateTracksCount();
        if (oldCount < newCount) {

            if (oldCount < 10) {
                if (oldCount < 3 && newCount >= 3) {
                    earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_1, artist.getId(), artist.getName(), 3));
                }
                if (oldCount < 3 && newCount >= 5
                        || (oldCount > 3 && oldCount < 5) && newCount >= 5) {
                    earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_1, artist.getId(), artist.getName(), 5));
                }
                if (oldCount < 5 && newCount >= 10
                        || (oldCount > 3 && oldCount < 5) && newCount >= 10
                        || (oldCount > 5 && oldCount < 10) && newCount >= 10) {
                    earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_1, artist.getId(), artist.getName(), 10));
                }
            }
            else if (oldCount < 25 && newCount >= 25) {
                earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_2, artist.getId(), artist.getName(), 25));
            }
            else if (oldCount < 50 && newCount >= 50) {
                earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_2, artist.getId(), artist.getName(), 50));
            }
            else if (oldCount > 50) {
                for (int i = oldCount; i < newCount; i++) {
                    if (i % 50 == 0) {
                        earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_3, artist.getId(), artist.getName(), i));

                    }
                }

            }

            if (artistHistory.get(artist.getId()).getAlbumsCount() == artistHistory.get(artist.getId()).getAlbumsTotal()) {
                earnedBadges.add(new Badge(BadgeType.ARTIST_KNOWLEDGE_4, artist.getId(), artist.getName()));
            }
        }
    }

    public void updateGeneratedQuizHistory(DatabaseReference db, String uId, String topicId, String quizId) {
        DatabaseReference generatedQuizHistoryRef = db.child("users").child(uId)
                .child("generatedQuizHistory");

        if (generatedQuizHistory == null || generatedQuizHistory.isEmpty()) {
            generatedQuizHistory = new HashMap<>();
            generatedQuizHistory.put(topicId, new HashMap<>());
        }

        String key = generatedQuizHistoryRef.child(topicId).push().getKey();
        if(generatedQuizHistory.get(topicId) == null)
        {
            generatedQuizHistory.put(topicId, new HashMap<>());
        }
        generatedQuizHistory.get(topicId).put(key, quizId);
        generatedQuizHistoryRef.child(topicId).child(key).setValue(quizId);
    }
    //#endregion

    //#region Initialization
    public void initGuest(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        setDifficulty(Difficulty.values()[sharedPref.getInt(activity.getString(R.string.difficulty),
                0)]);
        searchCount = sharedPref.getInt(activity.getString(R.string.searchCount), 0);


        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(activity.getString(R.string.searchCount), searchCount);
        editor.apply();
    }
    private void initLevels() {
        int xp = 0;
        for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
            xp += (int) doEquation(i);
            LEVELS.put(i, xp);
        }
    }

//    public void initCollections(DatabaseReference db, FirebaseUser firebaseUser) {
//        initArtists(db, firebaseUser, true);
//        initPlaylists(db);
//        initHistory(db);
//    }

    public void initArtists(DatabaseReference db, FirebaseUser firebaseUser, boolean initCollections) {
        LogUtil log = new LogUtil(TAG, "initArtists");
        artists = new HashMap<>();
        for (Map.Entry<String, String> entry : artistIds.entrySet()) {
            Artist artist = FirebaseService.checkDatabase(db, "artists", entry.getValue(), Artist.class);
            if (artist!=null) {
                artists.put(entry.getKey(), FirebaseService.checkDatabase(db, "artists", entry.getValue(), Artist.class));
                if (initCollections) {
                    artists.get(entry.getKey()).initCollections(db, this);
                }
            }
            else {
                log.w(String.format("%s doesn't exist in database. Removing from id list...",entry.getValue()));
                artistIds.remove(entry.getKey());
                db.child("users").child(firebaseUser.getUid()).child("artistIds").child(entry.getKey()).removeValue();
            }
        }
        log.i("Artists retrieved.");
    }

    public void initPlaylists(DatabaseReference db, DatabaseReference userRef) {
        LogUtil log = new LogUtil(TAG, "initPlaylists");
        playlists = new HashMap<>();
        List<String> removeQueue = new ArrayList<>();
        for (Map.Entry<String, String> entry : playlistIds.entrySet()) {
            Playlist playlist = FirebaseService.checkDatabase(db, "playlists", entry.getValue(), Playlist.class);

            if (playlist != null && playlist.getId() != null) {
                playlists.put(entry.getKey(), playlist);
            }
            else {
                removeQueue.add(entry.getKey());
                userRef.child("playlistIds").child(entry.getKey()).removeValue();
            }
        }
        for (String playlistKey : removeQueue) {
            playlistIds.remove(playlistKey);

        }
        log.i("Playlists retrieved.");
    }

    public void initHistory(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "initHistory");
        history = new LinkedList<>();
        List<String> removeQueue = new ArrayList<>();
        Map<String, List<PhotoUrl>> data = new HashMap<>();
        for (HistoryEntry entry : historyIds) {
            Track track = FirebaseService.checkDatabase(db, "tracks", entry.getId(), Track.class);
            if (track != null) {
                if (!data.containsKey(entry.getSource())) {
                    String[] entrySource = entry.getSource().split(":");
                    String child = null;
                    switch (entrySource[1]) {
                        case "playlist":
                            child = "playlists";
                            break;
                        case "album":
                            child = "albums";
                            break;
                    }
                    if (child != null) {
                        List<PhotoUrl> photoUrl = FirebaseService.getPhotoUrl(db, child, entry.getSource());
                        if (photoUrl != null) {
                            data.put(entry.getSource(), photoUrl);
                        }
                    }
                }
                track.setPhotoUrl(data.get(entry.getSource()));

                history.add(track);
            }
            else {
                removeQueue.add(entry.getId());
            }
        }
        for (String trackId : removeQueue) {
            historyIds.remove(trackId);
        }

        if (history.size() > 0) {
            log.i("History retrieved.");
        } else {
            log.i("No history to retrieve.");
        }
    }

    public void initBadges(DatabaseReference db) {
        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            if (BadgeService.hasThumbnail(entry.getValue().getType())) {
                String child = null;
                String photoUrl = null;
                String name = null;
                String path = BadgeService.getPath(entry.getValue());

                photoUrl = BadgeService.getBadgeThumbnail(db, path);
                entry.getValue().setPhotoUrl(photoUrl);

                name = BadgeService.getBadgeName(db, path);
                entry.getValue().setName(name);
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

    @Exclude
    public boolean isSearchLimitReached(FirebaseUser firebaseUser, Role role, Activity activity) {
        return searchLimitCheck(firebaseUser, role, activity);
    }

    private boolean searchLimitCheck(FirebaseUser firebaseUser, Role role, Activity activity) {
        LogUtil log = new LogUtil(TAG, "isSearchLimitReached");
        if ((firebaseUser == null && role == Role.USER) || (firebaseUser != null && role == Role.GUEST)) {
            log.e("Parameter error.");
            return true;
        }

        if (firebaseUser != null && role == Role.USER) {
            SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

//            // DEBUG: Uncomment me to reset the search limit
//            editor.putInt(activity.getString(R.string.searchCount), 0);
//            editor.apply();

            Long lastTime = sharedPref.getLong(activity.getString(R.string.limitReached), -1);

            if (searchCount >= getSearchLimit(role) && lastTime == -1) {
                editor.putLong(activity.getString(R.string.limitReached), System.currentTimeMillis());
                editor.apply();
                return true;
            }

            if (lastTime == -1) {
                return false;
            }
            Long thisTime = System.currentTimeMillis();
            if (thisTime - lastTime >= ONE_DAY) {

                editor.putInt(activity.getString(R.string.searchCount), 0);
                editor.putLong(activity.getString(R.string.limitReached), -1);
                editor.apply();
                searchCount = 0;
                return false;
            }
        }

        return searchCount >= getSearchLimit(role);
    }


    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
