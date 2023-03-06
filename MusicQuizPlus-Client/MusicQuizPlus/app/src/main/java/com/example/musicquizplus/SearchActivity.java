package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import model.GoogleSignIn;
import model.Search;
import model.SearchResult;
import model.SignUpPopUp;
import model.TrackResult;
import model.User;
import model.item.Track;
import model.type.Role;
import model.type.SearchFilter;
import service.FirebaseService;
import service.SpotifyService;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private ImageButton backToTop;
    private Context context;
    private ImageView emptySearchImage;
    private TextView emptySearchText;
    private ProgressBar progressBar;
    private ImageButton homeButton;

    private Search search;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;
    private SpotifyService spotifyService;

    private int offset;
    private String lastQuery;
    private boolean allSearch;
    private boolean searchStarted;
    private boolean doingSearch;
    private boolean searchLimitReached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;

        homeButton = findViewById(R.id.home_button);


        searchStarted = false;
        doingSearch = false;

        progressBar = findViewById(R.id.search_progress_bar);
        progressBar.setVisibility(View.GONE);

        GoogleSignIn googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        search = new Search();
        searchView = findViewById(R.id.search_bar);
        // Some devices automatically focus to the search view
        searchView.clearFocus();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.search_bar) {
                        searchView.onActionViewExpanded();
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

        lastQuery = searchView.getQuery().toString();

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

        RadioGroup searchFilters = findViewById(R.id.search_filter_group);
        searchFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                onSearchFilterChanged(radioGroup, i);
            }
        });

        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        offset = 0;
    }

    private <T> void doSearch(String query) {
        SharedPreferences sharedPref = ((Activity)this).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (searchLimitReached || firebaseUser == null
                && user.isSearchLimitReached(firebaseUser, Role.GUEST, this)) {
            searchLimitReached = true;
            Context context = this;
            Activity activity = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, getString(R.string.join_message_search_guest));
                    signUpPopUp.createAndShow();
                }
            });

            return;
        }
        else if (searchLimitReached || firebaseUser != null
                && user.isSearchLimitReached(firebaseUser, Role.USER, this)) {

            Context context = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Toast toast = Toast.makeText(context,
                            "You've reached you're daily search limit.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

            finish();
        }



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
        if (!lastQuery.equals(query)) {
            allSearch = false;
        }

        lastQuery = query;
        SearchFilter lastFilter = search.getCurrentFilter();
        search = new Search(query, 100, spotifyService, lastFilter, allSearch);
        if (!search.execute(offset)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(context,
                            "Encountered an error while searching, try again later.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        user.incrementSearchCount();
        editor.putInt(getString(R.string.searchCount), user.getSearchCount());
        editor.apply();

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

    private void onSearchFilterChanged(RadioGroup radioGroup, int i) {
        List<SearchResult> results = new ArrayList<>();

        // If the search hasn't been started skip a lot of the logic
        if (searchStarted) {
            // If all results are already retrieved
            if (allSearch) {
                switch (i) {
                    case R.id.search_filter_all:
                        results = search.getAll();
                        break;
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
                setSearchResults(results);
                setFilterOnPosition(i);
            }
            // Else if the search has started
            // and the query hasn't changed
            // and all hasn't been retrieved yet
            // OR
            // if the query did change and search has already started
            else if ((searchStarted
                    && lastQuery.equals(searchView.getQuery().toString())
                    && !allSearch)
                    || !lastQuery.equals(searchView.getQuery().toString()) && searchStarted) {
                // Set allSearch to true, so this block doesn't repeat until the search query changes
                allSearch = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doSearch(searchView.getQuery().toString());
                        setFilterOnPosition(i);
                    }
                }).start();
            }
        }
            else {
                setFilterOnPosition(i);
            }
    }

    private void setSearchResults(List<SearchResult> results) {
        searchAdapter.setSearchResults(results);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                searchAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setFilterOnPosition(int i) {
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

    @Override
    public void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            user = (User) extras.getSerializable("user");
            if(user != null)
            {
                user.setSearchCount(sharedPref.getInt(getString(R.string.searchCount), 0));
                searchAdapter.setUser(user);
            }
        }

        Activity activity = this;
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(view.getContext(), SearchActivity.class);
//                view.getContext().startActivity(intent);
                activity.finish();
            }
        });
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

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public DatabaseReference getDb() { return db;}

    public SpotifyService getSpotifyService() { return spotifyService; }

    public Search getSearch() {
        return search;
    }
}