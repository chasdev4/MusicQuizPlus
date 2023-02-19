package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import model.Search;
import model.SearchResult;
import model.type.SearchFilter;
import service.SpotifyService;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private List<SearchResult> results;
    private RadioGroup searchFilters;
    private ImageButton backToTop;

    private Search search;
    private SpotifyService spotifyService;
    private int offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.search_bar);
        // Some devices automatically focus to the search view
        searchView.clearFocus();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.search_bar:
                        searchView.onActionViewExpanded();
                        break;
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doSearch(query);
                    }
                }).start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        backToTop = findViewById(R.id.backToTop);

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        recyclerView = findViewById(R.id.search_recycler_view);
        recyclerView.setAdapter(new SearchAdapter((Context) this, new ArrayList<>()));
        recyclerView.setVisibility(View.GONE);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int scroll = llm.findFirstVisibleItemPosition();

                if(scroll > 0)
                {
                    backToTop.setVisibility(View.VISIBLE);
                }
                else
                {
                    backToTop.setVisibility(View.GONE);
                }

            }
        });

        results = new ArrayList<>();

        searchFilters = findViewById(R.id.search_filter_group);
        searchFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.search_filter_all:
                        search.setCurrentFilter(SearchFilter.ALL);
                        Log.d("TAG", "onCheckedChanged: ALL");
                        break;
                    case R.id.search_filter_artist:
                        search.setCurrentFilter(SearchFilter.ARTIST);
                        Log.d("TAG", "onCheckedChanged: ARTIST");
                        break;
                    case R.id.search_filter_album:
                        search.setCurrentFilter(SearchFilter.ALBUM);
                        Log.d("TAG", "onCheckedChanged: ALBUM");
                        break;
                    case R.id.search_filter_song:
                        search.setCurrentFilter(SearchFilter.SONG);
                        Log.d("TAG", "onCheckedChanged: SONG");
                        break;
                    case R.id.search_filter_playlist:
                        search.setCurrentFilter(SearchFilter.PLAYLIST);
                        Log.d("TAG", "onCheckedChanged: PLAYLIST");
                        break;
                }
            }
        });

        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        offset = 0;

        // Get the intent, verify the action and get the query
//        Intent intent = getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            doSearch(query);
//        }
    }

    private <T> void doSearch(String query) {
        search = new Search(query, 50, spotifyService);
        search.execute(offset);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        List<SearchResult> results = new ArrayList<>();
       switch (search.getCurrentFilter()) {
           case ALL:
               results = search.getAll();
               break;
           case ARTIST:
               results = search.getArtists();
               break;
           case ALBUM:
               results = search.getAlbums();
               break;
           case SONG:
               results =  search.getTracks();
               break;
           case PLAYLIST:
               results =  search.getPlaylists();
               break;
       }
       recyclerView.setAdapter(new SearchAdapter(this, results));
       recyclerView.post(new Runnable() {
           @Override
           public void run() {
               recyclerView.getAdapter().notifyDataSetChanged();

           }
       });
    }
}