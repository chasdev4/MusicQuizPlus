package service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.PhotoUrl;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;

public class FirebaseService {

    private final static String TAG = "FirebaseService.java";

    public static <T> T checkDatabase(DatabaseReference db, String child, String id, Class cls) {
        CountDownLatch done = new CountDownLatch(1);
        final User[] users = new User[1];
        final Album[] albums = new Album[1];
        final Artist[] artists = new Artist[1];
        final Playlist[] playlists = new Playlist[1];
        final Track[] tracks = new Track[1];
        db.child(child).child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, String.format("Attempting to retrieve /%s/%s from database.", child, id));
                switch (cls.getSimpleName()) {
                    case "User":
                        users[0] = (User)dataSnapshot.getValue(cls);
                        break;
                    case "Album":
                        albums[0] = (Album)dataSnapshot.getValue(cls);
                        break;
                    case "Artist":
                        artists[0] = (Artist)dataSnapshot.getValue(cls);
                        break;
                    case "Playlist":
                        playlists[0] = (Playlist)dataSnapshot.getValue(cls);
                        break;
                    case "Track":
                        tracks[0] = (Track)dataSnapshot.getValue(cls);
                        break;
                    default:
                        Log.w(TAG, String.format("checkDatabase: unsupported class %s.", cls.getSimpleName()));
                        break;
                }
                done.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }

        });

        try {
            done.await();


        } catch(InterruptedException e) {
            e.printStackTrace();
        }


        switch (cls.getSimpleName()) {
            case "User":
                return (T) users[0];

            case "Album":
                return (T) albums[0];
            case "Artist":
                return (T) artists[0];
            case "Playlist":
                return (T) playlists[0];
            case "Track":
                return (T) tracks[0];
            default:
                Log.w(TAG, String.format("checkDatabase: unsupported class %s.", cls.getSimpleName()));
                break;
        }


        return null;
    }

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
    public static void heartAlbum(User user, FirebaseUser firebaseUser, DatabaseReference db, Album album,
                                  SpotifyService spotifyService) {

        if (nullCheck(user, firebaseUser, db, spotifyService, "heartAlbum")) {
            return;
        }
        if (album == null) {
            Log.e(TAG, "Album provided to heartAlbum was null.");
            return;
        }


        Map<String, Object> updates = new HashMap<>();

        // Add the albumId to the user
        boolean result = user.addAlbumId(album.getId());

        // If the album wasn't added, return
        if (!result) {
            Log.w(TAG, String.format("%s already exists in albumIds list.", album.getId()));
            return;
        }

        // Save the albumId to the db user
        db.child("users")
                .child(firebaseUser.getUid())
                .child("albumIds")
                .child(String.valueOf(user.getAlbumIds().size() - 1))
                .setValue(album.getId());



        // Add the artistId to the user
        String artistId = album.getArtistIds().get(0);
        result = user.addArtistId(artistId);

        // If the artist was added, add it to the db user
        if (result) {
            db.child("users")
                    .child(firebaseUser.getUid())
                    .child("artistIds")
                    .child(String.valueOf(user.getArtistIds().size() - 1))
                    .setValue(artistId);
            Log.i(TAG, String.format("%s added to the artistIds list.", artistId));
        }
        else {
            Log.i(TAG, String.format("%s already exists in the artistIds list.", artistId));
        }


        // Check to see if the artist exists in the database
        // If the artist exists, then there is no need to save their discography
        Artist artist = checkDatabase(db, "artists", artistId, Artist.class);

        if (artist == null) {
            saveArtistOverview(artistId, album, db, spotifyService);
        }

        Album album1 = checkDatabase(db, "albums", album.getId(), Album.class);

        if (!album1.isTrackIdsKnown()) {
            // Save the hearted album's tracks to the database
            saveAlbumTracks(album, db, spotifyService);
            Log.i(TAG, String.format("Tracks from %s saved to database child \"tracks\"", album.getId()));
        }
        else {
            Log.i(TAG, String.format("Tracks from %s have previously been saved to database", album.getId()));
        }
        updates.put("albums/"+album.getId()+"/followers", ServerValue.increment(1));
        if (!album1.isFollowersKnown()) {
            updates.put("albums/"+album.getId()+"/followersKnown", true);
        }
        updates.put("artists/"+artistId+"/followers", ServerValue.increment(1));
        db.updateChildren(updates);
    }

    private static void saveArtistOverview(String artistId, Album album, DatabaseReference db, SpotifyService spotifyService) {
        Log.i(TAG, "Fetching artist overview for " + artistId);

        // Get the artist overview from the Spotify API
        Artist artist = spotifyService.artistOverview(artistId);
        Log.i(TAG, String.format("Artist Overview for \"%s\" %s retrieved.",
                artist.getName(),artist.getId()));

        // Save the artist to the database
        db.child("artists").child(artist.getId()).setValue(artist);
        Log.i(TAG, String.format("%s saved to database child \"artists\"", artist.getId()));

        // Save each album to the database
        createAlbums(artist.getAlbums(), db, spotifyService);
        createAlbums(artist.getSingles(), db, spotifyService);
        createAlbums(artist.getCompilations(), db, spotifyService);
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
                    album.getArtistIds(),
                    0,
                    false);
            album.getTrackIds().add(track.getId());
            db.child("tracks").child(track.getId()).setValue(track);
        }
        album.setTrackIdsKnown(true);
        db.child("albums").child(album.getId()).child("trackIdsKnown").setValue(true);
        db.child("albums").child(album.getId()).child("trackIds").setValue(album.getTrackIds());
    }

    // TODO: Complete or delete this method
    public static void createDefaultPlaylists(FirebaseUser firebaseUser, DatabaseReference db, SpotifyService spotifyService) {
        JsonArray jsonArray = spotifyService.getDefaultPlaylists();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String playlistId = jsonObject.getAsJsonObject("uri").getAsString();

            List<PhotoUrl> photoUrl = new ArrayList<>();
            String[] imageIdArray = jsonObject.get("image_url").getAsString().split(":");
            photoUrl.add(new PhotoUrl("https://i.scdn.co/image/" + imageIdArray[2], 0, 0));


//            Playlist playlist = new Playlist(
//                    jsonObject.getAsJsonObject("uri").getAsString(),
//                    jsonObject.getAsJsonObject("name").getAsString(),
//                    photoUrl,
//                    "Spotify",
//
//                    );
//
//            db.child("default_playlists")
//                    .child(playlistId)
//                    .setValue(playlist);
        }
    }

    // When the user "hearts" an playlist
    public static void heartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                                     SpotifyService spotifyService) {

        if (nullCheck(user, firebaseUser, db, spotifyService, "heartPlaylist")) {
            return;
        }
        if (playlist == null) {
            Log.e(TAG, "Playlist provided to heartPlaylist was null.");
            return;
        }

        // Add the playlistId to the user
        boolean result = user.addPlaylistId(playlist.getId());

        // If the playlist wasn't added, return
        if (!result) {
            Log.w(TAG, String.format("%s already exists in playlistIds list.", playlist.getId()));
            return;
        }

        // Save the playlistId to the db user
        db.child("users")
                .child(firebaseUser.getUid())
                .child("playlistIds")
                .child(String.valueOf(user.getPlaylistIds().size() - 1))
                .setValue(playlist.getId());

        // Check to see if the playlist exists in the database
        // If the playlist exists, then there is no need to save it
        CountDownLatch done = new CountDownLatch(1);
        final boolean playlistExists[] = {false};
        db.child("playlists").child(playlist.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playlistExists[0] = dataSnapshot.getValue() != null;
                done.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }


        });

        try {
            done.await();

            if (playlistExists[0]) {
                Log.i(TAG, String.format("%s already exists in database.", playlist.getId()));
                return;
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        if (!playlistExists[0]) {
            Log.i(TAG, "DataSnapshot returned null, saving playlist...");
            db.child("playlists").child(playlist.getId()).setValue(playlist);
            Log.i(TAG, String.format("%s saved to child \"playlists\"", playlist.getId()));
        }

    }

    // Used when a playlists tracks have not been populated yet, like from the search results
    public static Playlist populatePlaylistTracks(DatabaseReference db, Playlist playlist, SpotifyService spotifyService) {
        DatabaseReference playlistRef = db.child("playlists").child(playlist.getId());

        // Playlist is already populated, do nothing
        if (playlist.isPopulated() && playlistRef.child("isPopulated").get().equals("true")) {
            return playlist;
        }

        // Use the spotify service class to get playlist's tracks
        JsonArray items = spotifyService.playlistTracks(playlist.getId(), 200, 0);

        // Loop thru and extract each tracks
        for (int i = 0; i < items.size(); i++) {
            JsonObject jsonObject = items.get(i).getAsJsonObject().getAsJsonObject("track");

            JsonArray artistsArray = jsonObject.getAsJsonArray("artists");
            List<String> artistIds = new ArrayList<>();
            for (int j = 0; j < artistsArray.size(); j++) {
                artistIds.add(artistsArray.get(j).getAsJsonObject().getAsJsonObject("uri").getAsString());
            }

            // Add the trackId's to the playlist
            playlist.addTrackId(jsonObject.getAsJsonObject("uri").getAsString());

            // Build a new track
            Track track = new Track(
                    jsonObject.getAsJsonObject("uri").getAsString(),
                    jsonObject.getAsJsonObject("name").getAsString(),
                    jsonObject.getAsJsonObject("album").getAsJsonObject("uri").getAsString(),
                    artistIds,
                    jsonObject.getAsJsonObject("popularity").getAsShort(),
                    true
            );

            // Save that track to the database
            db.child("tracks").child(track.getId()).setValue(track);

            // Add the track to the playlist
            playlist.addTrack(track);
        }


        playlist.setPopulated(true);
        db.child("playlists").child(playlist.getId()).child("trackIds").setValue(playlist.getTrackIds());
        db.child("playlists").child(playlist.getId()).child("isPopulated").setValue(true);

        return playlist;
    }

    public static void unheartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                                        SpotifyService spotifyService) {
        if (nullCheck(user, firebaseUser, db, spotifyService, "unheartPlaylist")) {
            return;
        }
        if (playlist == null) {
            Log.e(TAG, "Playlist provided to unheartPlaylist was null.");
            return;
        }


    }

    private static boolean nullCheck(User user, FirebaseUser firebaseUser, DatabaseReference db,
                                  SpotifyService spotifyService, String methodName) {
        if (user == null) {
            Log.e(TAG, String.format("User provided to %s was null.", methodName));
            return true;
        }
        if (firebaseUser == null) {
            Log.e(TAG, String.format("FirebaseUser provided to %s was null.", methodName));
            return true;
        }
        if (db == null) {
            Log.e(TAG, String.format("DatabaseReference provided to %s was null.", methodName));
            return true;
        }
        if (spotifyService == null) {
            Log.e(TAG, String.format("SpotifyService provided to %s was null.", methodName));
            return true;
        }
        return false;
    }
}
