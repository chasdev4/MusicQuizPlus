package service;

import android.content.Context;
import android.widget.GridView;

import androidx.annotation.NonNull;

import com.example.musicquizplus.ArtistsAdapter;
import com.example.musicquizplus.HistoryAdapter;
import com.example.musicquizplus.PlaylistsAdapter;
import com.example.musicquizplus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.FirebaseDatabase;
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

import model.ValidationObject;
import model.item.Playlist;

import model.PhotoUrl;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Track;
import model.type.Severity;
import utils.FormatUtil;
import utils.LogUtil;
import utils.ValidationUtil;

public class FirebaseService {

    private final static String TAG = "FirebaseService.java";

    public static <T> T checkDatabase(DatabaseReference db, String child, String id, Class cls) {
        LogUtil log = new LogUtil(TAG, "checkDatabase");
        CountDownLatch done = new CountDownLatch(1);
        final User[] users = new User[1];
        final Album[] albums = new Album[1];
        final Artist[] artists = new Artist[1];
        final Playlist[] playlists = new Playlist[1];
        final Track[] tracks = new Track[1];
        db.child(child).child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                log.v(String.format("Attempting to retrieve /%s/%s from database.", child, id));
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
                        log.w(String.format("checkDatabase: unsupported class %s.", cls.getSimpleName()));
                        break;
                }
                done.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                log.e(error.getMessage());
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
                log.w(String.format("checkDatabase: unsupported class %s.", cls.getSimpleName()));
                break;
        }


        return null;
    }



    public static void retrieveData(GridView gridView, Context context, String dbChild, Class cls) {
        LogUtil log = new LogUtil(TAG, "retrieveData");
        String className = cls.getSimpleName();

        List<Playlist> playlists = new ArrayList<>();
        List<Artist> artists = new ArrayList<>();
        List<Track> history = new ArrayList<>();

        //CustomAdapter customAdapter = null;
        PlaylistsAdapter playlistsAdapter = null;
        ArtistsAdapter artistsAdapter = null;
        HistoryAdapter historyAdapter = null;

        switch (cls.getSimpleName()) {
            case "Artist":
                artistsAdapter = new ArtistsAdapter(context, R.layout.gridview_contents, artists);
                gridView.setAdapter(artistsAdapter);
                break;
            case "Playlist":
                playlistsAdapter = new PlaylistsAdapter(context, R.layout.gridview_contents, playlists);
                gridView.setAdapter(playlistsAdapter);
                break;
            case "Track":
                historyAdapter = new HistoryAdapter(context, R.layout.gridview_contents, history);
                gridView.setAdapter(historyAdapter);
                break;
            default:
                log.w(String.format("Unsupported class %s.", cls.getSimpleName()));
                return;
        }


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(dbChild);
        ArtistsAdapter finalArtistsAdapter = artistsAdapter;
        PlaylistsAdapter finalPlaylistsAdapter = playlistsAdapter;
        HistoryAdapter finalHistoryAdapter = historyAdapter;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    switch (className) {
                        case "Artist":
                            Artist artist = (Artist)dataSnapshot.getValue(cls);
                            artists.add(artist);
                            break;
                        case "Playlist":
                            Playlist playlist = (Playlist)dataSnapshot.getValue(cls);
                            playlists.add(playlist);
                            //finalPlaylistsAdapter.notifyDataSetChanged();
                            break;
                        case "Track":
                            Track track = (Track)dataSnapshot.getValue(cls);
                            history.add(track);
                            break;
                    }
                }

                switch (className) {
                    case "Artist":
                        finalArtistsAdapter.notifyDataSetChanged();
                        break;
                    case "Playlist":
                        finalPlaylistsAdapter.notifyDataSetChanged();
                        break;
                    case "Track":
                        finalHistoryAdapter.notifyDataSetChanged();
                        break;
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });















