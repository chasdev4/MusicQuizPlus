package service.firebase;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import model.User;
import model.ValidationObject;
import model.item.Album;
import model.item.Artist;
import model.item.Track;
import model.type.AlbumType;
import model.type.HeartResponse;
import model.type.Severity;
import service.FirebaseService;
import service.SpotifyService;
import utils.LogUtil;
import utils.ValidationUtil;

public class AlbumService {
    private final static String TAG = "AlbumService.java";

    // When the user "hearts" an album
    public static HeartResponse heart(User user1, FirebaseUser firebaseUser, DatabaseReference db, Album album,
                                      SpotifyService spotifyService, Runnable hidePopup) {
        LogUtil log = new LogUtil(TAG, "heart");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(album, Album.class, Severity.HIGH));
                add(new ValidationObject(spotifyService, SpotifyService.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return HeartResponse.NULL_PARAMETER;
        }
        User user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
        if (user == null) {
            return HeartResponse.NULL_USER;
        }

        // Database updates
        Map<String, Object> updates = new HashMap<>();

        // Generate a key for the album
        String albumKey = db.child("users").child(firebaseUser.getUid()).child("albumIds").push().getKey();

        // If the album already exists
        if (user.getAlbumIds().containsValue(album.getId())) {
            log.w(String.format("%s already exists in albumIds list.", album.getId()));
            return HeartResponse.ALBUM_EXISTS;
        }

        // Add albumId to db user
        updates.put("users/" + firebaseUser.getUid() + "/albumIds/" + albumKey, album.getId());
//        db.child("users")
//                .child(firebaseUser.getUid())
//                .child("albumIds")
//                .child(key)
//                .setValue(album.getId());


        String artistKey = null;
        // Check for artist
        if (!user.getArtistIds().containsValue(album.getArtistId())) {
            // Generate a new key
            artistKey = db.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
        } else {
            for (Map.Entry<String, String> artistId : user.getArtistIds().entrySet()) {
                if (album.getArtistId().equals(artistId.getValue())) {
                    artistKey = artistId.getKey();
                }
            }
        }

        boolean result = false;
        User dbUser = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
        if (dbUser.getArtistIds().size() > user.getArtistIds().size()) {
            result = !dbUser.getArtistIds().containsValue(album.getArtistId());
        } else {
            result = !user.getArtistIds().containsValue(album.getArtistId());
        }
        if (result) {
            updates.put("users/" + firebaseUser.getUid() + "/artistIds/" + artistKey, album.getArtistId());

        } else {
            log.i(String.format("%s already exists in the artistIds list.", album.getArtistId()));
        }


        // Check to see if the artist exists in the database
        // If the artist exists, then there is no need to save their discography
        Artist artist = FirebaseService.checkDatabase(db, "artists", album.getArtistId(), Artist.class);

        if (artist == null) {
            CountDownLatch cdl = new CountDownLatch(1);
            artist = saveArtistOverview(album.getArtistId(), album, db, spotifyService);
            cdl.countDown();

            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Album album1 = FirebaseService.checkDatabase(db, "albums", album.getId(), Album.class);

        if (album1 == null) {
            //TODO: Inform the user? Error occurs when hearting directly from search view
            updates.remove("users/" + firebaseUser.getUid() + "/albumIds/" + albumKey);
            log.e("Album can't be saved because it isn't saved to artist.");
            return HeartResponse.ALBUM_ARTIST_DO_NOT_MATCH;
        }

        if (!album1.isTrackIdsKnown()) {
            // Save the hearted album's tracks to the database
            saveAlbumTracks(album, db, spotifyService);
            log.i(String.format("Tracks from %s saved to database child \"tracks\"", album.getId()));
        } else {
            log.i(String.format("Tracks from %s have previously been saved to database", album.getId()));
        }
        db.child("albums").child(album.getId()).child("followers").setValue(ServerValue.increment(1));
//        updates.put("albums/" + album.getId() + "/followers", ServerValue.increment(1));
        if (!album1.isFollowersKnown()) {
            updates.put("albums/" + album.getId() + "/followersKnown", true);
        }
        if (result) {
            db.child("artists").child(album.getArtistId()).child("followers").setValue(ServerValue.increment(1));
//            updates.put("artists/" + album.getArtistId() + "/followers", ServerValue.increment(1));
            updates.put("artists/" + album.getArtistId() + "/followersKnown", true);
        }

        user.getArtists().put(album.getArtistId(), artist);
        db.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hidePopup.run();
            }
        });
        user.addAlbumId(albumKey, album.getId());
        if (result) {
            user.addArtistId(artistKey, album.getArtistId());
            user.getArtists().put(artistKey, artist);
            log.i(String.format("%s added to the artistIds list.", album.getArtistId()));
        }
        return HeartResponse.OK;
    }


    private static Artist saveArtistOverview(String artistId, Album album, DatabaseReference db, SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "saveArtistOverview");
        log.i("Fetching artist overview for " + artistId);

        // Get the artist overview from the Spotify API
        Artist artist = spotifyService.artistOverview(artistId);
        log.i(String.format("Artist Overview for \"%s\" %s retrieved.",
                artist.getName(), artist.getId()));

        // Save the artist to the database
        db.child("artists").child(artist.getId()).setValue(artist);
        log.i(String.format("%s saved to database child \"artists\"", artist.getId()));

        // Save each album to the database
        createAlbums(artist.getAlbums(), AlbumType.ALBUM, db, spotifyService);
        createAlbums(artist.getSingles(), AlbumType.SINGLE, db, spotifyService);
        createAlbums(artist.getCompilations(), AlbumType.COMPILATION, db, spotifyService);
        return artist;
    }

    private static void createAlbums(List<Album> albums, AlbumType albumType, DatabaseReference db, SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "createAlbums");
        for (Album a : albums) {
            db.child("albums").child(a.getId()).setValue(a);
        }
        log.i(String.format("%s %sS saved to database child \"albums\"",
                albums.size(), albumType));
    }

    private static void saveAlbumTracks(Album album, DatabaseReference db, SpotifyService spotifyService) {
        JsonObject jsonObject = spotifyService.albumTracks(album.getId());
        JsonArray jsonArray = jsonObject
                .getAsJsonObject("data")
                .getAsJsonObject("album")
                .getAsJsonObject("tracks")
                .getAsJsonArray("items");
        String trackIds = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonTrack = jsonArray.get(i).getAsJsonObject().getAsJsonObject("track");
            String[] idParts = jsonTrack.get("uri").getAsString().split(":");
            trackIds += idParts[2] + "%2C";
        }
        trackIds = trackIds.substring(0, trackIds.length() - 3);
        Map<String, String> previewUrls = new HashMap<>();
        JsonArray tracks = spotifyService.getTracks(trackIds);
        for (int i = 0; i < tracks.size(); i++) {
            JsonObject track = tracks.get(i).getAsJsonObject();
            if (track.get("is_playable").getAsBoolean()) {
                previewUrls.put(track.get("uri").getAsString(), track.get("preview_url").getAsString());
            }
        }

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonTrack = jsonArray.get(i).getAsJsonObject().getAsJsonObject("track");
            String trackId = jsonTrack.get("uri").getAsString();

            if (previewUrls.containsKey(trackId)) {

                JsonArray artistsArray = jsonTrack.get("artists").getAsJsonObject().get("items").getAsJsonArray();
                String artistId = artistsArray.get(0).getAsJsonObject().get("uri").getAsString();
                Map<String, String> artistsMap = new HashMap<>();
                for (int j = 0; j < artistsArray.size(); j++) {
                    artistsMap.put(artistsArray.get(j).getAsJsonObject().get("uri").getAsString(),
                            artistsArray.get(j).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString());
                }

                Track track = new Track(
                        trackId,
                        jsonTrack.get("name").getAsString(),
                        album.getId(),
                        album.getName(),
                        true,
                        artistId,
                        artistsMap,
                        0,
                        false,
                        previewUrls.get(trackId),
                        album.getYear(),
                        jsonTrack.getAsJsonObject("playability").get("playable").getAsBoolean(),
                        album.getPhotoUrl());
                album.addTrackId(track.getId());
                db.child("tracks").child(track.getId()).setValue(track);
            }
        }
        album.setTrackIdsKnown(true);
        db.child("albums").child(album.getId()).child("trackIdsKnown").setValue(true);
        db.child("albums").child(album.getId()).child("trackIds").setValue(album.getTrackIds());
    }

    public static HeartResponse unheart(User user1, FirebaseUser firebaseUser, DatabaseReference db, Album album, Runnable hidePopUp) {
        LogUtil log = new LogUtil(TAG, "unheart");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(album, Album.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return HeartResponse.NULL_PARAMETER;
        }

        User user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
        if (user == null) {
            return HeartResponse.NULL_USER;
        }

        String albumKey = null;
        if (user.getAlbumIds().containsValue(album.getId())) {
            for (Map.Entry<String, String> albumId : user.getAlbumIds().entrySet()) {
                if (albumId.getValue().equals(albumId.getValue())) {
                    albumKey = albumId.getKey();
                    break;
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            log.e("Album not previously saved to user. Aborting...");
            return HeartResponse.ALBUM_NOT_FOUND;
        }

        Map<String, Object> updates = new HashMap<>();

//        updates.put("users/" + firebaseUser.getUid() + "/albumIds/" + albumKey, null);
//        db.child("users").child(firebaseUser.getUid()).child("albumIds").child(key).removeValue();

        // If the album is on it's last follower or lower, remove it's tracks from the database
        if (album.getFollowers() <= 1 && album.isFollowersKnown()) {
            for (String trackId : album.getTrackIds()) {
                // Get the track from the database
                Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
                if (track != null) {
                    // Check to see if it's safe to delete, the track may belong to a saved playlist
                    if (track.isAlbumKnown() && track.getPlaylistIds() == null) {
                        updates.put("tracks/" + trackId, null);
//                        db.child("tracks").child(trackId).removeValue();
//                        log.i(String.format("%s removed from database child /tracks", trackId));
                    } else {
//                        log.i(String.format("%s belongs to one or more playlists.", trackId));
                    }
                }
            }
        }

//        // Check to see if the user has any other albums saved
//        Artist artist = FirebaseService.checkDatabase(db, "artists", album.getArtistId(), Artist.class);
//        int count = 0;
//
//        for (String artistAlbumId : artist.getAlbumIds()) {
//            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
//                if (userAlbumId.getValue().equals(artistAlbumId)) {
//                    count++;
//                }
//            }
//        }
//        for (String artistAlbumId : artist.getSingleIds()) {
//            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
//                if (userAlbumId.getValue().equals(artistAlbumId)) {
//                    count++;
//                }
//            }
//        }
//        for (String artistAlbumId : artist.getCompilationIds()) {
//            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
//                if (userAlbumId.getValue().equals(artistAlbumId)) {
//                    count++;
//                }
//            }
//        }
//
//        String artistKey = null;
//        // There's no longer any saved albums from the artist
//        if (count == 1) {
//            if (user.getArtistIds().containsValue(artist.getId())) {
//                for (Map.Entry<String, String> artistId : user.getArtistIds().entrySet()) {
//                    if (artist.getId().equals(artistId.getValue())) {
//                        artistKey = artistId.getKey();
//                    }
//                }
//            } else {
//                log.e("Artist not previously saved to user. Aborting...");
//                return HeartResponse.ARTIST_NOT_FOUND;
//            }
//
//            // Remove the artist from the database user
//            updates.put("users/" + firebaseUser.getUid() + "/artistIds/" + artistKey, null);
////            db.child("users").child(firebaseUser.getUid()).child("artistIds").child(key).removeValue();
////            log.i(String.format("\"%s : %s\" removed from users/%s/artistIds", key, album.getId(), firebaseUser.getUid()));
//            db.child("albums").child(album.getId()).child("followers").setValue(ServerValue.increment(-1));
////            updates.put("albums/" + album.getId() + "/followers", ServerValue.increment(-1));
//
//            CountDownLatch cdl = new CountDownLatch(1);
//            final int[] followers = {0};
//            db.child("albums").child(album.getId()).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    followers[0] = snapshot.getValue(Integer.class);
//                    cdl.countDown();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//            try {
//                cdl.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (followers[0] <= 0) {
//                updates.put("albums/" + album.getId() + "/followers", 0);
//                updates.put("albums/" + album.getId() + "/followersKnown", false);
//                updates.put("albums/" + album.getId() + "/tracksIds", null);
//                updates.put("albums/" + album.getId() + "/tracksIdsKnown", false);
//            }
//
//
//        }

        String artistKey = null;
        if (user.getArtistIds().containsValue(album.getArtistId())) {
            for (Map.Entry<String, String> artistId : user.getArtistIds().entrySet()) {
                if (album.getArtistId().equals(artistId.getValue())) {
                    artistKey = artistId.getKey();
                }
            }
        }
        // Check the database artist to see if it has enough followers to live
        Artist artist = FirebaseService.checkDatabase(db, "artists", album.getArtistId(), Artist.class);

        if (artist != null) {
            List<String> albumIds = new ArrayList<>();

            if (artist.getAlbumIds() != null) {
                albumIds.addAll(artist.getAlbumIds());
            }
            if (artist.getSingleIds() != null) {
                albumIds.addAll(artist.getSingleIds());
            }
            if (artist.getCompilationIds() != null) {
                albumIds.addAll(artist.getCompilationIds());
            }

            int albumCount = 0;

            for (String albumId : albumIds) {
                for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
                    if (albumId.equals(userAlbumId.getValue())) {
                        albumCount++;
                    }
                }
            }

            if (albumCount == 1 && artist.getFollowers() <= 1) {
                if (artist.getAlbumIds().size() > 0) {
                    for (String albumId : artist.getAlbumIds()) {
                        updates.put("albums/" + albumId, null);
//                    db.child("albums").child(albumId).removeValue();
                    }
                }
                if (artist.getSingleIds().size() > 0) {
                    for (String albumId : artist.getSingleIds()) {
                        updates.put("albums/" + albumId, null);

//                    db.child("albums").child(albumId).removeValue();
                    }
                }
                if (artist.getCompilationIds().size() > 0) {
                    for (String albumId : artist.getCompilationIds()) {
                        updates.put("albums/" + albumId, null);
//                    db.child("albums").child(albumId).removeValue();
                    }
                }

                updates.put("artists/" + artist.getId(), null);
                db.child("users").child(firebaseUser.getUid()).child("artistIds").child(artistKey).removeValue();
//                updates.put("users/" + firebaseUser.getUid() + "/artistIds/" + artistKey, null);
//            db.child("artists").child(artist.getId()).removeValue();
            } else if (albumCount > 1) {
                db.child("artists").child(artist.getId()).child("followers").setValue(ServerValue.increment(-1));
//            updates.put("artists/" + artist.getId() + "/followers", ServerValue.increment(-1));
            }
        } else {
            return HeartResponse.ARTIST_NOT_FOUND;
        }

        // Remove the album from the database
        db.child("users").child(firebaseUser.getUid()).child("albumIds").child(albumKey).removeValue();
//        log.i(String.format("%s removed from /albums", album.getId()));
        db.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hidePopUp.run();
            }
        });
        log.i(String.format("\"%s : %s\" removed from users/%s/albumIds", albumKey, album.getId(), firebaseUser.getUid()));

        return HeartResponse.OK;
    }

    public static void showError(HeartResponse response, Context context) {
        Toast toast = null;
        switch (response) {
            //TODO: Handle Errors
            default:
                toast = Toast.makeText(context, "Encountered an error while hearting, try again later.", Toast.LENGTH_LONG);
                break;
        }
        toast.show();

    }

}
