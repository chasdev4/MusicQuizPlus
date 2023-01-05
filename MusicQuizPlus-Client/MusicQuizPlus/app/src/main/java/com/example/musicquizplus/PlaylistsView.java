package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import model.PhotoUrl;
import model.item.Playlist;

public class PlaylistsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists_view);

        List<Playlist> itemsList = new ArrayList<>();
        /*
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
         */

        GridView gridView = findViewById(R.id.playlistGridView);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.gridview_contents, itemsList);
        gridView.setAdapter(customAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("sample_playlists");
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