//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(dbChild);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                //itemsList.clear();
//
//                String description = null;
//                String id = null;
//                String name = null;
//                String owner = null;
//                String photoUrl = null;
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren())
//                {
//                    for (DataSnapshot dss : dataSnapshot.getChildren())
//                    {
//                        String key = dss.getKey();
//
//                        if(Objects.equals(key, "_description"))
//                        {
//                            description = Objects.requireNonNull(dss.getValue()).toString();
//                        }
//                        else if(Objects.equals(key, "id"))
//                        {
//                            id = Objects.requireNonNull(dss.getValue()).toString();
//                        }
//                        else if(Objects.equals(key, "name"))
//                        {
//                            name = Objects.requireNonNull(dss.getValue()).toString();
//                        }
//                        else if(Objects.equals(key, "_owner"))
//                        {
//                            owner = Objects.requireNonNull(dss.getValue()).toString();
//                        }
//                        else if(Objects.equals(key, "photoUrl"))
//                        {
//                            for (DataSnapshot photoUrlSnapshot : dss.getChildren())
//                            {
//                                String uriKey = photoUrlSnapshot.getKey();
//
//                                if(Objects.equals(uriKey, "0"))
//                                {
//                                    for (DataSnapshot urlSnapshot : photoUrlSnapshot.getChildren())
//                                    {
//                                        String UrlKey = urlSnapshot.getKey();
//
//                                        if(Objects.equals(UrlKey, "url"))
//                                        {
//                                            photoUrl = Objects.requireNonNull(urlSnapshot.getValue()).toString();
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    String finalPhotoUrl = photoUrl;
//                    Playlist playlistToAdd = new Playlist(
//                            id,
//                            name,
//                            new ArrayList<PhotoUrl>() {{
//                            add(new PhotoUrl(finalPhotoUrl, 0, 0));
//                        }},
//                            owner,
//                            description);
//                    itemsList.add(playlistToAdd);
//                }
//
//                customAdapter.notifyDataSetChanged();
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    // Create a new user on both databases
    public static void createUser(FirebaseUser firebaseUser, FirebaseFirestore firestore,
                                  DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "createUser");
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
                        log.d("DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        log.w("Error writing document", e);
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
        LogUtil log = new LogUtil(TAG, "deleteUser");
        final boolean[] result = {true};

        db.child("users").child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    log.d("User data deleted from Realtime Database.");
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
                            log.d("Firestore user data successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            log.w("Error deleting Firestore user", e);
                        }
                    });
        }
        return result[0];
    }

    // When the user "hearts" an album
    public static void heartAlbum(User user, FirebaseUser firebaseUser, DatabaseReference db, Album album,
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
            log.i(String.format("%s added to the artistIds list.", artistId));
        }
        else {
            log.i(String.format("%s already exists in the artistIds list.", artistId));
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
            log.i(String.format("Tracks from %s saved to database child \"tracks\"", album.getId()));
        }
        else {
            log.i(String.format("Tracks from %s have previously been saved to database", album.getId()));
        }
        updates.put("albums/"+album.getId()+"/followers", ServerValue.increment(1));
        if (!album1.isFollowersKnown()) {
            updates.put("albums/"+album.getId()+"/followersKnown", true);
        }
        updates.put("artists/"+artistId+"/followers", ServerValue.increment(1));
        updates.put("artists/"+artistId+"/followersKnown", true);

        db.updateChildren(updates);
    }

    private static void saveArtistOverview(String artistId, Album album, DatabaseReference db, SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "saveArtistOverview");
        log.i("Fetching artist overview for " + artistId);

        // Get the artist overview from the Spotify API
        Artist artist = spotifyService.artistOverview(artistId);
        log.i(String.format("Artist Overview for \"%s\" %s retrieved.",
                artist.getName(),artist.getId()));

        // Save the artist to the database
        db.child("artists").child(artist.getId()).setValue(artist);
        log.i(String.format("%s saved to database child \"artists\"", artist.getId()));

        // Save each album to the database
        createAlbums(artist.getAlbums(), db, spotifyService);
        createAlbums(artist.getSingles(), db, spotifyService);
        createAlbums(artist.getCompilations(), db, spotifyService);
    }

    private static void createAlbums(List<Album> albums, DatabaseReference db, SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "createAlbums");
        for (Album a : albums) {
            db.child("albums").child(a.getId()).setValue(a);
        }
        log.i(String.format("%s %sS saved to database child \"albums\"",
                albums.size(), albums.get(0).getType()));
    }

    private static void saveAlbumTracks(Album album, DatabaseReference db, SpotifyService spotifyService) {
        JsonObject jsonObject = spotifyService.albumTracks(album.getId());
        JsonArray jsonArray = jsonObject
                .getAsJsonObject("data")
                .getAsJsonObject("album")
                .getAsJsonObject("tracks")
                .getAsJsonArray("items");


        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonTrack = jsonArray.get(i).getAsJsonObject().getAsJsonObject("track");

            JsonArray artistsArray = jsonTrack.get("artists").getAsJsonObject().get("items").getAsJsonArray();
            String artistId = artistsArray.get(0).getAsJsonObject().get("uri").getAsString();
            Map<String, String> artistsMap = new HashMap<>();
            for (int j = 0; j < artistsArray.size(); j++) {
                artistsMap.put(artistsArray.get(j).getAsJsonObject().get("uri").getAsString(),
                        artistsArray.get(j).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString());
            }

            Track track = new Track(
                    jsonTrack.get("uri").getAsString(),
                    jsonTrack.get("name").getAsString(),
                    album.getId(),
                    album.getName(),
                    artistId,
                    artistsMap,
                    0,
                    false,
                    null,
                    true,
                    album.getYear(),
                    jsonTrack.getAsJsonObject("playability").get("playable").getAsBoolean());
            album.addTrackId(track.getId());
            db.child("tracks").child(track.getId()).setValue(track);
        }
        album.setTrackIdsKnown(true);
        db.child("albums").child(album.getId()).child("trackIdsKnown").setValue(true);
        db.child("albums").child(album.getId()).child("trackIds").setValue(album.getTrackIds());
    }

    public static void unheartAlbum(User user, FirebaseUser firebaseUser, DatabaseReference db, Album album,
                                    SpotifyService spotifyService) {
        LogUtil log = new LogUtil(TAG, "unheartAlbum");

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
                Track track = checkDatabase(db, "tracks", trackId, Track.class);
                if (track != null) {
                    // Check to see if it's safe to delete, the track may belong to a saved playlist
                    if (track.isAlbumKnown() && track.getPlaylistIds() == null) {
                        db.child("tracks").child(trackId).removeValue();
                        log.i(String.format("%s removed from database child /tracks", trackId));
                    }
                    else {
                        log.i(String.format("%s belongs to one or more playlists.", trackId));
                    }
                }
            }


            // Attempt to remove the artist from the user
            key = user.removeArtistId(album.getArtistIds().get(0));

            // If the artist wasn't found, abort
            if (key == null) {
                log.w("Artist not previously saved to user. Aborting...");
                return;
            }

            // Remove the artist from the database user
            db.child("users").child(firebaseUser.getUid()).child("artistIds").child(key).removeValue();
            log.i(String.format("\"%s : %s\" removed from users/%s/artistIds", key, album.getId(), firebaseUser.getUid()));

            // If the album's artist is on it's last follower, remove it and it's albums from the database.
            Artist artist = checkDatabase(db, "artists", album.getArtistIds().get(0), Artist.class);
            if (artist.getFollowers() <= 1 && artist.isFollowersKnown()) {
                for (String albumId : artist.getAlbumIds()) {
                    db.child("albums").child(albumId).removeValue();
                }
                for (String albumId : artist.getSingleIds()) {
                    db.child("albums").child(albumId).removeValue();
                }
                for (String albumId : artist.getCompilationIds()) {
                    db.child("albums").child(albumId).removeValue();
                }

                db.child("artists").child(artist.getId()).removeValue();
            }
            // The artist is still followed, update the entries instead
            else {
                updates.put("albums/"+album.getId()+"/followers", 0);
                updates.put("albums/"+album.getId()+"/followersKnown", false);
                updates.put("albums/"+album.getId()+"/tracksIds", null);
                updates.put("albums/"+album.getId()+"/tracksIdsKnown", false);
                updates.put("artists/"+artist.getId()+"/followers", ServerValue.increment(-1));
                db.updateChildren(updates);
                log.i(String.format("%s follower count has decremented.", album.getId()));
                log.i(String.format("%s follower count has decremented.", artist.getId()));
            }

            // Remove the album from the database
            db.child("albums").child(album.getId()).removeValue();
            log.i(String.format("%s removed from /albums", album.getId()));

        }
        // Else the album has enough followers to live
        else {
            // Decrement the follower count
            updates.put("albums/"+album.getId()+"/followers", ServerValue.increment(-1));
            db.updateChildren(updates);
            log.i(String.format("%s follower count has decremented.", album.getId()));
        }


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
        LogUtil log = new LogUtil(TAG, "populatePlaylistTracks");
        // Playlist is already populated, do nothing
        if (playlist.getTracks().size() > 0) {
            return playlist;
        }

        // Check the database for the playlist
        Playlist playlist1 = checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

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
                Track track = checkDatabase(db, "tracks", trackId, Track.class);

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
                    playlist.addTrack(track);
                }
            }
            else {
                log.i(String.format("Track #%s in items was null", String.valueOf(i)));
            }

        }

        playlist.setAveragePopularity(popularity / numTracks);
        return playlist;
    }

    // When the user "hearts" a playlist
    public static void heartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
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
        Playlist playlist1 = checkDatabase(db, "playlists", playlist.getId(), Playlist.class);

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
        for (Track t : playlist.getTracks()) {
            db.child("tracks").child(t.getId()).setValue(t);
        }

        // DEBUG: Uncomment me to create a sample_history table
        // db.child("sample_history").setValue(playlist.getTrackIds());

    }

    public static void unheartPlaylist(User user, FirebaseUser firebaseUser, DatabaseReference db, Playlist playlist,
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
                Track track = checkDatabase(db, "tracks", trackId, Track.class);
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
