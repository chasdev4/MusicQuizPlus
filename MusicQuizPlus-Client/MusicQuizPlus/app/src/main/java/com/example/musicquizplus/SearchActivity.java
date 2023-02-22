package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import model.GoogleSignIn;
import model.Search;
import model.SearchResult;
import model.TrackResult;
import model.User;
import model.item.Track;
import model.type.SearchFilter;
import service.FirebaseService;
import service.SpotifyService;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private RadioGroup searchFilters;
    private ImageButton backToTop;
    private Context context;
    private ImageView emptySearchImage;
    private TextView emptySearchText;
    private ProgressBar progressBar;

    private Search search;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;
    private SpotifyService spotifyService;

    private int offset;
    private String lastQuery;
    private boolean allSearch;
    private boolean searchStarted;
    private boolean doingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;

        searchStarted = false;
        doingSearch = false;

        progressBar = findViewById(R.id.search_progress_bar);
        progressBar.setVisibility(View.GONE);

        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        search = new Search();
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
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setImageDrawable(ContextCompat.getDrawable((Activity)this,R.drawable.search));
        ImageView searchCloseIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchCloseIcon.setImageDrawable(ContextCompat.getDrawable((Activity)this,R.drawable.close));

        emptySearchImage = findViewById(R.id.search_empty_image);
        emptySearchText = findViewById(R.id.search_empty_text);
        emptySearchImage.setVisibility(View.VISIBLE);
        emptySearchText.setVisibility(View.VISIBLE);

        backToTop = findViewById(R.id.backToTop);

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        recyclerView = findViewById(R.id.search_recycler_view);
        setupRecyclerView();
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int scroll = llm.findFirstVisibleItemPosition();

                if (scroll > 0) {
                    backToTop.setVisibility(View.VISIBLE);
                } else {
                    backToTop.setVisibility(View.GONE);
                }

            }
        });

        searchFilters = findViewById(R.id.search_filter_group);
        searchFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                boolean skip =false;
                List<SearchResult> results = new ArrayList<>();
                if (allSearch && i == R.id.search_filter_all) {
                    results = search.getAll();
                } else if (search.getCurrentFilter() == SearchFilter.ALL && i != R.id.search_filter_all || allSearch) {
                    allSearch = true;
                    switch (i) {
                        case R.id.search_filter_artist:
                            results = search.getArtists();
                            break;
                        case R.id.search_filter_album:
                            results = search.getAlbums();
                            break;
                        case R.id.search_filter_song:
                            results = search.getTracks();
                            break;
                        case R.id.search_filter_playlist:
                            results = search.getPlaylists();
                            break;
                    }
                }
                else if (search.getCurrentFilter() != SearchFilter.ALL && i == R.id.search_filter_all
                        || search.getCurrentFilter() != SearchFilter.ALL && searchView.getQuery().toString().equals(lastQuery)) {
                    search.setCurrentFilter(SearchFilter.ALL);
                    skip = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            doSearch(searchView.getQuery().toString());
                        }
                    }).start();
                }
                searchAdapter.setSearchResults(results);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        searchAdapter.notifyDataSetChanged();
                    }
                });

                if (!skip) {
                    switch (i) {
                        case R.id.search_filter_all:
                            search.setCurrentFilter(SearchFilter.ALL);
                            break;
                        case R.id.search_filter_artist:
                            search.setCurrentFilter(SearchFilter.ARTIST);
                            break;
                        case R.id.search_filter_album:
                            search.setCurrentFilter(SearchFilter.ALBUM);
                            break;
                        case R.id.search_filter_song:
                            search.setCurrentFilter(SearchFilter.SONG);
                            break;
                        case R.id.search_filter_playlist:
                            search.setCurrentFilter(SearchFilter.PLAYLIST);
                            break;
                    }
                }
            }
        });

        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        offset = 0;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseUser != null)
        {
            new Thread(new Runnable() {
                public void run() {
                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    searchAdapter.setUser(user);
                }
            }).start();
        }
    }

    public TrackResult getTrackResult(Track track) {
        return search.getTrackResult(track);
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchAdapter(context, new ArrayList<>());
        searchAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onDataChange();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(searchAdapter);
        onDataChange();
    }

    private void onDataChange() {
        if (searchStarted && !doingSearch) {
            if (searchAdapter.getItemCount() == 0) {
                recyclerView.setVisibility(View.INVISIBLE);
                emptySearchImage.setVisibility(View.VISIBLE);
                emptySearchText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptySearchImage.setVisibility(View.INVISIBLE);
                emptySearchText.setVisibility(View.INVISIBLE);
            }
        }
    }

    private <T> void doSearch(String query) {
        doingSearch = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                searchAdapter.setSearchResults(new ArrayList<>());
            }
        });
        if (!searchStarted) {
            searchStarted = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    emptySearchImage.setVisibility(View.INVISIBLE);
                    emptySearchText.setVisibility(View.INVISIBLE);
                    emptySearchImage.setImageResource(R.drawable.no_results);
                    emptySearchText.setText(R.string.search_no_results_text);
                }
            });

        }
        if (lastQuery != query) {
            allSearch = false;
        }
        lastQuery = query;
        SearchFilter lastFilter = search.getCurrentFilter();
        search = new Search(query, 100, spotifyService, lastFilter);
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
                results = search.getTracks();
                break;
            case PLAYLIST:
                results = search.getPlaylists();
                break;
        }
        searchAdapter.setSearchResults(results);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                searchAdapter.notifyDataSetChanged();
                doingSearch = false;
            }
        });
    }

    public Search getSearch() {
        return search;
    }
}