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
import utils.LogUtil;
import utils.ValidationUtil;

// SUMMARY
// Static methods for Firebase management

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
}
