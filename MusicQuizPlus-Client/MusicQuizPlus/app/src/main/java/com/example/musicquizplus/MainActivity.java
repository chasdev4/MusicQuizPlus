package com.example.musicquizplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.musicquizplus.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.Map;

import model.GoogleSignIn;

import model.User;

import service.FirebaseService;
import service.SpotifyService;
import service.firebase.PlaylistService;
import service.firebase.UserService;
import utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private User user;
    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private GoogleSignIn googleSignIn;
    private Button signInWithGoogleButton;
    private SpotifyService spotifyService;
    private Map<String, String> defaultPlaylistIds;

    private final String TAG = "MainActivity.java";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                new Thread(new Runnable() {
                    public void run() {
                        //#region DEBUG: Search
//                        Search search = new Search("Morrissey", 30, spotifyService);
//                        search.execute(0);
//                        List<Track> trackList = search.getTracks();
//                        TrackResult result = search.getTrackResult(trackList.get(0));
//
//                        Log.d(TAG,"done");
                        //#endregion


                    }
                }).start();

            }
        });

        googleSignIn = new GoogleSignIn();
        db = FirebaseDatabase.getInstance().getReference();
        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));


        // Find the view of the button and set the on click listener to begin signing in
        signInWithGoogleButton = findViewById(R.id.sign_in_with_google_button);
        Context context = this;
        Activity activity = this;
        signInWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn.signInWithGoogle(view, activity, context);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        // googleSignIn.signOut();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        updateUI(firebaseUser);
    }

    public void updateUI(FirebaseUser firebaseUser) {
        LogUtil log = new LogUtil(TAG, "updateUI");

        this.firebaseUser = firebaseUser;
        // TODO: Update the state of app depending if the user is logged in or not
        if (this.firebaseUser != null) {
            // User is signed in
            signInWithGoogleButton.setVisibility(View.GONE);
            log.d(firebaseUser.getDisplayName());
            log.d(firebaseUser.getEmail());


            new Thread(new Runnable() {
                public void run() {
                    defaultPlaylistIds = PlaylistService.getDefaultPlaylistIds(db);
                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    if (user != null) {
                        //#region DEBUG: Playlist Quiz Sandbox
//                        user.initCollections(db);
//                        Playlist userPlaylist = user.getPlaylist("spotify:playlist:37i9dQZF1DWTJ7xPn4vNaz");
//                        userPlaylist.initCollection(db);
//                        List<String> newTrackIds = new ArrayList<>();
//                        int i = 0;
//                        for (String trackId : userPlaylist.getTrackIds()) {
//                            if (!trackId.equals(userPlaylist.getTracksListFromMap().get(userPlaylist.getTrackIds().indexOf(trackId)).getId())) {
//                                log.e("Tracks are out of order.");
//                            }
//                            i++;
//                        }
//                        for (int k = 80; k > 0; k--) {
//                            userPlaylist.getTrackIds().remove(userPlaylist.getTrackIds().size() - 1);
//                            userPlaylist.getTracks().remove(k + 19);
//                        }
//
//                        Random rnd = new Random();
//                        Quiz quiz = new Quiz(userPlaylist, user, db, firebaseUser);
//                        Question question = quiz.getFirstQuestion();
//                        quiz.start();
//                        i = 1;
//                        while (question != null) {
//                            log.d(String.valueOf(i));
//                            i++;
//                            CountDownLatch cdl = new CountDownLatch(1);
//                            int index = ((rnd.nextInt(2)+1) % 2 == 0) ? rnd.nextInt(4) : question.getAnswerIndex();
//                            try {
//                                Thread.sleep(rnd.nextInt(1) * 1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            question = quiz.nextQuestion(index);
//                            cdl.countDown();
//
//                            try {
//                                cdl.await();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        quiz.end();
//                        log.d("Done.");
                        //#endregion

/*
                        //region TESTING BADGES

                        user.initArtists(db);
                        Artist artist = user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
                        user.setArtistQuizCount(2);
                        Quiz quiz = new Quiz(artist, user);
                        quiz.setNumQuestions(10);
                        quiz.setNumCorrect(10);
                        Badge badge = new Badge(user, quiz);
                        badge.getEarnedBadges(getBaseContext());
                        int i = 0;

                        //endregion
 */



                        //#region DEBUG: Uncomment me to test out playlist quiz generation
//                        user.initCollections(db);
//                        Playlist userPlaylist = user.getPlaylist("spotify:playlist:37i9dQZF1DWTJ7xPn4vNaz");
//                        userPlaylist.initCollection(db);
//                        List<String> newTrackIds = new ArrayList<>();
//                        int i = 0;
//                        for (String trackId : userPlaylist.getTrackIds()) {
//                            if (!trackId.equals(userPlaylist.getTracksListFromMap().get(userPlaylist.getTrackIds().indexOf(trackId)).getId())) {
//                                log.e("Tracks are out of order.");
//                            }
//                            i++;
//                        }
//                        for (int k = 80; k > 0; k--) {
//                            userPlaylist.getTrackIds().remove(userPlaylist.getTrackIds().size() - 1);
//                            userPlaylist.getTracks().remove(k + 19);
//                        }
//
//                        Quiz quiz = new Quiz(userPlaylist, user, db, firebaseUser);
//                        quiz.end();
//                        log.d("Done.");
                        //#endregion

                        //#region DEBUG: Uncomment me to test out artist quiz generation
//                        user.initCollections(db);
//                        CountDownLatch countDownLatch = new CountDownLatch(1);
//                        Artist artist = user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
//                        artist.initCollections(db, user);
//                        countDownLatch.countDown();
//                        try {
//                            countDownLatch.await();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        countDownLatch = new CountDownLatch(1);
//                        artist.initTracks(db);
//                        countDownLatch.countDown();
//                        try {
//                            countDownLatch.await();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        Quiz quiz = new Quiz(artist, user, db, firebaseUser);
//                        quiz.end();
//                        log.d("Done.");
                        //#endregion

                        //#region DEBUG: Uncomment me to test heartPlaylist
//                        Playlist playlist = new Playlist(
//                                "spotify:playlist:37i9dQZF1DX4Wsb4d7NKfP",
//                                "NKVT 2021",
//                                new ArrayList<PhotoUrl>() {{
//                                    add(new PhotoUrl("https://i.scdn.co/image/ab67706f00000003c535afb205514b59e204627a",
//                                            0, 0));
//                                }},
//                                "Spotify",
//                                "NKVT sunar: yılın favori Türkçe rap parçaları. Kapak: UZI"
//                        );
//
//
//                        playlist = PlaylistService.populatePlaylistTracks(db, playlist, spotifyService);
//
//
//                        PlaylistService.heart(user, firebaseUser, db,
//                                playlist,
//                                spotifyService
//                        );
                        //#endregion

                        //#region DEBUG: Uncomment me to test unheartPlaylist
//                   playlist = FirebaseService.checkDatabase(db, "playlists", "spotify:playlist:37i9dQZF1DX4Wsb4d7NKfP", Playlist.class);
//                    PlaylistService.unheart(user, firebaseUser, db, playlist, spotifyService);
                        //#endregion

                        //#region DEBUG: Uncomment me to test heartAlbum
//                        AlbumService.heart(user, firebaseUser, db,
//                                new Album("spotify:album:1LybLcJ9KuOeLHsn1NEe3j",
//                                        "Inna",
//                                        new ArrayList<PhotoUrl>() {{
//                                            add(new PhotoUrl("https://i.scdn.co/image/ab67616d0000b2733257e2b781094bcdc048b2f2",
//                                                    640, 640));
//                                        }},
//                                        "spotify:artist:2w9zwq3AktTeYYMuhMjju8",
//                                        new HashMap<String, String>() {
//                                            {
//                                                put("spotify:artist:2w9zwq3AktTeYYMuhMjju8", "INNA");
//                                            }
//                                        },
//                                        AlbumType.ALBUM, new ArrayList<String>(),
//                                        false, 0, false,
//                                        "2015"), spotifyService);
//
//                        user.initCollections(db);
//
//                        Artist artist = user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
//                        artist.initCollections(db, user);
//
//                        AlbumService.heart(user, firebaseUser, db,
//                                user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8").getAlbums().get(1), spotifyService);
//                        AlbumService.heart(user, firebaseUser, db,
//                                user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8").getAlbums().get(2), spotifyService);
                        //#endregion


                        //#region DEBUG: Uncomment me to test unheartAlbum
//                    Album album = FirebaseService.checkDatabase(db, "albums", "spotify:album:1LybLcJ9KuOeLHsn1NEe3j", Album.class);
//                    AlbumService.unheartalbum(user, firebaseUser, db, album, spotifyService);
                        //#endregion





                        //#region DO NOT USE unless absolutely necessary
//                        PlaylistService.createDefaultPlaylists(db, spotifyService);
//                        log.d("Done.");
                        //#endregion
                    } else {
                        UserService.createUser(firebaseUser, db, defaultPlaylistIds);
                    }
                }
            }).start();


        } else {
            // No user is signed in
            signInWithGoogleButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignIn.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}