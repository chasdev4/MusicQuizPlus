package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.io.Serializable;
import java.net.ContentHandler;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Playlist;
import model.item.Track;
import model.type.Source;
import service.SpotifyService;
import service.firebase.PlaylistService;

public class PlaylistQuizView extends AppCompatActivity implements Serializable {

    ImageView coverImage;
    TextView title;
    TextView owner;
    RecyclerView listView;
    Button startQuiz;
    Playlist playlist;
    HistoryAdapter adapter;
    Handler mainHandler = new Handler();
    ImageButton backToTop;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
    ImageButton backButton;
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
    boolean showToolTipsBool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_quiz_view);

        coverImage = findViewById(R.id.pqvCoverImage);
        title = findViewById(R.id.pqvTitle);
        title.setSelected(true);
        owner = findViewById(R.id.pqvPlaylistOwner);
        owner.setSelected(true);
        listView = findViewById(R.id.pqvRecyclerView);
        listView.setVisibility(View.INVISIBLE);
        startQuiz = findViewById(R.id.pqvStartButton);
        backToTop = findViewById(R.id.pqvBackToTop);
        backButton = findViewById(R.id.pqvBackButton);
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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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

        if(showToolTipsBool)
        {
            if(!currentDate.equals(pqvToolTipsDate))
            {
                new Handler().postDelayed(this::showNext, 2500);
                pqvToolTips++;
                pqvToolTipsDate = currentDate;
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
                            if(firebaseUser != null)
                            {
                                if (heartButton.isChecked()) {
                                    SpotifyService spotifyService = new SpotifyService(view.getContext().getString(R.string.SPOTIFY_KEY));
                                    PlaylistService.heart(user, firebaseUser, db, playlist, spotifyService);
                                } else {
                                    PlaylistService.unheart(user, firebaseUser, db, playlist);
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

        new Thread(new Runnable() {
            public void run() {
                CountDownLatch cdl = new CountDownLatch(1);
                if (source != Source.SEARCH) {
                    playlist.initCollection(reference);
                    cdl.countDown();
                } else {
                    playlist = PlaylistService.populatePlaylistTracks(reference, playlist, spotifyService);
                    playlist.initCollection(reference);
                    cdl.countDown();
                }

                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<Track> tracksList = new ArrayList<>(playlist.getTracks().values());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new HistoryAdapter(user, tracksList, null, getBaseContext(), 1);
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
        }).start();

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playlist.isInitializing()) {
                    Intent intent = new Intent(view.getContext(), ActiveQuiz.class);
                    intent.putExtra("currentPlaylist", playlist);
                    intent.putExtra("currentUser", user);
                    startActivity(intent);
                }
            }
        });

    }

    private void onDataChange() {
        if (adapter.getItemCount() == 0) {
            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }else {
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public String getPlaylistIdAsUrl(String playlistID) {
        String id = playlistID.substring(17);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/playlist/%s", id);
    }

}