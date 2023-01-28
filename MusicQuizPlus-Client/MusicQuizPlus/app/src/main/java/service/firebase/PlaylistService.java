package service.firebase;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.PhotoUrl;
import model.User;
import model.ValidationObject;
import model.item.Playlist;
import model.item.Track;
import model.type.Severity;
import service.FirebaseService;
import service.SpotifyService;
import utils.LogUtil;
import utils.ValidationUtil;


public class PlaylistService {
    private final static String TAG = "PlaylistService.java";

    // Used when a playlists tracks have not been populated yet, like from the search results
    public static Playlist populatePlaylistTracks(DatabaseReference db, Playlist playlist, SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "populatePlaylistTracks");
        // Playlist is already populated, do nothing
        if (playlist.getTracksListFromMap().size() > 0) {
            return playlist;
        }

        // Check the database for the playlist
        Playlist playlist1 = FirebaseService.checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

        // The playlist doesn't exist
        if (playlist1 == null) {
            log.i("DataSnapshot returned null, retrieving playlist tracks...");
        }
        // It exists, the tracks should be known
        else if (playlist1.getTrackIds().size() > 0) {
            log.i("Playlist exists in database, returning playlist...");
            return playlist1;
        }

        // Use the spotify service class to get playlist's tracks
        JsonArray items = spotifyService.playlistTracks(playlist.getId());

        int popularity = 0;
        int numTracks = 0;

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
                Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);

                // If the track doesn't exist
                if (track == null) {
                    // Extract track's artist info
                    JsonArray artistsArray = jsonObject.getAsJsonArray("artists");
                    Map<String, String> artistsMap = new HashMap<>();
                    String artistId = artistsArray.get(0).getAsJsonObject().get("uri").getAsString();
                    for (int j = 0; j < 1; j++) {
                        artistsMap.put(artistsArray.get(j).getAsJsonObject().get("uri").getAsString(),
                                artistsArray.get(j).getAsJsonObject().get("name").getAsString());
                    }

                    // Build a new track
                    track = new Track(
                            trackId,
                            jsonObject.get("name").getAsString(),
                            jsonObject.getAsJsonObject("album").get("uri").getAsString(),
                            jsonObject.getAsJsonObject("album").get("name").getAsString(),
                            artistId,
                            artistsMap,
                            jsonObject.get("popularity").getAsInt(),
                            true,
                            jsonObject.get("preview_url").getAsString(),
                            false,
                            jsonObject.getAsJsonObject("album").get("release_date").toString().substring(1, 5),
                            jsonObject.get("is_playable").getAsBoolean());
                }

                popularity += track.getPopularity();
                numTracks++;

                // Get a key to add the playlist Id to the track
                DatabaseReference trackRef = db.child("tracks").child(trackId);
                String key = trackRef.child("playlistIds").push().getKey();

                // Try and add the playlistId to the track
                boolean result = track.addPlaylistId(key, playlist.getId());
                if (result) {
                    // Add the track to the playlist
                    playlist.putTrack(playlist.getTrackIds().size() - 1, track);
                }
            }
            else {
                log.i(String.format("Track #%s in items was null", String.valueOf(i)));
            }

        }

        playlist.setAveragePopularity(popularity / numTracks);
        return playlist;
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


    // When the user "hearts" a playlist
    public static void heart(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                             SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "heartPlaylist");

        // Return if any of these fields are null
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(user, User.class, Severity.HIGH));
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(playlist, Playlist.class, Severity.HIGH));
                add(new ValidationObject(spotifyService, SpotifyService.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        // Add the playlistId to the user
        String key = db.child("users").child(firebaseUser.getUid()).child("playlistIds").push().getKey();
        boolean result = user.addPlaylistId(key, playlist.getId());

        // If the playlist wasn't added, return
        if (!result) {
            log.w(String.format("%s already exists in playlistIds list.", playlist.getId()));
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
        Playlist playlist1 = FirebaseService.checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

        // Playlist doesn't exist in db yet
        if (playlist1 == null) {
            log.i("DataSnapshot returned null, saving playlist...");
        }
        // Playlist exists but something really really unexpected happened
        else if (!playlist.getId().equals(playlist1.getId())) {
            log.e("Playlist retrieved but the ID's don't match. That was unexpected...");
            return;
        }
        // Playlist exists and the followers are only known on the db
        else if (playlist1.isFollowersKnown() && !playlist.isFollowersKnown()) {
            playlist.setFollowersKnown(true);
            playlist.setFollowers(playlist1.getFollowers());
        }

        // Save the playlist to the db
        db.child("playlists").child(playlist.getId()).setValue(playlist);
        log.i(String.format("%s saved to child \"playlists\"", playlist.getId()));

        // Increment the follower count
        Map<String, Object> updates = new HashMap<>();
        updates.put("playlists/"+playlist.getId()+"/followers", ServerValue.increment(1));
        if (!playlist.isFollowersKnown()) {
            updates.put("playlists/"+playlist.getId()+"/followersKnown", true);
        }

        db.updateChildren(updates);

        // Save each track to the database
        for (Map.Entry<Integer, Track> t : playlist.getTracks().entrySet()) {
            db.child("tracks").child(t.getValue().getId()).setValue(t.getValue());
        }
    }

    public static void unheart(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
                               SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "unheartPlaylist");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(user, User.class, Severity.HIGH));
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(playlist, Playlist.class, Severity.HIGH));
                add(new ValidationObject(spotifyService, SpotifyService.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        // Attempt to remove the playlist from the user
        String key = user.removePlaylistId(playlist.getId());

        // If the playlist wasn't found, abort
        if (key == null) {
            log.w("Playlist not previously saved to user. Aborting...");
            return;
        }

        // Remove the playlist from the database user
        db.child("users").child(firebaseUser.getUid()).child("playlistIds").child(key).removeValue();
        log.i(String.format("\"%s : %s\" removed from users/%s/playlistIds", key, playlist.getId(), firebaseUser.getUid()));

        // If the playlist is on it's last follower or lower, remove it and it's tracks from the database
        if (playlist.getFollowers() <= 1 && playlist.isFollowersKnown()) {
            for (String trackId : playlist.getTrackIds()) {
                // Get the track from the database
                Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
                // Check to see if it's safe to delete, the track may belong to a saved album
                if (!track.isAlbumKnown()) {
                    db.child("tracks").child(trackId).removeValue();
                    log.i(String.format("%s removed from database child /tracks", trackId));
                }
                else {
                    log.i(String.format("%s belongs to a saved album.", trackId));
                }

                db.child("tracks").child(trackId).removeValue();
            }
            log.i(String.format("%s tracks belonging to %s have removed from /tracks", String.valueOf(playlist.getTrackIds().size()), playlist.getId(), firebaseUser.getUid()));

            // Remove the playlist from the database
            db.child("playlists").child(playlist.getId()).removeValue();
            log.i(String.format("%s removed from /playlists", playlist.getId()));

        }
        // Else the playlist has enough followers to live
        else {
            // Decrement the follower count
            Map<String, Object> updates = new HashMap<>();
            updates.put("playlists/"+playlist.getId()+"/followers", ServerValue.increment(-1));
            db.updateChildren(updates);
            log.i(String.format("%s follower count has decremented.", playlist.getId()));
        }
    }

}