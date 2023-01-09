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

    public static void heartAlbum(FirebaseUser firebaseUser, DatabaseReference db, Album album,
                                  SpotifyService spotifyService) {
        final boolean[] artistExists = {false};
        String artistId = album.getArtistIds().get(0);

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


        if (!artistExists[0])
        {
            Artist artist = spotifyService.artistOverview(artistId);

            saveArtist(artist, db);

            processAlbums(artist.getAlbums(), db, spotifyService);
            processAlbums(artist.getSingles(), db, spotifyService);
            processAlbums(artist.getCompilations(), db, spotifyService);

            saveAlbumTracks(album, db, spotifyService);

            Log.d(TAG, "New artist");

        }


        DatabaseReference userRef = db.child("users").child(firebaseUser.getUid());

        userRef.child("albums").child(album.get_id()).setValue(album.getArtistIds().get(0));
        userRef.child("artists").child(album.getArtistIds().get(0)).setValue(album.getArtistIds().get(0));
    }

    private static void processAlbums(List<Album> albums, DatabaseReference db, SpotifyService spotifyService) {
        for (Album a : albums) {
            db.child("albums").child(a.get_id()).setValue(a);
        }
    }

    private static void saveAlbumTracks(Album album, DatabaseReference db, SpotifyService spotifyService) {
        JsonObject jsonObject = spotifyService.albumTracks(album.get_id(), 300, 0);
        JsonArray jsonArray = jsonObject
                .getAsJsonObject("data")
                .getAsJsonObject("album")
                .getAsJsonObject("tracks")
                .getAsJsonArray("items");
        // List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonTrack = jsonArray.get(i).getAsJsonObject().getAsJsonObject("track");
            Track track = new Track(
                    jsonTrack.get("uri").getAsString(),
                    jsonTrack.get("name").getAsString(),
                    album.get_id(),
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
            singles.add(a.get_id());
        }
        for (Album a : artist.getAlbums()) {
            albums.add(a.get_id());
        }
        for (Album a : artist.getCompilations()) {
            compilations.add(a.get_id());
        }

        map.put("singles", singles);
        map.put("albums", albums);
        map.put("compilations", compilations);

        db.child("artists").child(artist.getId()).setValue(map);
    }

}
