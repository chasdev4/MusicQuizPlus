package service;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.GridView;

import androidx.annotation.NonNull;

import com.example.musicquizplus.CustomAdapter;
import com.example.musicquizplus.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import model.item.Playlist;

public class FirebaseService {

    public static void createUser(FirebaseUser firebaseUser, FirebaseFirestore firestore, DatabaseReference db) {
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

    public static void retrieveData(GridView gridView, Context context, String dbChild) {
        List<Playlist> itemsList = new ArrayList<>();

        CustomAdapter customAdapter = new CustomAdapter(context, R.layout.gridview_contents, itemsList);
        gridView.setAdapter(customAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(dbChild);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();

                String description = null;
                String id = null;
                String name = null;
                String owner = null;
                String photoUrl = null;

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    for (DataSnapshot dss : dataSnapshot.getChildren())
                    {
                        String key = dss.getKey();

                        if(Objects.equals(key, "_description"))
                        {
                            description = dss.getValue().toString();
                        }
                        else if(Objects.equals(key, "_id"))
                        {
                            id = dss.getValue().toString();
                        }
                        else if(Objects.equals(key, "_name"))
                        {
                            name = dss.getValue().toString();
                        }
                        else if(Objects.equals(key, "_owner"))
                        {
                            owner = dss.getValue().toString();
                        }
                        else if(Objects.equals(key, "_photoUrl"))
                        {
                            for (DataSnapshot photoUrlSnapshot : dss.getChildren())
                            {
                                String uriKey = photoUrlSnapshot.getKey();

                                if(Objects.equals(uriKey, "0"))
                                {
                                    for (DataSnapshot urlSnapshot : photoUrlSnapshot.getChildren())
                                    {
                                        String UrlKey = urlSnapshot.getKey();

                                        if(Objects.equals(UrlKey, "url"))
                                        {
                                            photoUrl = urlSnapshot.getValue().toString();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Playlist playlistToAdd = new Playlist(id, name, null, owner, description, photoUrl);
                    itemsList.add(playlistToAdd);
                }
                customAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
