package service.firebase;

import androidx.annotation.NonNull;

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

import model.User;
import model.ValidationObject;
import model.item.Album;
import model.item.Artist;
import model.item.Track;
import model.type.AlbumType;
import model.type.Severity;
import service.FirebaseService;
import service.SpotifyService;
import utils.LogUtil;
import utils.ValidationUtil;

public class AlbumService {
    private final static String TAG = "AlbumService.java";

    // When the user "hearts" an album
    public static void heart(User user, FirebaseUser firebaseUser, DatabaseReference db, Album album,
                             SpotifyService spotifyService) {

        LogUtil log = new LogUtil(TAG, "heartAlbum");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(user, User.class, Severity.HIGH));
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(album, Album.class, Severity.HIGH));
                add(new ValidationObject(spotifyService, SpotifyService.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        // Add the albumId to the user
        String key = db.child("users").child(firebaseUser.getUid()).child("albumIds").push().getKey();
        boolean result = user.addAlbumId(key, album.getId());

        // If the album wasn't added, return
        if (!result) {
            log.w(String.format("%s already exists in albumIds list.", album.getId()));
            return;
        }

        // Save the albumId to the db user
        db.child("users")
                .child(firebaseUser.getUid())
                .child("albumIds")
                .child(key)
                .setValue(album.getId());

        // Add the artistId to the user
        String artistId = album.getArtistId();
        key = db.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
        result = user.addArtistId(key, artistId);

        // If the artist was added, add it to the db user
        if (result) {
            db.child("users")
                    .child(firebaseUser.getUid())
                    .child("artistIds")
                    .child(key)
                    .setValue(artistId);
            log.i(String.format("%s added to the artistIds list.", artistId));
        } else {
            log.i(String.format("%s already exists in the artistIds list.", artistId));
        }


        // Check to see if the artist exists in the database
        // If the artist exists, then there is no need to save their discography
        Artist artist = FirebaseService.checkDatabase(db, "artists", artistId, Artist.class);

        if (artist == null) {
            CountDownLatch cdl = new CountDownLatch(1);
            artist = saveArtistOverview(artistId, album, db, spotifyService);
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
            log.e("Album can't be saved because it isn't saved to artist.");
            return;
        }

        if (!album1.isTrackIdsKnown()) {
            // Save the hearted album's tracks to the database
            saveAlbumTracks(album, db, spotifyService);
            log.i(String.format("Tracks from %s saved to database child \"tracks\"", album.getId()));
        } else {
            log.i(String.format("Tracks from %s have previously been saved to database", album.getId()));
        }
        updates.put("albums/" + album.getId() + "/followers", ServerValue.increment(1));
        if (!album1.isFollowersKnown()) {
            updates.put("albums/" + album.getId() + "/followersKnown", true);
        }
        if (result) {
            updates.put("artists/" + artistId + "/followers", ServerValue.increment(1));
            updates.put("artists/" + artistId + "/followersKnown", true);
        }

        user.getArtists().put(album.getArtistId(), artist);
        db.updateChildren(updates);
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
        trackIds = trackIds.substring(0, trackIds.length()-3);
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

    public static void unheart(User user, FirebaseUser firebaseUser, DatabaseReference db, Album album) {
        LogUtil log = new LogUtil(TAG, "unheartAlbum");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(user, User.class, Severity.HIGH));
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
                add(new ValidationObject(album, Album.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        // Attempt to remove the album from the user
        String key = user.removeAlbumId(album.getId());

        // If the album wasn't found, abort
        if (key == null) {
            log.w("Album not previously saved to user. Aborting...");
            return;
        }

        // Remove the album from the database user
        db.child("users").child(firebaseUser.getUid()).child("albumIds").child(key).removeValue();
        log.i(String.format("\"%s : %s\" removed from users/%s/albumIds", key, album.getId(), firebaseUser.getUid()));

        Map<String, Object> updates = new HashMap<>();

        // If the album is on it's last follower or lower, remove it's tracks from the database
        if (album.getFollowers() <= 1 && album.isFollowersKnown()) {
            for (String trackId : album.getTrackIds()) {
                // Get the track from the database
                Track track = FirebaseService.checkDatabase(db, "tracks", trackId, Track.class);
                if (track != null) {
                    // Check to see if it's safe to delete, the track may belong to a saved playlist
                    if (track.isAlbumKnown() && track.getPlaylistIds() == null) {
                        db.child("tracks").child(trackId).removeValue();
                        log.i(String.format("%s removed from database child /tracks", trackId));
                    } else {
                        log.i(String.format("%s belongs to one or more playlists.", trackId));
                    }
                }
            }
        }

        // Check to see if the user has any other albums saved
        Artist artist = user.getArtist(album.getArtistId());
        int count = 0;

        for (String artistAlbumId : artist.getAlbumIds()) {
            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
                if (userAlbumId.getValue().equals(artistAlbumId)) {
                    count++;
                }
            }
        }
        for (String artistAlbumId : artist.getSingleIds()) {
            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
                if (userAlbumId.getValue().equals(artistAlbumId)) {
                    count++;
                }
            }
        }
        for (String artistAlbumId : artist.getCompilationIds()) {
            for (Map.Entry<String, String> userAlbumId : user.getAlbumIds().entrySet()) {
                if (userAlbumId.getValue().equals(artistAlbumId)) {
                    count++;
                }
            }
        }

        // There's no longer any saved albums from the artist
        if (count == 0) {
            // Attempt to remove the artist from the user
            key = user.removeArtistId(album.getArtistId());

            // If the artist wasn't found, abort
            if (key == null) {
                log.w("Artist not previously saved to user. Aborting...");
                return;
            }

            user.getArtists().remove(album.getArtistId());

            // Remove the artist from the database user
            db.child("users").child(firebaseUser.getUid()).child("artistIds").child(key).removeValue();
            log.i(String.format("\"%s : %s\" removed from users/%s/artistIds", key, album.getId(), firebaseUser.getUid()));
            updates.put("albums/" + album.getId() + "/followers", ServerValue.increment(-1));

            CountDownLatch cdl = new CountDownLatch(1);
            final int[] followers = {0};
            db.child("albums").child(album.getId()).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    followers[0] = snapshot.getValue(Integer.class);
                    cdl.countDown();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (followers[0] <= 0) {
                updates.put("albums/" + album.getId() + "/followers", 0);
                updates.put("albums/" + album.getId() + "/followersKnown", false);
                updates.put("albums/" + album.getId() + "/tracksIds", null);
                updates.put("albums/" + album.getId() + "/tracksIdsKnown", false);
            }

            db.updateChildren(updates);
            updates.clear();
        }

        // Check the database artist to see if it has enough followers to live
        artist = FirebaseService.checkDatabase(db, "artists", album.getArtistId(), Artist.class);

        if (artist.getFollowers() <= 1) {
            if (artist.getAlbumIds().size() > 0) {
                for (String albumId : artist.getAlbumIds()) {
                    db.child("albums").child(albumId).removeValue();
                }
            }
            if (artist.getSingleIds().size() > 0) {
                for (String albumId : artist.getSingleIds()) {
                    db.child("albums").child(albumId).removeValue();
                }
            }
            if (artist.getCompilationIds().size() > 0) {
                for (String albumId : artist.getCompilationIds()) {
                    db.child("albums").child(albumId).removeValue();
                }
            }

            db.child("artists").child(artist.getId()).removeValue();
        } else {
            updates.put("artists/" + artist.getId() + "/followers", ServerValue.increment(-1));
            db.updateChildren(updates);
        }


//            // If the album's artist is on it's last follower, remove it and it's albums from the database.
//            if (artist.getFollowers() <= 1) {
//                for (String albumId : artist.getAlbumIds()) {
//                    db.child("albums").child(albumId).removeValue();
//                }
//                for (String albumId : artist.getSingleIds()) {
//                    db.child("albums").child(albumId).removeValue();
//                }
//                for (String albumId : artist.getCompilationIds()) {
//                    db.child("albums").child(albumId).removeValue();
//                }
//
//                db.child("artists").child(artist.getId()).removeValue();
//            }
//            // The artist is still followed, update the entries instead
//            else {
//                updates.put("albums/" + album.getId() + "/followers", 0);
//                updates.put("albums/" + album.getId() + "/followersKnown", false);
//                updates.put("albums/" + album.getId() + "/tracksIds", null);
//                updates.put("albums/" + album.getId() + "/tracksIdsKnown", false);
//                updates.put("artists/" + artist.getId() + "/followers", ServerValue.increment(-1));
//                db.updateChildren(updates);
//                log.i(String.format("%s follower count has decremented.", album.getId()));
//                log.i(String.format("%s follower count has decremented.", artist.getId()));
//            }

        // Remove the album from the database
        db.child("albums").child(album.getId()).removeValue();
        log.i(String.format("%s removed from /albums", album.getId()));


    // Else the album has enough followers to live
//        else {
//            // Decrement the follower count
//            updates.put("albums/" + album.getId() + "/followers", ServerValue.increment(-1));
//            db.updateChildren(updates);
//            log.i(String.format("%s follower count has decremented.", album.getId()));
//        }


    }
}
