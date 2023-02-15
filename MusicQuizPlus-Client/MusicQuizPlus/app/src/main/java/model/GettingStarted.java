package model;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.musicquizplus.ParentOfFragments;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.item.Album;
import model.item.Artist;
import service.FirebaseService;
import service.SpotifyService;
import service.firebase.AlbumService;
import utils.LogUtil;

public class GettingStarted {

    //#region Members
    private Map<Integer, List<Artist>> decadeArtistMap;
    private Map<Integer, Boolean> selectedDecades;
    private Map<String, Artist> artists;
    private Map<String, Artist> selectedArtists;
    private User user;
    private boolean areAllSelected;
    private int numDecadesSelected;
    //#endregion

    //#region Constants
    public static final List<String> DIFFICULTY_NAMES = new ArrayList<>() {
        {
            add("Easy");
            add("Medium");
            add("Hard");
        }
    };
    public static final List<String> DIFFICULTY_DESCRIPTIONS = new ArrayList<>() {
        {
            add("On easy difficulty, 100% of the least popular tracks will be filtered out first.");
            add("On medium difficulty, 50% of the least popular tracks will be filtered out first.");
            add("On hard difficulty, 0% of the least popular tracks will be filtered out.");
        }
    };
    private final static String TAG = "GettingStarted.java";
    //#endregion

    public GettingStarted(User user, DatabaseReference db) {
        this.user = user;
        decadeArtistMap = new HashMap<>();
        selectedDecades = new HashMap<>();
        selectedArtists = new HashMap<>();
        init(db);
    }

    //#region Accessors
    // Call after decades are selected
    public Map<String, Artist> getArtists() {
        artists = new HashMap<>();
        for (Map.Entry<Integer, List<Artist>> entry : this.decadeArtistMap.entrySet()) {
            if (selectedDecades.get(entry.getKey()) || numDecadesSelected == 0) {
                for (Artist artist : entry.getValue()) {
                    if (!artists.containsValue(artist)) {
                        artists.put(artist.getId(), artist);
                    }
                }
            }
        }

        return artists;
    }

    // Populate a list of available decades
    public List<Integer> getDecades() {
        List<Integer> decadesList = new ArrayList<>();

        for (Map.Entry<Integer, List<Artist>> entry : decadeArtistMap.entrySet()) {
            if (decadesList.size() == 0) {
                decadesList.add(entry.getKey());
            } else {

                int index = decadesList.size();
                for (int i = 0; i < decadesList.size(); i++) {
                    if (entry.getKey() < decadesList.get(i)) {
                        index = decadesList.indexOf(decadesList.get(i));
                        break;
                    }
                }
                decadesList.add(index, entry.getKey());
            }
        }
        return decadesList;
    }
    //#endregion

    //#region Data initialization
    private void init(DatabaseReference db) {
        retrieveArtists(db);
        for (Map.Entry<Integer, List<Artist>> entry : decadeArtistMap.entrySet()) {
            selectedDecades.put(entry.getKey(), false);
        }
        numDecadesSelected = 0;
    }

    private void retrieveArtists(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "retrieveArtists");
        decadeArtistMap = new HashMap<>();
        CountDownLatch cdl = new CountDownLatch(1);
        Query query = db.child("artists").orderByChild("followers").limitToFirst(50);
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Artist artist = (Artist) snapshot.getValue(Artist.class);
                    for (Integer decade : artist.getDecades()) {
                        if (decadeArtistMap.size() == 0 || decadeArtistMap.get(decade) == null || decadeArtistMap.get(decade).size() == 0) {
                            decadeArtistMap.put(decade, new ArrayList<>());
                        }
                        decadeArtistMap.get(decade).add(artist);
                    }
                }
                cdl.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                log.w(String.format("Unable to obtain the 50 artists: %s", error));
            }
        });
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //#endregion

    //#region Methods
    public void selectDecade(int decade) {
        boolean selected = selectedDecades.get(decade);
        if (selected) {
            numDecadesSelected--;
        } else {
            numDecadesSelected++;
        }

        selectedDecades.put(decade, !selected);
        updateSelectAll();
    }

    public void selectAllDecades() {
        Map<Integer, Boolean> selected = new HashMap<>();
        if (areAllSelected) {
            for (Map.Entry<Integer, Boolean> entry : selectedDecades.entrySet()) {
                selected.put(entry.getKey(), false);
            }
            selectedDecades = selected;
            numDecadesSelected = 0;

        } else {
            for (Map.Entry<Integer, Boolean> entry : selectedDecades.entrySet()) {
                selected.put(entry.getKey(), true);
            }
            selectedDecades = selected;
            numDecadesSelected = decadeArtistMap.size();
        }
        areAllSelected = !areAllSelected;
    }

    private void updateSelectAll() {
        if (numDecadesSelected == decadeArtistMap.size() && !areAllSelected) {
            areAllSelected = true;
        } else if (numDecadesSelected < decadeArtistMap.size() && areAllSelected) {
            areAllSelected = false;
        }
    }

    public void selectArtist(String artistId) {
        if (selectedArtists.containsKey(artistId)) {
            selectedArtists.remove(artistId);
        } else {
            selectedArtists.put(artistId, artists.get(artistId));
        }
    }

    public Intent finished(DatabaseReference db, FirebaseUser firebaseUser, SpotifyService spotifyService, Context context) {
        //add the selected artists to the user
        DatabaseReference userRef = db.child("users").child(firebaseUser.getUid());
        for (Map.Entry<String, Artist> artist : selectedArtists.entrySet()) {
            String randomId = artist.getValue().getRandomId();
            Album album = FirebaseService.checkDatabase(db, "albums", randomId, Album.class);
            AlbumService.heart(user, firebaseUser, db, album, spotifyService);
        }
        return new Intent(context, ParentOfFragments.class);
    }
    //#endregion
}