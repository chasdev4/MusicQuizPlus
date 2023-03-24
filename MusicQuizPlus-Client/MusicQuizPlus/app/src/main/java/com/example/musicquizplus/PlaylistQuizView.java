package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Playlist;
import model.item.Track;
import model.type.HeartResponse;
import model.type.Source;
import service.FirebaseService;
import service.SpotifyService;
import service.firebase.AlbumService;
import service.firebase.PlaylistService;

public class PlaylistQuizView extends AppCompatActivity implements Serializable {

    ImageView coverImage;
    TextView title;
    TextView owner;
    RecyclerView listView;
    AppCompatButton startQuiz;
    Playlist playlist;
    HistoryAdapter adapter;
    Handler mainHandler = new Handler();
    ImageButton backToTop;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
    ImageButton spotifyButton;
    ImageButton shareButton;
    boolean isSpotifyInstalled;
    private Source source;
    private SpotifyService spotifyService;
    private ToggleButton heartButton;
    private User user;
    private ProgressBar progressBar;

    private ToolTipsManager toolTipsManager;
    private ToolTip.Builder builder;
    ConstraintLayout root;
    int track = 0;
    String pqvToolTipsDate, currentDate;
    int pqvToolTips;
    boolean showToolTipsBool, firstLaunchEver;

