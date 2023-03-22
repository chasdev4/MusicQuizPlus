package model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.type.Difficulty;
import service.firebase.AlbumService;
import service.firebase.PlaylistService;

public class Settings implements Serializable {
    //#region Members
    private Difficulty difficulty;
    private boolean ignorePlaylistDifficulty;
    private boolean ignoreArtistDifficulty;

    private User user;
    //#endregion

    // Excluded from database
    private boolean playNowBannerHidden;

    //#region Constructors
    public Settings(Difficulty difficulty,
                    User user,
                    boolean ignorePlaylistDifficulty,
                    boolean ignoreArtistDifficulty) {
        this.difficulty = difficulty;
        this.user = user;
        this.ignorePlaylistDifficulty = ignorePlaylistDifficulty;
        this.ignoreArtistDifficulty = ignoreArtistDifficulty;
    }

    public Settings() {
        difficulty = Difficulty.EASY;
        ignorePlaylistDifficulty = false;
        ignoreArtistDifficulty = false;
    }
    //#endregion

    //#region Accessors
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isIgnorePlaylistDifficulty() {
        return ignorePlaylistDifficulty;
    }

    public boolean isIgnoreArtistDifficulty() {
        return ignoreArtistDifficulty;
    }

    @Exclude
    public User getUser() {
        return user;
    }
    @Exclude
    public int getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }

    }
    //#endregion

    //#region Mutators
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void clickIgnorePlaylistDifficulty() {
        if (ignoreArtistDifficulty && !ignorePlaylistDifficulty) {
            ignoreArtistDifficulty = false;
        }
        ignorePlaylistDifficulty = !ignorePlaylistDifficulty;
    }

    public void clickIgnoreArtistDifficulty() {
        if (ignorePlaylistDifficulty && !ignoreArtistDifficulty) {
            ignorePlaylistDifficulty = false;
        }
        ignoreArtistDifficulty = !ignoreArtistDifficulty;
    }
    //#endregion

    //#region Methods
    public void unheartAllPlaylists(FirebaseUser firebaseUser, DatabaseReference db) {
        for (Map.Entry<String, Playlist> playlist : user.getPlaylists().entrySet()) {
            PlaylistService.unheart(user, firebaseUser, db, playlist.getValue(), new Runnable() {
                @Override
                public void run() {
                    return;
                }
            });
        }
        user.setPlaylists(new HashMap<>());
        user.setPlaylistIds(new HashMap<>());
        db.child("users").child(firebaseUser.getUid()).child("playlistIds").removeValue();
    }

    public void unheartAllAlbums(FirebaseUser firebaseUser, DatabaseReference db) {
        for (Map.Entry<String, Artist> artist : user.getArtists().entrySet()) {
            List<Album> albums = new ArrayList<>();
            albums.addAll(artist.getValue().getAlbums());
            albums.addAll(artist.getValue().getSingles());
            albums.addAll(artist.getValue().getCompilations());

            for (Album album : albums) {
                AlbumService.unheart(firebaseUser, db, album, new Runnable() {
                    @Override
                    public void run() {
                        return;
                    }
                });
                album.setTrackIds(new ArrayList<>());
                album.setTracks(new ArrayList<>());
            }
            artist.getValue().setAlbumIds(new ArrayList<>());
            artist.getValue().setAlbums(new ArrayList<>());
            artist.getValue().setSingleIds(new ArrayList<>());
            artist.getValue().setSingles(new ArrayList<>());
            artist.getValue().setCompilationIds(new ArrayList<>());
            artist.getValue().setCompilations(new ArrayList<>());
        }
        user.setArtists(new HashMap<>());
        user.setArtistIds(new HashMap<>());
    }

    public void clearHistory(FirebaseUser firebaseUser, DatabaseReference db) {
        if (firebaseUser != null) {
            user.setHistory(new LinkedList<>());
            user.setHistoryIds(new ArrayList<>());
            db.child("users").child(firebaseUser.getUid()).child("historyIds").removeValue();
        }
    }

    public boolean deleteAccount(FirebaseUser firebaseUser, DatabaseReference db) {
        unheartAllPlaylists(firebaseUser, db);
        unheartAllAlbums(firebaseUser, db);
        return user.delete(firebaseUser, db);
    }

    public void signOut(GoogleSignIn googleSignIn) { googleSignIn.signOut(); }

    // Create a copy of settings before modification, pass it here
    public void close(FirebaseUser firebaseUser, DatabaseReference db, Settings oldSettings) {
        if (firebaseUser == null) {
            // TODO: Save difficulty locally
            // Note: This might need to be done from an activity
            return;
        }
        // Update the database
        Map<String, Object> updates = new HashMap<>();
        String userPath = "users/" + firebaseUser.getUid() + "/settings/";
        if (!oldSettings.difficulty.equals(difficulty)) {
            updates.put(userPath+"difficulty", difficulty);
        }
        if (!oldSettings.ignorePlaylistDifficulty != ignorePlaylistDifficulty) {
            updates.put(userPath+"ignorePlaylistDifficulty", ignorePlaylistDifficulty);
        }
        if (!oldSettings.ignoreArtistDifficulty != ignoreArtistDifficulty) {
            updates.put(userPath+"ignoreArtistDifficulty", ignoreArtistDifficulty);
        }
        if (updates.size() > 0) {
            db.updateChildren(updates);
        }
    }

    @Exclude
    public boolean isPlayNowBannerHidden() {
        return playNowBannerHidden;
    }

    public void setPlayNowBannerHidden(boolean playNowBannerHidden) {
        this.playNowBannerHidden = playNowBannerHidden;
    }
    //#endregion
}
