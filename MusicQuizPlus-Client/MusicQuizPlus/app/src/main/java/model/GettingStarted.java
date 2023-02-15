package model;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.musicquizplus.ParentOfFragments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import service.FirebaseService;
import utils.LogUtil;

/*

CreateDecades() - create the list of decades based off min and max year
Add() - adds user selected decades or artists to their respective lists
Remove() - removes from user selected lists
SetDifficulty() - update difficulty selection
Finished() - send the selected artists to the database, save difficulty setting, navigate to playlists
SelectAll() - selects/unselects all decades
GetArtists() - get suggested artists from our database, derive the min and max era from each object
Sort() - this method will move artists from selected decades to the front of the list.

 */

public class GettingStarted {

    private Map<Integer, List<Artist>> artists;

    private final int maxYear;
    private int minYear;
    public Integer[] decadesToSelect;
    private List<Integer> selectedDecades;
    private List<String> selectedArtists;
    private List<Artist> artistsToSelect;
    private User user;
    private Difficulty currentDifficulty;
    private boolean areAllSelected;

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
            add("On easy difficulty, 100% of the least popular tracks will be filtered out.");
            add("On medium difficulty, 50% of the least popular tracks will be filtered out.");
            add("On hard difficulty, 0% of the least popular tracks will be filtered out.");
        }
    };
    private final static String TAG = "GettingStarted.java";
    //#endregion

    public GettingStarted(User user, DatabaseReference db)
    {
        this.maxYear = (short) Calendar.getInstance().get(Calendar.YEAR);
        this.selectedArtists = new ArrayList<>();
        this.selectedDecades = new ArrayList<>();
        this.artistsToSelect = new ArrayList<>();
        this.user = user;
        init(db);
    }

    private void init(DatabaseReference db) {
        retrieveArtists(db);
    }

    public Map<Integer, List<Artist>> getArtists() {
        return artists;
    }

    private void retrieveArtists(DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "retrieveArtists");
        artists = new HashMap<>();
        CountDownLatch cdl = new CountDownLatch(1);
        Query query = db.child("artists").orderByChild("followers").limitToFirst(50);
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Artist artist = (Artist) snapshot.getValue(Artist.class);
                    for (Integer decade : artist.getDecades()) {
                        if (artists.size() == 0 || artists.get(decade) == null || artists.get(decade).size() == 0) {
                            artists.put(decade, new ArrayList<>());
                        }
                        artists.get(decade).add(artist);
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



    public void createDecades() {
        int arraySize = ((maxYear - minYear) / 10) + 1;
        decadesToSelect = new Integer[arraySize];
        int year = minYear;
        int index = 0;

        while (year <= maxYear)
        {
            decadesToSelect[index] = year;
            year += 10;
            index++;
        }
    }

    public void addToList(Object itemToAdd, String listToAddTo)
    {
        switch (listToAddTo)
        {
            case "decadesList":
                selectedDecades.add((Integer) itemToAdd);
            case "artistsList":
                selectedArtists.add((String) itemToAdd);
        }
    }

    public void removeFromList(Object itemToRemove, String listToRemoveFrom)
    {
        switch (listToRemoveFrom)
        {
            case "decadesList":
                selectedDecades.remove((Integer) itemToRemove);
            case "artistsList":
                selectedArtists.remove((String) itemToRemove);
        }
    }

    public void selectAllDecades()
    {
        if(areAllSelected)
        {
            selectedDecades.clear();
        }
        else
        {
            selectedDecades.clear();
            selectedDecades.addAll(Arrays.asList(decadesToSelect));
        }
        areAllSelected = !areAllSelected;
    }

    public Intent finished(DatabaseReference db, FirebaseUser firebaseUser, Context context)
    {
        //add the selected artists to the user
        for (String artist : selectedArtists) {
            String key = db.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
            user.addArtistId(key, artist);
        }

        //send the selected artists to the database, save difficulty setting to database
        updateUser(db, firebaseUser, context);

        // navigate to playlists
        return new Intent(context, ParentOfFragments.class);
    }

    private List<Artist> sort()
    {
        List<Artist> sortedList = new ArrayList<>();
        int size = selectedDecades.size();

        for(int i = size; i > 0; i--)
        {
            for(Artist artist : artistsToSelect)
            {
                if(selectedDecades.contains(artist.getSortedDecades().get(0)))
                {
                    sortedList.add(artist);
                }
            }
        }
        return sortedList;
    }

    public List<Artist> getArtists(DatabaseReference db)
    {
        final String TAG = "GettingStarted.java";
        LogUtil log = new LogUtil(TAG, "getArtists");
        CountDownLatch cdl = new CountDownLatch(1);
        Query query = db.child("artists").orderByChild("followers").limitToFirst(50);
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    artistsToSelect.add((Artist) snapshot.getValue(Artist.class));
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

        return sort();
    }

    private void updateUser(DatabaseReference db, FirebaseUser firebaseUser, Context context)
    {
        User updatedUser = new User(user);

        db.child("users").child(firebaseUser.getUid()).setValue(updatedUser).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User has been updated..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update the user data..", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setMinYear(short minYear) {
        this.minYear = minYear;
    }

    public int getMinYear(){
        return minYear;
    }

    public int getMaxYear(){
        return maxYear;
    }

    public List<Integer> getSelectedDecades() {
        return selectedDecades;
    }

    public void setDifficulty(Difficulty difficulty)
    {
        user.setDifficulty(difficulty);
        currentDifficulty = difficulty;
    }

    public Difficulty getDifficulty()
    {
        return currentDifficulty;
    }

    public void setAreAllSelected(boolean areAllSelected)
    {
        this.areAllSelected = areAllSelected;
    }

    public boolean getAreAllSelected()
    {
        return areAllSelected;
    }
}