    private View loadingPopUp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_quiz_view);

        loadingPopUp = findViewById(R.id.pqvSaving);
        coverImage = findViewById(R.id.pqvCoverImage);
        title = findViewById(R.id.pqvTitle);
        title.setSelected(true);
        owner = findViewById(R.id.pqvPlaylistOwner);
        owner.setSelected(true);
        listView = findViewById(R.id.pqvRecyclerView);
        listView.setVisibility(View.INVISIBLE);
        startQuiz = findViewById(R.id.pqvStartButton);
        backToTop = findViewById(R.id.pqvBackToTop);
        spotifyButton = findViewById(R.id.pqvSpotifyButton);
        shareButton = findViewById(R.id.pqvShareButton);
        heartButton = findViewById(R.id.playlist_heart);
        progressBar = findViewById(R.id.playlist_quiz_view_progress_bar);
        root = findViewById(R.id.pqvRoot);
        toolTipsManager = new ToolTipsManager();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = df.format(c);

        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));

        PackageManager pm = getPackageManager();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            user = (User) extras.getSerializable("currentUser");
            playlist = (Playlist) extras.getSerializable("currentPlaylist");
            firstLaunchEver = extras.getBoolean("firstLaunchEver");

            if(firstLaunchEver)
            {
                startQuiz.setVisibility(View.INVISIBLE);
                ((TextView)loadingPopUp.findViewById(R.id.loading_text)).setText(R.string.loading_quiz);
                loadingPopUp.setVisibility(View.VISIBLE);
            }
        }

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) listView.getLayoutManager();
                int scroll = llm.findFirstVisibleItemPosition();

                if (scroll > 0) {
                    backToTop.setVisibility(View.VISIBLE);
                } else {
                    backToTop.setVisibility(View.GONE);
                }

            }
        });

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        });


        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));

        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("com.spotify.music", 0);
                    isSpotifyInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isSpotifyInstalled = false;
                }

                if (isSpotifyInstalled) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(playlist.getId()));
                    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + getBaseContext().getPackageName()));
                    startActivity(intent);
                } else {
                    String url = getPlaylistIdAsUrl(playlist.getId());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getPlaylistIdAsUrl(playlist.getId()));
                shareIntent.putExtra(Intent.EXTRA_TITLE, "Share Spotify Playlist");
                shareIntent.setType("text/*");
                startActivity(Intent.createChooser(shareIntent, null));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("ToolTipsData", MODE_PRIVATE);
        pqvToolTips = sh.getInt("pqvToolTips", 0);
        pqvToolTipsDate = sh.getString("pqvToolTipsDate", "");
        showToolTipsBool = sh.getBoolean("showToolTipsBool", true);

        if(showToolTipsBool && !firstLaunchEver)
        {
            if(!currentDate.equals(pqvToolTipsDate))
            {
                new Handler().postDelayed(this::showNext, 2500);
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
        myEdit.putInt("pqvToolTips", pqvToolTips);
        myEdit.putString("pqvToolTipsDate", pqvToolTipsDate);
        myEdit.apply();
    }

    private void showNext()
    {
        toolTipsManager.dismissAll();

        if(pqvToolTips < 3)
        {
            if(track == 0)
            {
                builder = new ToolTip.Builder(this, heartButton, root, "Click Here To Heart A Playlist\nTo Add It To Your Collection", ToolTip.POSITION_LEFT_TO);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showNext, 3000);
            }
            else if(track == 1)
            {
                //show tool tip for spotify button
                builder = new ToolTip.Builder(this, spotifyButton, root, "Click Here To View\nThis Playlist On Spotify", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showNext, 3000);
            }
            else if(track == 2)
            {
                //show tool tip for share button
                builder = new ToolTip.Builder(this, shareButton, root, "Click Here To Share\nThis Spotify Playlist", ToolTip.POSITION_BELOW);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showNext, 3000);
            }
            else if(track == 3)
            {
                //show tool tip for start quiz button
                builder = new ToolTip.Builder(this, startQuiz, root, "Click Here To Be Quizzed On This Playlist", ToolTip.POSITION_ABOVE);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                track++;
                new Handler().postDelayed(this::showNext, 3000);
            }
            else if(track == 4)
            {
                pqvToolTips++;
                pqvToolTipsDate = currentDate;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            source = (Source) extras.getSerializable("source");
            user = (User) extras.getSerializable("currentUser");

            if(user != null)
            {
                heartButton.setChecked(user.getPlaylistIds().containsValue(playlist.getId()));
                if (heartButton.isChecked()) {
                    Window window = getWindow();
                    window.setStatusBarColor(getResources().getColor(R.color.mqPurpleGreenBackground));
                    window.setNavigationBarColor(getResources().getColor(R.color.mqPurpleGreenBackground));
                    ((ConstraintLayout)findViewById(R.id.pqvRoot))
                            .setBackgroundColor(ContextCompat.getColor(this, R.color.mqPurpleGreenBackground));
                }
            }

            Activity activity = this;
            Context context = this;

            heartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(user != null && firebaseUser != null)
                            {
                                updatePopUpText(heartButton.isChecked());
                                updatePopUpColor(!heartButton.isChecked());
                                showPopUp();

                                HeartResponse response = null;
                                if (heartButton.isChecked()) {
                                    SpotifyService spotifyService = new SpotifyService(view.getContext().getString(R.string.SPOTIFY_KEY));
                                    response = PlaylistService.heart(user, firebaseUser, db, playlist, spotifyService, () -> hidePopUp());
                                } else {
                                    response = PlaylistService.unheart(user, firebaseUser, db, playlist, () -> hidePopUp());
                                }
                                if (response != HeartResponse.OK) {
                                    heartButton.setChecked(false);
                                    hidePopUp();
                                    if (response != HeartResponse.ITEM_EXISTS) {
                                        HeartResponse finalResponse = response;
                                        ((PlaylistQuizView) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlbumService.showError(finalResponse, context);
                                            }
                                        });
                                    }
                                }
                                else {
                                    updatePopUpColor(heartButton.isChecked());
                                            if (heartButton.isChecked()) {
                                                Window window = getWindow();
                                                window.setStatusBarColor(getResources().getColor(R.color.mqPurpleGreenBackground));
                                                window.setNavigationBarColor(getResources().getColor(R.color.mqPurpleGreenBackground));
                                                ((ConstraintLayout) findViewById(R.id.pqvRoot))
                                                        .setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleGreenBackground));
                                            } else {
                                                Window window = getWindow();
                                                window.setStatusBarColor(getResources().getColor(R.color.mqPurple3));
                                                window.setNavigationBarColor(getResources().getColor(R.color.mqPurple3));
                                                ((ConstraintLayout) findViewById(R.id.pqvRoot))
                                                        .setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple3));
                                            }
                                            if (adapter != null) {
                                                listView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                }
                            }
                            else
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, "Pump Up The Jam! You Can Save This Playlist By Joining");
                                        signUpPopUp.createAndShow();
                                        heartButton.setChecked(false);
                        }
                                });
                            }
                        }
                    }).start();

                }
            });

            if (playlist.getName().length() >= 19) {
                title.setTextSize(16);
            }

            title.setText(playlist.getName());
            owner.setText(playlist.getOwner());
