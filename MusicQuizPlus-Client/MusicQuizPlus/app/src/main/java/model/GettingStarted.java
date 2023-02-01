package model;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.musicquizplus.ParentOfFragments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import model.type.Difficulty;

public class GettingStarted {

    private final short maxYear;
    private short minYear;
    private String[] decadesToSelect;
    private List<String> selectedDecades;
    private List<String> selectedArtists;
    private User user;
    private Difficulty currentDifficulty;
    private boolean areAllSelected;

    public GettingStarted()
    {
        maxYear = (short) Calendar.getInstance().get(Calendar.YEAR);
        selectedArtists = new ArrayList<>();
        selectedDecades = new ArrayList<>();
        user = new User();
    }

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

    public void createDecades() {
        int arraySize = ((maxYear - minYear) / 10) + 1;
        decadesToSelect = new String[arraySize];
        String strYear;
        short year = minYear;
        int index = 0;

        while ( year <= maxYear)
        {
            strYear = String.valueOf(year);
            decadesToSelect[index] = strYear;
            year += 10;
            index++;
        }
    }

    public void addToList(String itemToAdd, String listToAddTo)
    {
        switch (listToAddTo)
        {
            case "decadesList":
                selectedDecades.add(itemToAdd);
            case "artistsList":
                selectedArtists.add(itemToAdd);
        }
    }

    public void removeFromList(String itemToRemove, String listToRemoveFrom)
    {
        switch (listToRemoveFrom)
        {
            case "decadesList":
                selectedDecades.remove(itemToRemove);
            case "artistsList":
                selectedArtists.remove(itemToRemove);
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

    public short getMinYear(){
        return minYear;
    }

    public short getMaxYear(){
        return maxYear;
    }

    public List<String> getSelectedDecades() {
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


/*


Methods

GetArtists() - get suggested artists from our database, derive the min and max era from each object
Sort() - this method will move artists from selected decades to the front of the list.


Completed Methods

CreateDecades() - create the list of decades based off min and max year
Add() - adds user selected decades or artists to their respective lists
Remove() - removes from user selected lists
SetDifficulty() - update difficulty selection
Finished() - send the selected artists to the database, save difficulty setting, navigate to playlists
SelectAll() - selects/unselects all decades


 */