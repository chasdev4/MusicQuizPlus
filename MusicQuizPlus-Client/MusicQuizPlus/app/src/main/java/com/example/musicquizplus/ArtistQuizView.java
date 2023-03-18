package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.ExternalLink;
import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.type.ExternalLinkType;
import model.type.HeartResponse;
import model.type.Source;
import service.FirebaseService;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;
import utils.LogUtil;

public class ArtistQuizView extends AppCompatActivity {

    User user;
    Artist artist;
    TextView artistNameTV;
    TextView artistBioTV;
    ImageView artistPreviewImage;
    ImageButton spotify;
    ImageButton facebook;
    ImageButton twitter;
    ImageButton wikipedia;
    ImageButton instagram;
    ImageButton share;
    ToggleButton heartLatest;
    Button startQuiz;
    ImageView latestImage;
    TextView latestTitle;
    TextView latestType;
    TextView latestYear;
    TextView latestText;
    TextView latestMiddleDot;
    View latestRelease;
    RecyclerView albumsRV;
    RecyclerView compilationsRV;
    RecyclerView singlesRV;
    TextView singlesTextView;
    TextView compilationsTextView;
    TextView albumsTextView;
    ConstraintLayout entireAQV;
    ProgressBar aqvProgressBar;
    Album latest;
    private View loadingPopUp;
    boolean isSpotifyInstalled;
    boolean isFacebookInstalled;
    boolean isTwitterInstalled;
    boolean isWikipediaInstalled;
    boolean isInstagramInstalled;
    String facebookURL;
    String twitterURL;
    String wikipediaURL;
    String instagramURL;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
    HistoryAdapter singleAdapter;
    HistoryAdapter albumAdapter;
    HistoryAdapter compilationAdapter;
    SpotifyService spotifyService;
    private Source source;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_quiz_view);

        loadingPopUp = findViewById(R.id.aqvSaving);

        artistNameTV = findViewById(R.id.aqvArtistName);
        artistBioTV = findViewById(R.id.aqvArtistDescription);
        artistPreviewImage = findViewById(R.id.aqvPreviewImage);
        spotify = findViewById(R.id.aqvSpotify);
        facebook = findViewById(R.id.aqvFacebook);
        twitter = findViewById(R.id.aqvTwitter);
        wikipedia = findViewById(R.id.aqvWikipedia);
        instagram = findViewById(R.id.aqvInstagram);
        share = findViewById(R.id.aqvShare);
        latestImage = findViewById(R.id.aqvTrackImage);
        latestTitle = findViewById(R.id.aqvTrackTitle);
        latestTitle.setSelected(true);
        latestType = findViewById(R.id.aqvTrackAlbum);
        latestYear = findViewById(R.id.aqvTrackYear);
        latestMiddleDot = findViewById(R.id.middleDotAfterAlbum);
        heartLatest = findViewById(R.id.album_heart);
        startQuiz = findViewById(R.id.aqvStartButton);
        latestText = findViewById(R.id.latestTextView);
        latestRelease = findViewById(R.id.latestRelease);
        albumsRV = findViewById(R.id.aqvAlbums);
        albumsRV.setNestedScrollingEnabled(false);
        compilationsRV = findViewById(R.id.aqvCompilations);
        compilationsRV.setNestedScrollingEnabled(false);
        singlesRV = findViewById(R.id.aqvSingles);
        singlesRV.setNestedScrollingEnabled(false);
        singlesTextView = findViewById(R.id.singlesTextView);
        spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        compilationsTextView = findViewById(R.id.compilationsTextView);
        albumsTextView = findViewById(R.id.albumsTextView);
        entireAQV = findViewById(R.id.entireAQVConstraintLayout);
        aqvProgressBar = findViewById(R.id.aqvProgressBar);

        PackageManager pm = getPackageManager();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            artist = (Artist) extras.getSerializable("currentArtist");
            source = (Source) extras.getSerializable("source");
            user = (User) extras.getSerializable("currentUser");
        }
        Activity context = this;

        spotify.setOnClickListener(new View.OnClickListener() {
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
                    intent.setData(Uri.parse(artist.getId()));
                    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + getBaseContext().getPackageName()));
                    startActivity(intent);
                } else {
                    String url = getArtistIdAsSpotifyUrl(artist.getId());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                }
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("com.facebook.katana", 0);
                    isFacebookInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isFacebookInstalled = false;
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(facebookURL));
                startActivity(browserIntent);
