package com.example.musicquizplus;

import static android.app.PendingIntent.getActivity;

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
        Context context = this;
        new Thread(new Runnable() {
            public void run() {

                // If the user is signed in
                if (firebaseUser != null) {
                    // User is signed in

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            signInWithGoogleButton.setVisibility(View.GONE);

                        }
                    });

                    log.d(firebaseUser.getDisplayName());
                    log.d(firebaseUser.getEmail());
                    defaultPlaylistIds = PlaylistService.getDefaultPlaylistIds(db);
                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    // If there is a database entry for the suer
                    if (user != null) {
//                        user.initBadges(db);
//                        log.d("done");
                        //#region DEBUG: Getting Started
//                        GettingStarted gettingStarted = new GettingStarted(new User(), db);
//                        gettingStarted.selectDecade(1980);
//                        Map<String, Artist> artistMap = gettingStarted.getArtists();
//                        for (Map.Entry<String, Artist> entry : artistMap.entrySet()) {
//                            gettingStarted.selectArtist(entry.getKey());
//                        }
//                        gettingStarted.finished(db, firebaseUser, spotifyService, context);
//                        log.d("done");
                        //#endregion

                        //#region DEBUG: Playlist Quiz Sandbox
//                        user.initBadgeThumbnails(db);
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
//
//                        for (int p = 0; p < 100; p++) {
//                            CountDownLatch countDownLatch = new CountDownLatch(1);
//                            Random rnd = new Random();
//                            Quiz quiz = new Quiz(userPlaylist, user, db, firebaseUser);
//                            Question question = quiz.getFirstQuestion();
//                            quiz.start();
//                            i = 1;
//                            log.d(String.valueOf(p));
//                            while (question != null) {
//                                i++;
////                                int index = ((rnd.nextInt(2) + 1) % 2 == 0) ? rnd.nextInt(4) : question.getAnswerIndex();
//                                int index = question.getAnswerIndex();
//                                try {
//                                    Thread.sleep(rnd.nextInt(1) * 1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                question = quiz.nextQuestion(index);
//                            }
//
//                            Results results = quiz.end();
//                            countDownLatch.countDown();
//                            try {
//                                countDownLatch.await();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                Thread.sleep(50);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                        log.d("Done.");
                        //#endregion

                        //#region DEBUG: Artist Quiz Sandbox
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
//                        for (int p = 0; p < 50; p++) {
//                            log.d(String.valueOf(p+1));
//                            countDownLatch = new CountDownLatch(1);
//                            int i = 1;
//                            Random rnd = new Random();
//                            Quiz quiz = new Quiz(artist, user, db, firebaseUser);
//                            Question question = quiz.getFirstQuestion();
//                            quiz.start();
//                            while (question != null) {
//                                i++;
//                                CountDownLatch cdl = new CountDownLatch(1);
////                            int index = ((rnd.nextInt(2)+1) % 2 == 0) ? rnd.nextInt(4) : question.getAnswerIndex();
//                                int index = question.getAnswerIndex();
//                                try {
//                                    Thread.sleep(rnd.nextInt(1) * 1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                question = quiz.nextQuestion(index);
//                                cdl.countDown();
//
//                                try {
//                                    cdl.await();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            Results results = quiz.end();
//                            countDownLatch.countDown();
//
//                            try {
//                                countDownLatch.await();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                                                        try {
//                                Thread.sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        log.d("Done.");
                        //#endregion

                        //#region TESTING BADGES
/*
                        user.initArtists(db);
                        Artist artist = user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
                        user.setArtistQuizCount(2);
                        Quiz quiz = new Quiz(artist, user);
                        quiz.setNumQuestions(10);
                        quiz.setNumCorrect(10);
                        Badge badge = new Badge(user, quiz);
                        badge.getEarnedBadges(getBaseContext());
                        int i = 0;
*/
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
//                                new Album("spotify:album:6vwexbVstZKBPZS0qjSYtV",
//                                        "Keep Moving",
//                                        new ArrayList<PhotoUrl>() {{
//                                            add(new PhotoUrl("https://i.scdn.co/image/ab67616d00001e02607b0acd32f9615ad7ea3cdc",
//                                                    300, 300));
//                                        }},
//                                        "spotify:artist:4AYkFtEBnNnGuoo8HaHErd",
//                                        new HashMap<String, String>() {
//                                            {
//                                                put("spotify:artist:4AYkFtEBnNnGuoo8HaHErd", "Madness");
//                                            }
//                                        },
//                                        AlbumType.ALBUM, new ArrayList<String>(),
//                                        false, 0, false,
//                                        "1984"), spotifyService);
//
//                        user.initCollections(db);
//
//                        Artist artist = user.getArtist("spotify:artist:4AYkFtEBnNnGuoo8HaHErd");
//                        artist.initCollections(db, user);
//
//                        AlbumService.heart(user, firebaseUser, db,
//                                user.getArtist("spotify:artist:4AYkFtEBnNnGuoo8HaHErd").getAlbums().get(1), spotifyService);
//                        AlbumService.heart(user, firebaseUser, db,
//                                user.getArtist("spotify:artist:4AYkFtEBnNnGuoo8HaHErd").getAlbums().get(2), spotifyService);
//                        log.d("Done.");
                        //#endregion

                        //#region DEBUG: Uncomment me to test unheartAlbum
//                    Album album = FirebaseService.checkDatabase(db, "albums", "spotify:album:1LybLcJ9KuOeLHsn1NEe3j", Album.class);
//                    AlbumService.unheartalbum(user, firebaseUser, db, album, spotifyService);
                        //#endregion

                        //#region DO NOT USE unless absolutely necessary
//                        PlaylistService.createDefaultPlaylists(db, spotifyService);
//                        log.d("Done.");
                        //#endregion
                    }
                    // No database entry create one
                    else {
                        UserService.createUser(firebaseUser, db, defaultPlaylistIds);
                    }

                }
                // No user is signed in
                else {
                    signInWithGoogleButton.setVisibility(View.VISIBLE);
                }
            }
        }).start();
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