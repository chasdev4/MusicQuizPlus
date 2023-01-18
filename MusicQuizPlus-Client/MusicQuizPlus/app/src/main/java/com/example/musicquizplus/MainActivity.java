package com.example.musicquizplus;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.musicquizplus.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import model.GoogleSignIn;
import model.User;
import service.FirebaseService;
import service.SpotifyService;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private User user;
    private DatabaseReference db;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private GoogleSignIn googleSignIn;
    private Button signInWithGoogleButton;
    private boolean showOneTapUI;
    private SpotifyService spotifyService;

    private final String TAG = "MainActivity.java";
    private static final int REQ_ONE_TAP = 2;



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

//                new Thread(new Runnable() {
//                    public void run() {
//                        final short limit = 30;
//                        SearchResults searchResults = spotifyService.search("Morrissey", limit, 0);
//
//                        for (Artist artist : searchResults.getArtists()) {
//                            db.child("sample_artists").child(artist.getId()).setValue(artist);
//                        }
//                        for (Album album : searchResults.getAlbums()) {
//                            db.child("sample_albums").child(album.getId()).setValue(album);
//                        }
//                        for (Playlist playlist : searchResults.getPlaylists()) {
//                            db.child("sample_playlists").child(playlist.getId()).setValue(playlist);
//                        }
//                        for (Track track : searchResults.getTracks()) {
//                            db.child("sample_tracks").child(track.getId()).setValue(track);
//                        }
//
//                        Log.d("TEMP", "Goodie goodie");
//
//                    }
//                }).start();

            }
        });

        googleSignIn = new GoogleSignIn();
        firestore = FirebaseFirestore.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));


        // Find the view of the button and set the on click listener to begin signing in
        signInWithGoogleButton = findViewById(R.id.sign_in_with_google_button);
        signInWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle(view);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        updateUI();
    }

    private void updateUI() {
        // TODO: Update the state of app depending if the user is logged in or not
        if (firebaseUser != null) {
            // User is signed in
            signInWithGoogleButton.setVisibility(View.GONE);
            Log.d(TAG, firebaseUser.getDisplayName());
            Log.d(TAG, firebaseUser.getEmail());

            new Thread(new Runnable() {
                public void run() {
                    user = (User)FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);

                    // DEBUG: Uncomment me to test heartPlaylist
//                    Playlist playlist = new Playlist(
//                            "spotify:playlist:37i9dQZF1DX4Wsb4d7NKfP",
//                            "NKVT 2021",
//                            new ArrayList<PhotoUrl>() {{
//                                add(new PhotoUrl("https://i.scdn.co/image/ab67706f00000003c535afb205514b59e204627a",
//                                        0, 0));
//                            }},
//                            "Spotify",
//                            "NKVT sunar: yılın favori Türkçe rap parçaları. Kapak: UZI"
//                    );
//
//                    if (!playlist.isTrackIdsKnown()) {
//                       playlist = FirebaseService.populatePlaylistTracks(db, playlist, spotifyService);
//                    }
//
//                    FirebaseService.heartPlaylist(user, firebaseUser, db,
//                            playlist,
//                            spotifyService
//                    );

                    // DEBUG: Uncomment me to test unheartPlaylist
//                    Playlist playlist = FirebaseService.checkDatabase(db, "playlists", "spotify:playlist:37i9dQZF1DX4Wsb4d7NKfP", Playlist.class);
//                    FirebaseService.unheartPlaylist(user, firebaseUser, db, playlist, spotifyService);


                    // DEBUG: Uncomment me to test heartAlbum
//                    FirebaseService.heartAlbum(user, firebaseUser, db,
//                            new Album("spotify:album:1LybLcJ9KuOeLHsn1NEe3j",
//                                    "Inna",
//                                    new ArrayList<PhotoUrl>() {{
//                                        add(new PhotoUrl("https://i.scdn.co/image/ab67616d0000b2733257e2b781094bcdc048b2f2",
//                                                640, 640));
//                                    }},
//                                    new ArrayList<String>() {
//                                        {
//                                            add("INNA");
//                                        }
//                                    },
//                                    new ArrayList<String>() {
//                                        {
//                                            add("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
//                                        }
//                                    },
//                                    AlbumType.ALBUM, new ArrayList<String>(),
//                                    false, 0, false), spotifyService);


                    // DEBUG: Uncomment me to test unheartAlbum
//                    Album album = FirebaseService.checkDatabase(db, "albums", "spotify:album:1LybLcJ9KuOeLHsn1NEe3j", Album.class);
//                    FirebaseService.unheartAlbum(user, firebaseUser, db, album, spotifyService);
                }
            }).start();


        } else {
            // No user is signed in
            signInWithGoogleButton.setVisibility(View.VISIBLE);
        }
    }

    private void signInWithGoogle(View view) {
        // Configuration of Google Sign In
        googleSignIn.setOneTapClient(Identity.getSignInClient(this));
        googleSignIn.setSignUpRequest(BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.SERVER_CLIENT_ID))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build());

        // Begin the Sign In Request
        googleSignIn.getOneTapClient().beginSignIn(googleSignIn.getSignUpRequest())
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: (C-Feature) Take the user to a Google Sign In Form to add an account
                        // Note: Might not work or be worth the effort...

                        Snackbar.make(view, "ERROR: No Google accounts associate with this device. Sign In to Google Play Services and try again.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the request code
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    // Create an account with a Google ID token
                    SignInCredential credential = googleSignIn.getOneTapClient().getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google.
                        Log.d(TAG, "Got ID token.");

                        // With the Google ID token, exchange it for a Firebase credential,
                        // and authenticate with Firebase using the Firebase credential
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        googleSignIn.getAuth().signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            firebaseUser = googleSignIn.getAuth().getCurrentUser();
                                            FirebaseService.createUser(firebaseUser, firestore, db);
                                            updateUI();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                                            updateUI();
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case CommonStatusCodes.CANCELED:
                            Log.d(TAG, "One-tap dialog was closed.");
                            // Don't re-prompt the user.
                            showOneTapUI = false;
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            Log.d(TAG, "One-tap encountered a network error.");
                            // Try again or just ignore.
                            break;
                        default:
                            Log.d(TAG, "Couldn't get credential from result."
                                    + e.getLocalizedMessage());
                            break;
                    }
                }
                break;
        }
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