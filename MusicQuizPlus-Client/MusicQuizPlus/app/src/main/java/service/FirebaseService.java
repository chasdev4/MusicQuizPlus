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
import com.google.gson.JsonElement;
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
        String key = db.child("users").child(firebaseUser.getUid()).child("albumIds").push().getKey();
        boolean result = user.addAlbumId(key, album.getId());

        // If the album wasn't added, return
        if (!result) {
            Log.w(TAG, String.format("%s already exists in albumIds list.", album.getId()));
            return;
        }

        // Save the albumId to the db user
        db.child("users")
                .child(firebaseUser.getUid())
                .child("albumIds")
                .child(key)
                .setValue(album.getId());

        // Add the artistId to the user
        String artistId = album.getArtistIds().get(0);
        key = db.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
        result = user.addArtistId(key, artistId);

        // If the artist was added, add it to the db user
        if (result) {
            db.child("users")
                    .child(firebaseUser.getUid())
                    .child("artistIds")
                    .child(key)
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
                    false,
                    null,
                    true);
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

    // Used when a playlists tracks have not been populated yet, like from the search results
    public static Playlist populatePlaylistTracks(DatabaseReference db, Playlist playlist, SpotifyService spotifyService) {
        // Playlist is already populated, do nothing
        if (playlist.isTrackIdsKnown()) {
            return playlist;
        }

        // Check the database for the playlist
        Playlist playlist1 = checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

        // The playlist doesn't exist
        if (playlist1 == null) {
            Log.i(TAG, "DataSnapshot returned null, retrieving playlist tracks...");
        }
        // It exists, the tracks should be known
        else if (playlist1.isTrackIdsKnown()) {
            Log.i(TAG, "Playlist exists in database, returning playlist...");
            return playlist1;
        }

        // Use the spotify service class to get playlist's tracks
        JsonArray items = spotifyService.playlistTracks(playlist.getId());

        // Loop thru and extract each track
        for (int i = 0; i < items.size(); i++) {
            // Get the JsonObject from the items array
            String jsonString = items.get(i).getAsJsonObject().get("track").toString();
            if (!jsonString.equals("null")) {
                JsonObject jsonObject = spotifyService.getGson().fromJson(jsonString, JsonElement.class).getAsJsonObject();

                // Grabbing the trackId
                String trackId = jsonObject.get("uri").getAsString();

                // Add the trackId to the playlist
                playlist.addTrackId(trackId);

                // Check the database to see if the track exists
                Track track = checkDatabase(db, "tracks", trackId, Track.class);

                // If the track doesn't exist
                if (track == null) {
                    // Extract track's artist info
                    JsonArray artistsArray = jsonObject.getAsJsonArray("artists");
                    List<String> artistIds = new ArrayList<>();
                    for (int j = 0; j < artistsArray.size(); j++) {
                        artistIds.add(artistsArray.get(j).getAsJsonObject().get("uri").toString());
                    }

                    // Build a new track
                    track = new Track(
                            trackId,
                            jsonObject.get("name").toString(),
                            jsonObject.getAsJsonObject("album").get("uri").toString(),
                            artistIds,
                            jsonObject.get("popularity").getAsInt(),
                            true,
                            jsonObject.get("preview_url").toString(),
                            false);
                }


                // Get a key to add the playlist Id to the track
                DatabaseReference trackRef = db.child("tracks").child(trackId);
                String key = trackRef.child("playlistIds").push().getKey();

                // Try and add the playlistId to the track
                boolean result = track.addPlaylistId(key, playlist.getId());
                if (result) {
                    // Add the track to the playlist
                    playlist.addTrack(track);
                }
            }
            else {
                Log.i(TAG, String.format("Track #%s in items was null", String.valueOf(i)));
            }

        }

        playlist.setTrackIdsKnown(true);
        return playlist;
    }

    // When the user "hearts" a playlist
    public static void heartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                                     SpotifyService spotifyService) {
        // Return if any of these fields are null
        if (nullCheck(user, firebaseUser, db, spotifyService, "heartPlaylist")) {
            return;
        }

        // Or if the playlist is null
        if (playlist == null) {
            Log.e(TAG, "Playlist provided to heartPlaylist was null.");
            return;
        }

        // Add the playlistId to the user
        String key = db.child("users").child(firebaseUser.getUid()).child("playlistIds").push().getKey();
        boolean result = user.addPlaylistId(key, playlist.getId());

        // If the playlist wasn't added, return
        if (!result) {
            Log.w(TAG, String.format("%s already exists in playlistIds list.", playlist.getId()));
            return;
        }

        // Save the playlistId to the db user
        db.child("users")
                .child(firebaseUser.getUid())
                .child("playlistIds")
                .child(key)
                .setValue(playlist.getId());


        // Check to see if the playlist exists in the database
        // If the playlist exists, then there is no need to save it
        Playlist playlist1 = checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

        // Playlist doesn't exist in db yet
        if (playlist1 == null) {
            Log.i(TAG, "DataSnapshot returned null, saving playlist...");
        }
        // Playlist exists but something really really unexpected happened
        else if (!playlist.getId().equals(playlist1.getId())) {
            Log.e(TAG, "Playlist retrieved but the ID's don't match. That was unexpected...");
            return;
        }
        // Playlist exists and the followers are only known on the db
        else if (playlist1.isFollowersKnown() && !playlist.isFollowersKnown()) {
            playlist.setFollowersKnown(true);
            playlist.setFollowers(playlist1.getFollowers());
        }

        // Save the playlist to the db
        db.child("playlists").child(playlist.getId()).setValue(playlist);
        Log.i(TAG, String.format("%s saved to child \"playlists\"", playlist.getId()));

        // Increment the follower count
        Map<String, Object> updates = new HashMap<>();
        updates.put("playlists/"+playlist.getId()+"/followers", ServerValue.increment(1));
        if (!playlist.isFollowersKnown()) {
                updates.put("playlists/"+playlist.getId()+"/followersKnown", true);
        }

        db.updateChildren(updates);

        // Save each track to the database
        for (Track t : playlist.getTracks()) {
            db.child("tracks").child(t.getId()).setValue(t);
        }

        // DEBUG: Uncomment me to create a sample_history table
        // db.child("sample_history").setValue(playlist.getTrackIds());

    }

    public static void unheartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                                        SpotifyService spotifyService) {
        // Null check
        if (nullCheck(user, firebaseUser, db, spotifyService, "unheartPlaylist")) {
            return;
        }
        if (playlist == null) {
            Log.e(TAG, "Playlist provided to unheartPlaylist was null.");
            return;
        }

        // Attempt to remove the playlist from the user
        String key = user.removePlaylistId(playlist.getId());

        // If the playlist wasn't found, abort
        if (key == null) {
            Log.w(TAG, "Playlist not previously saved to user. Aborting...");
            return;
        }

        // Remove the playlist from the user
        db.child("users").child(firebaseUser.getUid()).child("playlistIds").child(key).removeValue();
        Log.i(TAG, String.format("\"%s : %s\" removed from users/%s/playlistIds", key, playlist.getId(), firebaseUser.getUid()));

        // If the playlist is on it's last follower or lower, remove it from the database
        if (playlist.getFollowers() <= 1 && playlist.isFollowersKnown()) {
            for (String trackId : playlist.getTrackIds()) {
                db.child("tracks").child(trackId).removeValue();
            }
            Log.i(TAG, String.format("%s tracks belonging to %s have removed from /tracks", String.valueOf(playlist.getTrackIds().size()), playlist.getId(), firebaseUser.getUid()));

            // Remove the playlist from the database
            db.child("playlists").child(playlist.getId()).removeValue();
            Log.i(TAG, String.format("%s removed from /playlists", playlist.getId()));

        }
        // Else the playlist has enough followers to live
        else {
            // Decrement the follower count
            Map<String, Object> updates = new HashMap<>();
            updates.put("albums/"+playlist.getId()+"/followers", ServerValue.increment(-1));
            db.updateChildren(updates);
            Log.i(TAG, String.format("%s follower count has decremented.", playlist.getId()));
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
