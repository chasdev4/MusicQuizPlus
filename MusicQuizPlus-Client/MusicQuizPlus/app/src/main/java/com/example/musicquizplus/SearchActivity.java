package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import model.Search;
import service.SpotifyService;

public class SearchActivity extends AppCompatActivity {

    private Search search;
    private SpotifyService spotifyService;
    private int offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        offset = 0;

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void doSearch(String query) {
        search = new Search(query, 50, spotifyService);
        search.execute(offset);
    }
}