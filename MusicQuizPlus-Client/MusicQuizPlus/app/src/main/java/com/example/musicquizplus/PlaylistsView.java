package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists_view);

        List<GridViewItems> itemsList = new ArrayList<>();
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));
        itemsList.add(new GridViewItems(R.drawable.spotify_todays_hits, "Today's Top Hits"));

        GridView gridView = findViewById(R.id.playlistGridView);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.gridview_contents, itemsList);
        gridView.setAdapter(customAdapter);
    }
}