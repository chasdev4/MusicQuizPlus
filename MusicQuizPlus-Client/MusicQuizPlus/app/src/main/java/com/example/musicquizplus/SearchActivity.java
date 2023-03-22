package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.Locale;

import java.util.concurrent.CountDownLatch;


import model.GoogleSignIn;
import model.Search;
import model.SearchResult;
import model.TrackResult;
import model.User;
import model.item.Artist;
import model.item.Track;
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

    private RadioButton artist, album, song, playlist;

    private View loadingPopUp;
    private View playAlbumBanner;
    private ProgressBar playAlbumProgressBar;
    private AppCompatButton playAlbumYesButton;
    private AppCompatButton playAlbumNoButton;
    private AppCompatCheckBox playAlbumCheckbox;


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

    private ToolTipsManager toolTipsManager;
    private ToolTip.Builder builder;
    ConstraintLayout root;
    int track = 0;
    String searchToolTipsDate, currentDate;
    int searchToolTips;
    boolean showToolTipsBool;

    private String playNowArtistId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;


        artist = findViewById(R.id.search_filter_artist);
        album = findViewById(R.id.search_filter_album);
        song = findViewById(R.id.search_filter_song);
        playlist = findViewById(R.id.search_filter_playlist);
        toolTipsManager = new ToolTipsManager();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = df.format(c);

        playAlbumBanner = findViewById(R.id.play_album_banner);
        playAlbumProgressBar = findViewById(R.id.play_album_progressbar);
        playAlbumProgressBar.setProgress(100);
        playAlbumYesButton = findViewById(R.id.play_album_yes);
        playAlbumNoButton = findViewById(R.id.play_album_no);

        playAlbumCheckbox = findViewById(R.id.play_album_checkbox);

        loadingPopUp = findViewById(R.id.search_saving);


        searchStarted = false;
        doingSearch = false;

        progressBar = findViewById(R.id.search_progress_bar);
        progressBar.setVisibility(View.GONE);

        GoogleSignIn googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();
        root = findViewById(R.id.searchRoot);

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
        searchIcon.setImageDrawable(ContextCompat.getDrawable((Activity) this, R.drawable.search));
        ImageView searchCloseIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchCloseIcon.setImageDrawable(ContextCompat.getDrawable((Activity) this, R.drawable.close));

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

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        hidePopUp();

        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("ToolTipsData", MODE_PRIVATE);
        searchToolTips = sh.getInt("searchToolTips", 0);
        searchToolTipsDate = sh.getString("searchToolTipsDate", "");
        showToolTipsBool = sh.getBoolean("showToolTipsBool", true);

        if(showToolTipsBool)
        {
            if(!currentDate.equals(searchToolTipsDate))
            {
                new Handler().postDelayed(this::showToolTips, 1500);
                searchToolTips++;
                searchToolTipsDate = currentDate;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Creating a shared pref object
        SharedPreferences sharedPreferences = getSharedPreferences("ToolTipsData", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putInt("searchToolTips", searchToolTips);
        myEdit.putString("searchToolTipsDate", searchToolTipsDate);
        myEdit.apply();
    }

    private void showToolTips()
    {
        toolTipsManager.dismissAll();

        if(searchToolTips < 3)
        {
            if (track == 0)
            {
                builder = new ToolTip.Builder(this, artist, root, "Click To Filter Search\nResults By Artists", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showToolTips, 3000);
            }
            else if(track == 1)
            {
                builder = new ToolTip.Builder(this, album, root, "Click To Filter Search\nResults By Albums", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showToolTips, 3000);
            }
            else if(track == 2)
            {
                builder = new ToolTip.Builder(this, song, root, "Click To Filter Search\nResults By Songs", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showToolTips, 3000);
            }
            else if(track == 3)
            {
                builder = new ToolTip.Builder(this, playlist, root, "Click To Filter Search\nResults By Playlists", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showToolTips, 3000);
            }
        }
    }

    private <T> void doSearch(String query) {
        SharedPreferences sharedPref = ((Activity) this).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
//        if (searchLimitReached || firebaseUser == null
//                && user.isSearchLimitReached(firebaseUser, Role.GUEST, this)) {
//            searchLimitReached = true;
//            Context context = this;
//            Activity activity = this;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, getString(R.string.join_message_search_guest));
//                    signUpPopUp.createAndShow();
//                }
//            });
//
//            return;
//        }
//        else if (searchLimitReached || firebaseUser != null
//                && user.isSearchLimitReached(firebaseUser, Role.USER, this)) {
//
//            Context context = this;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    final Toast toast = Toast.makeText(context,
//                            "You've reached you're daily search limit.", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            });
//
//            finish();
//        }


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
        search = new Search(query, 50, spotifyService, lastFilter, allSearch);
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
                        setFilterOnPosition(i);
                        doSearch(searchView.getQuery().toString());
                    }
                }).start();
            }
        } else {
            setFilterOnPosition(i);
        }
    }

    private void setSearchResults(List<SearchResult> results) {
//        searchAdapter.clear();
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
//            SharedPreferences.Editor editor1 = sharedPref.edit();
//            editor1.putBoolean(getString(R.string.playNowHidden), false);
//            editor1.apply();
            user = (User) extras.getSerializable("user");
            if (user != null) {
                user.setSearchCount(sharedPref.getInt(getString(R.string.searchCount), 0));
                user.getSettings().setPlayNowBannerHidden(sharedPref.getBoolean(getString(R.string.playNowHidden), false));
                if (searchAdapter.getUser() == null) {
                    searchAdapter.setUser(user);
                }

                playAlbumNoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((SearchActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (playAlbumCheckbox.isChecked()) {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean(getString(R.string.playNowHidden), true);
                                    editor.apply();
                                    user.getSettings().setPlayNowBannerHidden(true);
                                }
                                else {
                                    playAlbumProgressBar.setProgress(100);
                                }
                                playAlbumBanner.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                playAlbumYesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playAlbumBanner.setVisibility(View.GONE);
                                updateToLoadingPopUpText();
                                showPopUp();
                            }
                        });
                        if (getPlayNowArtistId() == null) {
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Artist artist = null;
                                User user = null;
                                while (artist == null) {
                                    user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                                    if (user.getArtistIds().containsValue(playNowArtistId)) {
                                        user.initArtists(db, firebaseUser, false);
                                    }
                                    artist = user.getArtist(getPlayNowArtistId());
                                }

                                CountDownLatch countDownLatch = new CountDownLatch(1);
                                artist.initCollections(db, user);
                                countDownLatch.countDown();
                                try {
                                    countDownLatch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                countDownLatch = new CountDownLatch(1);
                                artist.initTracks(db, user);
                                countDownLatch.countDown();
                                try {
                                    countDownLatch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                int size = artist.getTrackPoolSize();

                                if (size >= 15) {
                                    Intent intent = new Intent(context, ActiveQuiz.class);
                                    intent.putExtra("currentArtist", artist);
                                    intent.putExtra("currentUser", user);
                                    startActivity(intent);
                                }
                                else {
                                    hidePopUp();
                                    Artist finalArtist = artist;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(context,
                                                    String.format("Not enough data to start quiz. Heart more albums by %s and try again.",
                                                            finalArtist.getName()), Toast.LENGTH_LONG);
                                            toast.show();
                                        }
                                    });
                                }
                            }
                        }).start();


                    }
                });
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            int pos = extras.getInt("pos");
            String artistKey = extras.getString("artistKey");
            String artistId = extras.getString("artistId");
            String albumKey = extras.getString("albumKey");
            String albumValue = extras.getString("albumValue");
            String playlistKey = extras.getString("playlistKey");
            String playlistValue = extras.getString("playlistValue");
            User user = (User) extras.getSerializable("user");
            boolean updateApater = false;
            if (artistId != null && artistKey != null && pos >= 0 && user != null) {
                if (!user.getArtistIds().containsValue(artistId)) {
                    user.addArtistId(artistKey, artistId);
                }
                updateApater = true;
//                searchAdapter.notifyDataSetChanged();
            } else if (albumKey != null && albumValue != null && pos >= 0 && user != null) {
                if (!user.getAlbumIds().containsValue(albumValue)) {
                    user.addAlbumId(albumKey, albumValue);
                }
                updateApater = true;
            } else if (playlistKey != null && playlistValue != null && pos >= 0 && user != null) {
                if (!user.getPlaylistIds().containsValue(playlistValue)) {
                    user.addPlaylistId(playlistKey, playlistValue);
                }
                updateApater = true;
            }

            if (updateApater) {
                this.user = user;
                searchAdapter.setUser(user);
                searchAdapter.getSearchResults().set(pos, searchAdapter.getSearchResults().get(pos));
                searchAdapter.notifyItemChanged(pos);
            }
        }
    }


    private void showPlayNow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playAlbumProgressBar.setProgress(100);
                playAlbumBanner.setVisibility(View.VISIBLE);

                if (backToTop.getVisibility() == View.VISIBLE) {
                    backToTop.setVisibility(View.GONE);
                }
            }
        });
        ValueAnimator valueAnimator = ValueAnimator.ofInt(100, 0);
        valueAnimator.setDuration(10000);
        valueAnimator.setStartDelay(0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                playAlbumProgressBar.setProgress((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                playAlbumBanner.setVisibility(View.GONE);
            }
        });

        valueAnimator.start();
    }

    private void hidePlayNow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playAlbumBanner.setVisibility(View.GONE);
            }
        });
    }

    public TrackResult getTrackResult(Track track) {
        return search.getTrackResult(track);
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchAdapter(this, this, new ArrayList<>());
        searchAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onDataChange();
            }
        });
        searchAdapter.setHidePopUp(() -> hidePopUp());
        searchAdapter.setShowPopUp(() -> showPopUp());
        searchAdapter.setHidePlayNow(() -> hidePlayNow());
        searchAdapter.setShowPlayNow(() -> showPlayNow());
        searchAdapter.setUpdatePopUpTextFalse(() -> updateSavingPopUpText(false));
        searchAdapter.setUpdatePopUpTextTrue(() -> updateSavingPopUpText(true));

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

    private void hidePopUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPopUp.setVisibility(View.GONE);
            }
        });
    }

    private void showPopUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPopUp.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateSavingPopUpText(boolean b) {
        ((TextView) loadingPopUp.findViewById(R.id.loading_text)).setText(
                b ? R.string.saving_message
                        : R.string.removing_message
        );
    }
    private void updateToLoadingPopUpText() {
        ((TextView) loadingPopUp.findViewById(R.id.loading_text)).setText(getString(R.string.loading_message));
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public DatabaseReference getDb() {
        return db;
    }

    public SpotifyService getSpotifyService() {
        return spotifyService;
    }

    public Search getSearch() {
        return search;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public View getPlayAlbumBanner() {
        return playAlbumBanner;
    }

    public ProgressBar getPlayAlbumProgressBar() {
        return playAlbumProgressBar;
    }

    public AppCompatButton getPlayAlbumYesButton() {
        return playAlbumYesButton;
    }

    public void setPlayNowArtistId(String artistId) {
        this.playNowArtistId = artistId;
    }

    public String getPlayNowArtistId() {
        return playNowArtistId;
    }
}