package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Objects;

import service.FirebaseService;

public class ArtistsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_view);

        GridView gridView = findViewById(R.id.artistGridView);
        TextView userLevel = findViewById(R.id.userLevel);
        View noUser = findViewById(R.id.artistNoCurrentUser);

        if(Objects.equals(userLevel.getText(), "GUEST"))
        {
            gridView.setVisibility(View.GONE);
            noUser.setVisibility(View.VISIBLE);
        }
        else
        {
            gridView.setVisibility(View.VISIBLE);
            noUser.setVisibility(View.GONE);
        }

        if(Objects.equals(gridView.getVisibility(), View.VISIBLE)) {
            FirebaseService.retrieveData(gridView, this, "sample_artists");
        }
    }
}