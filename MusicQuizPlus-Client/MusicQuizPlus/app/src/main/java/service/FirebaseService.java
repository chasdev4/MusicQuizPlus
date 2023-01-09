package service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.item.Album;
import model.item.Artist;
import model.item.Track;

public class FirebaseService {

    private final static String TAG = "FirebaseService.java";

    // Create a new user on both databases
    public static void createUser(FirebaseUser firebaseUser, FirebaseFirestore firestore,
                                  DatabaseReference db) {
        // Create a new user with a first and last name
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", firebaseUser.getDisplayName());
        userMap.put("email", firebaseUser.getEmail());
        userMap.put("photo_url", firebaseUser.getPhotoUrl());

        firestore.collection("users").document(firebaseUser.getUid())
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        userMap.clear();
        userMap = new HashMap<>();
        userMap.put("xp", 0);
        userMap.put("level", 1);

        db.child("users").child(firebaseUser.getUid()).setValue(userMap);
    }

    // Delete user from database
    public static boolean deleteUser(FirebaseUser firebaseUser, FirebaseFirestore firestore,
                                     DatabaseReference db) {
        final boolean[] result = {true};

        db.child("users").child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User data deleted from Realtime Database.");
                }
                else
                {
                    result[0] = false;
                }
            }
        });

        if (result[0] == true) {
            firestore.collection("users").document(firebaseUser.getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Firestore user data successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting Firestore user", e);
                        }
                    });
        }
        return result[0];
    }

    // When the user "hearts" an album
    public static void heartAlbum(FirebaseUser firebaseUser, DatabaseReference db, Album album,
                                  SpotifyService spotifyService) {

        final boolean[] artistExists = {false};
        String artistId = album.getArtistIds().get(0);

        // Check to see if the artist exists
        // If the artist exists, then there is no need to save their discography
        db.child("artists").child(artistId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
                }
                else {
                    artistExists[0] = true;
                    Log.d(TAG, String.valueOf(task.getResult().getValue()));
                }
            }
        });

        // If the artist doesn't exist
        if (!artistExists[0])
        {
            Log.i(TAG, "Fetching artist overview for " + artistId);

            // Get the artist overview from the Spotify API
            Artist artist = spotifyService.artistOverview(artistId);
            Log.i(TAG, String.format("Artist Overview for \"%s\" %s retrieved.",
                    artist.getName(),artist.getId()));


            // Save the artist to the database
            saveArtist(artist, db);
            Log.i(TAG, String.format("%s saved to database child \"artists\"", artist.getId()));

            // Save each album to the database
            createAlbums(artist.getAlbums(), db, spotifyService);
            createAlbums(artist.getSingles(), db, spotifyService);
            createAlbums(artist.getCompilations(), db, spotifyService);

            // Save the hearted album's tracks to the database
            saveAlbumTracks(album, db, spotifyService);
            Log.i(TAG, String.format("Tracks from %s saved to database child \"tracks\"", album.getId()));

        }

        DatabaseReference userRef = db.child("users").child(firebaseUser.getUid());

        userRef.child("albums").child(album.getId()).setValue(album.getArtistIds().get(0));
        Log.i(TAG, String.format("Album ID %s saved to database child \"\\users\\albums\"", album.getId()));
        userRef.child("artists").child(album.getArtistIds().get(0)).setValue(album.getArtistIds().get(0));
        Log.i(TAG, String.format("Artist ID %s saved to database child \"\\users\\artists\"", album.getArtistIds().get(0)));
    }

    private static void createAlbums(List<Album> albums, DatabaseReference db, SpotifyService spotifyService) {
        for (Album a : albums) {
            db.child("albums").child(a.getId()).setValue(a);
        }
        Log.i(TAG, String.format("%s %sS saved to database child \"albums\"",
                albums.size(), albums.get(0).getType()));
    }

    private static void saveAlbumTracks(Album album, DatabaseReference db, SpotifyService spotifyService) {
        JsonObject jsonObject = spotifyService.albumTracks(album.getId(), 300, 0);
        JsonArray jsonArray = jsonObject
                .getAsJsonObject("data")
                .getAsJsonObject("album")
                .getAsJsonObject("tracks")
                .getAsJsonArray("items");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonTrack = jsonArray.get(i).getAsJsonObject().getAsJsonObject("track");
            Track track = new Track(
                    jsonTrack.get("uri").getAsString(),
                    jsonTrack.get("name").getAsString(),
                    album.getId(),
                    album.getArtistIds()
            );
            db.child("tracks").child(track.getId()).setValue(track);
        }
    }

    private static void saveArtist(Artist artist, DatabaseReference db) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", artist.getId());
        map.put("name", artist.getName());
        map.put("photoUrl", artist.getPhotoUrl());
        map.put("bio", artist.getBio());
        map.put("externalLinks", artist.getExternalLinks());
        map.put("latest", artist.getLatest());
        List<String> singles = new ArrayList<>();
        List<String> albums = new ArrayList<>();
        List<String> compilations = new ArrayList<>();
        for (Album a : artist.getSingles()) {
            singles.add(a.getId());
        }
        for (Album a : artist.getAlbums()) {
            albums.add(a.getId());
        }
        for (Album a : artist.getCompilations()) {
            compilations.add(a.getId());
        }

        map.put("singles", singles);
        map.put("albums", albums);
        map.put("compilations", compilations);

        db.child("artists").child(artist.getId()).setValue(map);
    }

}
