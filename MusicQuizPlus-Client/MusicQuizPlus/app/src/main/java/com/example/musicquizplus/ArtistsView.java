package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;

import service.FirebaseService;

public class ArtistsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_view);

        GridView gridView = findViewById(R.id.artistGridView);

        FirebaseService.retrieveData(gridView, this, "sample_artists");

    }
}