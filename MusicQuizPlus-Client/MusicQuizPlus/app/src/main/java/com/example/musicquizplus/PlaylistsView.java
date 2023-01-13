package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Objects;

import service.FirebaseService;

public class PlaylistsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists_view);

        GridView gridView = findViewById(R.id.playlistGridView);
        TextView userLevel = findViewById(R.id.userLevel);
        View noCurrentUser = findViewById(R.id.playlistNoCurrentUser);

        if(Objects.equals(userLevel.getText(), "GUEST"))
        {
            gridView.setVisibility(View.GONE);
            noCurrentUser.setVisibility(View.VISIBLE);
        }
        else
        {
            gridView.setVisibility(View.VISIBLE);
            noCurrentUser.setVisibility(View.GONE);
        }

        if(Objects.equals(gridView.getVisibility(), View.VISIBLE)) {
            FirebaseService.retrieveData(gridView, this, "sample_playlists");
        }

    }
}