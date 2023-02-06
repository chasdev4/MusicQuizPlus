package model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

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
import service.firebase.UserService;

public class Settings {
    private Difficulty difficulty;
    private boolean ignorePlaylistDifficulty;
    private boolean ignoreArtistDifficulty;

    private User user;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private GoogleSignIn googleSignIn;

    public Settings(Difficulty difficulty, User user, FirebaseUser firebaseUser, boolean ignorePlaylistDifficulty, boolean ignoreArtistDifficulty, DatabaseReference db, String versionNumber, GoogleSignIn googleSignIn) {
        this.difficulty = difficulty;
        this.user = user;
        this.firebaseUser = firebaseUser;
        this.ignorePlaylistDifficulty = ignorePlaylistDifficulty;
        this.ignoreArtistDifficulty = ignoreArtistDifficulty;
        this.db = db;
        this.googleSignIn = googleSignIn;
    }

    public Settings() {
        difficulty = Difficulty.EASY;
        ignorePlaylistDifficulty = false;
        ignoreArtistDifficulty = false;
    }

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

    public void unheartAllPlaylists() {
        for (Map.Entry<String, Playlist> playlist : user.getPlaylists().entrySet()) {
            PlaylistService.unheart(user, firebaseUser, db, playlist.getValue());
        }
        user.setPlaylists(new HashMap<>());
        user.setPlaylistIds(new HashMap<>());
        db.child("users").child(firebaseUser.getUid()).child("playlistIds").removeValue();
    }

    public void unheartAllAlbums() {
        for (Map.Entry<String, Artist> artist : user.getArtists().entrySet()) {
            List<Album> albums = new ArrayList<>();
            albums.addAll(artist.getValue().getAlbums());
            albums.addAll(artist.getValue().getSingles());
            albums.addAll(artist.getValue().getCompilations());

            for (Album album : albums) {
                AlbumService.unheart(user, firebaseUser, db, album);
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

    public void clearHistory() {
        if (firebaseUser != null) {
            user.setHistory(new LinkedList<>());
            user.setHistoryIds(new ArrayList<>());
            db.child("users").child(firebaseUser.getUid()).child("historyIds").removeValue();
        }
    }

    public void deleteAccount(FirebaseFirestore firestore) {
        UserService.deleteUser(firebaseUser, firestore, db);
    }

    public void signOut() {
        googleSignIn.signOut();
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
}
