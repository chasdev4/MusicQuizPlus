package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseUser;
import com.example.musicquizplus.fragments.PlaylistFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.Quiz;
import model.User;
import model.SignUpPopUp;
import model.User;
import model.item.Playlist;
import model.item.Track;
import model.type.Source;
import service.SpotifyService;
import service.firebase.AlbumService;
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
        startQuiz = findViewById(R.id.pqvStartButton);
        backToTop = findViewById(R.id.pqvBackToTop);
        backButton = findViewById(R.id.pqvBackButton);
        spotifyButton = findViewById(R.id.pqvSpotifyButton);
        shareButton = findViewById(R.id.pqvShareButton);
        heartButton = findViewById(R.id.playlist_heart);

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
                //TODO: Add MQP logo to share menu when available.
                // Below we're passing a content URI to an image to be displayed
                //sendIntent.setData(mqpLogoUri);
                //sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("text/*");
                startActivity(Intent.createChooser(shareIntent, null));
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            playlist = (Playlist) extras.getSerializable("currentPlaylist");
            source = (Source) extras.getSerializable("source");

            if(user != null)
            {
                heartButton.setChecked(user.getPlaylistIds().containsValue(playlist.getId()));
            }

            heartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoogleSignIn googleSignIn = new GoogleSignIn();
                    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(user != null)
                            {
                                if (heartButton.isChecked()) {
                                    SpotifyService spotifyService = new SpotifyService(view.getContext().getString(R.string.SPOTIFY_KEY));
                                    PlaylistService.heart(user, firebaseUser, db, playlist, spotifyService);
                                } else {
                                    PlaylistService.unheart(user, firebaseUser, db, playlist);
                                }
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
                if (source != Source.SEARCH) {
                    playlist.initCollection(reference);
                } else {
                    PlaylistService.populatePlaylistTracks(reference, playlist, spotifyService);
                }
                List<Track> tracksList = new ArrayList<>(playlist.getTracks().values());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new HistoryAdapter(user, tracksList, null, getBaseContext(), 1);
                        listView.setAdapter(adapter);
                        listView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                    }
                });

            }
        }).start();

        Playlist finalPlaylist = playlist;

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

    public String getPlaylistIdAsUrl(String playlistID) {
        String id = playlistID.substring(17);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/playlist/%s", id);
    }

}