/*
                if(isFacebookInstalled)
                {
                    //TODO:Test opening page in facebook app
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + facebookURL));
                    startActivity(intent);
                }
                else
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(facebookURL));
                    startActivity(browserIntent);
                }

 */
            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("com.twitter.android", 0);
                    isTwitterInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isTwitterInstalled = false;
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(twitterURL));
                startActivity(browserIntent);
/*
                if(isTwitterInstalled)
                {
                    //TODO:Figure out how to open page in twitter app and test
                }
                else
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(twitterURL));
                    startActivity(browserIntent);
                }

 */
            }
        });

        wikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("org.wikipedia", 0);
                    isWikipediaInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isWikipediaInstalled = false;
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(wikipediaURL));
                startActivity(browserIntent);
/*
                if(isWikipediaInstalled)
                {
                    //TODO:Figure out how to open page in wiki app and test
                }
                else
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(wikipediaURL));
                    startActivity(browserIntent);
                }

 */
            }
        });

        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("com.instagram.android", 0);
                    isInstagramInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isInstagramInstalled = false;
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(instagramURL));
                startActivity(browserIntent);
/*
                if(isInstagramInstalled)
                {
                    //TODO:Figure out how to open page in instagram app and test
                }
                else
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(instagramURL));
                    startActivity(browserIntent);
                }

 */
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getArtistIdAsSpotifyUrl(artist.getId()));
                shareIntent.putExtra(Intent.EXTRA_TITLE, "Share Spotify Artist");
                //TODO: Add MQP logo to share menu when available.
                // Below we're passing a content URI to an image to be displayed
                //sendIntent.setData(mqpLogoUri);
                //sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("text/*");
                startActivity(Intent.createChooser(shareIntent, null));
            }
        });

        heartLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updatePopUpText(heartLatest.isChecked());
                            showPopUp();

                            HeartResponse response = null;
                            if (heartLatest.isChecked()) {
                                response = AlbumService.heart(firebaseUser, reference, latest, spotifyService,
                                        () -> hidePopUp());
                            } else {
                                response = AlbumService.unheart(firebaseUser, reference, latest, () -> hidePopUp());
                            }
                            if (response != HeartResponse.OK) {
                                heartLatest.setChecked(false);
                                hidePopUp();
                                HeartResponse finalResponse = response;
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlbumService.showError(finalResponse, context);
                                    }
                                });
                            }


                        }
                    }).start();
                } else {
                    SignUpPopUp signUpPopUp = new SignUpPopUp(getParent(), getBaseContext(), "Get Up And Dance! You Can Save This Album By Joining");
                    signUpPopUp.createAndShow();
                }
            }

        });
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
                        : R.string.renoving_message
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        SpotifyService spotifyService = new SpotifyService(getString(R.string.SPOTIFY_KEY));
        CountDownLatch cdl = new CountDownLatch(2);
        Activity activity = this;
        Context context = this;
        new Thread(new Runnable() {
            public void run() {

                if (source != Source.SEARCH) {

                    artist.initCollections(reference, user);
                    cdl.countDown();
                    artist.initTracks(reference, user);
                    cdl.countDown();

                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    artist = spotifyService.artistOverview(artist.getId());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        artistNameTV.setText(artist.getName());
                        artistBioTV.setText(artist.getBio());


                        Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl())).into(artistPreviewImage);
                        if (artist.getExternalLinks() != null) {
                            initializeExternalLinkButtons();
                        }
                    }
                });

                latest = artist.getLatest();
                if (latest != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get().load(ItemService.getSmallestPhotoUrl(latest.getPhotoUrl())).into(latestImage);
                        }
                    });
                    latestTitle.setText(latest.getName());
                    latestType.setText(latest.getType().toString());
                    latestYear.setText(latest.getYear());
                    latestMiddleDot.setText(getString(R.string.middle_dot));
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            latestText.setVisibility(View.GONE);
                            latestRelease.setVisibility(View.GONE);
                        }
                    });

                }

                LogUtil log = new LogUtil("ArtistQuizView", "onStart");
                ExecutorService executorService = Executors.newFixedThreadPool(3);

                if (artist.getSingles().size() != 0) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            singleAdapter = new HistoryAdapter(user, null, artist.getSingles(), context, 2);
                            singleAdapter.setHidePopUp(() -> hidePopUp());
                            singleAdapter.setShowPopUp(() -> showPopUp());
                            singleAdapter.setUpdatePopUpTextTrue(() -> updatePopUpText(true));
                            singleAdapter.setUpdatePopUpTextFalse(() -> updatePopUpText(false));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    singlesRV.setAdapter(singleAdapter);
                                    singlesRV.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            singlesRV.setVisibility(View.GONE);
                            singlesTextView.setVisibility(View.GONE);
                        }
                    });
                }

                if (artist.getCompilations().size() != 0) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            compilationAdapter = new HistoryAdapter(user, null, artist.getCompilations(), context, 2);
                            compilationAdapter.setHidePopUp(() -> hidePopUp());
                            compilationAdapter.setShowPopUp(() -> showPopUp());
                            compilationAdapter.setUpdatePopUpTextTrue(() -> updatePopUpText(true));
                            compilationAdapter.setUpdatePopUpTextFalse(() -> updatePopUpText(false));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    compilationsRV.setAdapter(compilationAdapter);
                                    compilationsRV.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            compilationsRV.setVisibility(View.GONE);
                            compilationsTextView.setVisibility(View.GONE);
                        }
                    });
                }

                if (artist.getAlbums().size() != 0) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            albumAdapter = new HistoryAdapter(user, null, artist.getAlbums(), context, 2);
                            albumAdapter.setHidePopUp(() -> hidePopUp());
                            albumAdapter.setShowPopUp(() -> showPopUp());
                            albumAdapter.setUpdatePopUpTextTrue(() -> updatePopUpText(true));
                            albumAdapter.setUpdatePopUpTextFalse(() -> updatePopUpText(false));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    albumsRV.setAdapter(albumAdapter);
                                    albumsRV.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumsRV.setVisibility(View.GONE);
                            albumsTextView.setVisibility(View.GONE);
                        }
                    });
                }


                executorService.shutdown();

                try {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    log.e(e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aqvProgressBar.setVisibility(View.GONE);
                        entireAQV.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(context, "Gathering data. Please wait...", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                if (source == Source.SEARCH) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Artist artist = initArtist();

                            int size = artist.getTrackPoolSize();

                            if (size >= 15) {
                                goToQuiz(context);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(context, "Not enough data to start quiz. Heart more albums and try again.", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    }).start();

                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Artist artist = initArtist();
                            int size = artist.getTrackPoolSize();
                            if (size >= 15 && !artist.isInitializing()) {
                                goToQuiz(view.getContext());
                            } else {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(context, "Not enough data to start quiz. Heart more albums and try again.", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });


    }

    private Artist initArtist() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        artist.initCollections(reference, user);
        countDownLatch.countDown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDownLatch = new CountDownLatch(1);
        artist.initTracks(reference, user);
        countDownLatch.countDown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Artist artist = this.artist;
        return artist;
    }

    private void goToQuiz(Context context) {
        Intent intent = new Intent(context, ActiveQuiz.class);
        intent.putExtra("currentArtist", artist);
        intent.putExtra("currentUser", user);
        startActivity(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            if (source == Source.SEARCH) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        user = FirebaseService.checkDatabase(reference, "users", firebaseUser.getUid(), User.class);
                        String artistKey = null;
                        String artistValue = null;
                        for (Map.Entry<String, String> artistId : user.getArtistIds().entrySet()) {
                            if (artistId.getValue().equals(artist.getId())) {
                                artistKey = artistId.getKey();
                                artistValue = artistId.getValue();
                            }
                        }

//                String artistKey = reference.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
                        Intent intent = getIntent();

                        intent.putExtra("artistKey", artistKey);
                        intent.putExtra("artistId", artist.getId());
                        intent.putExtra("user", user);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }).start();
                return true;
            } else {
                finish();
                return super.onKeyUp(keyCode, event);
            }
//           return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {


    }

    private void initializeExternalLinkButtons() {
        for (ExternalLink link : artist.getExternalLinks()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (link.getType() == ExternalLinkType.FACEBOOK) {
                        facebook.setVisibility(View.VISIBLE);
                        facebookURL = link.getUrl();
                    } else if (link.getType() == ExternalLinkType.INSTAGRAM) {
                        instagram.setVisibility(View.VISIBLE);
                        instagramURL = link.getUrl();
                    } else if (link.getType() == ExternalLinkType.TWITTER) {
                        twitter.setVisibility(View.VISIBLE);
                        twitterURL = link.getUrl();
                    } else if (link.getType() == ExternalLinkType.WIKIPEDIA) {
                        wikipedia.setVisibility(View.VISIBLE);
                        wikipediaURL = link.getUrl();
                    }
                }
            });

        }
    }

    public String getArtistIdAsSpotifyUrl(String artistId) {
        String id = artistId.substring(15);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/artist/%s", id);
    }

}