//            playlistTracks = playlist.getTracksListFromMap();

            new FetchImage(playlist.getPhotoUrl().get(0).getUrl(), coverImage, title, playlist.getName(), mainHandler).start();
        }
        Context context = this;


        new Thread(new Runnable() {
            public void run() {
                CountDownLatch cdl = new CountDownLatch(1);
                if (source != Source.SEARCH) {
                    playlist.initCollection(reference);
                    cdl.countDown();
                } else {
                    playlist = PlaylistService.populatePlaylistTracks(reference, playlist, spotifyService);
                    if (playlist.getTracks() == null || playlist.getTracks().size() == 0) {
                        playlist.initCollection(reference);
                    }
                    cdl.countDown();
                }

                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (playlist.getTracks() != null) {
                    List<Track> tracksList = new ArrayList<>(playlist.getTracks().values());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new HistoryAdapter(user, tracksList, null, context, 1);
                            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                @Override
                                public void onChanged() {
                                    super.onChanged();
                                    onDataChange();
                                }
                            });
                            listView.setAdapter(adapter);
                            listView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                            onDataChange();
                        }
                    });
                }
                else {
                    Log.d("TAG", "run: ");
                }

            }
        }).start();

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playlist.isInitializing()) {
                    if (playlist.getTracks().size() >= 15) {
                        Intent intent = new Intent(view.getContext(), ActiveQuiz.class);
                        intent.putExtra("currentPlaylist", playlist);
                        intent.putExtra("currentUser", user);
                        startActivity(intent);
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(context,
                                        "Not enough data to start this quiz, choose different playlist", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void updatePopUpColor(boolean alternate) {
        loadingPopUp.findViewById(R.id.popup_background).setBackgroundColor(ContextCompat.getColor(this,
                alternate ? R.color.mqPurpleGreen : R.color.mqPurple2));
    }

    private void onDataChange() {
        if (adapter.getItemCount() == 0) {
            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }else {
            listView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            if(firstLaunchEver)
            {
                loadingPopUp.setVisibility(View.GONE);
                startQuiz.performClick();
            }
        }
    }

    public String getPlaylistIdAsUrl(String playlistID) {
        String id = playlistID.substring(17);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/playlist/%s", id);
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

    private void updatePopUpText(boolean b) {
        ((TextView)loadingPopUp.findViewById(R.id.loading_text)).setText(
                b ? R.string.saving_message
                        : R.string.removing_message
        );
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            if (heartButton.isChecked()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        user = FirebaseService.checkDatabase(reference, "users", firebaseUser.getUid(), User.class);
                        String playlistKey = null;
                        String playlistValue = null;
                        for (Map.Entry<String, String> playlistId : user.getPlaylistIds().entrySet()) {
                            if (playlistId.getValue().equals(playlist.getId())) {
                                playlistKey = playlistId.getKey();
                                playlistValue = playlistId.getValue();
                            }
                        }

                        Intent intent = getIntent();

                        intent.putExtra("playlistKey", playlistKey);
                        intent.putExtra("playlistValue", playlistValue);
                        intent.putExtra("user", user);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }).start();
            }
            else {
                return super.onKeyUp(keyCode, event);
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean isChecked() {
        return heartButton.isChecked();
    }
}