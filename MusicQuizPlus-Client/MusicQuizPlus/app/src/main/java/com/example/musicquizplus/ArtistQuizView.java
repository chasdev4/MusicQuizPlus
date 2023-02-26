package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import model.ExternalLink;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.ExternalLinkType;
import service.ItemService;

public class ArtistQuizView extends AppCompatActivity {

    Artist artist;
    TextView artistNameTV;
    TextView artistBioTV;
    ImageView artistPreviewImage;
    ImageButton backButton;
    ImageButton spotify;
    ImageButton facebook;
    ImageButton twitter;
    ImageButton wikipedia;
    ImageButton instagram;
    ImageButton share;
    ImageView latestImage;
    TextView latestTitle;
    TextView latestType;
    TextView latestYear;
    TextView latestText;
    View latestRelease;

    Album latest;
    boolean isSpotifyInstalled;
    boolean isFacebookInstalled;
    boolean isTwitterInstalled;
    boolean isWikipediaInstalled;
    boolean isInstagramInstalled;
    String facebookURL;
    String twitterURL;
    String wikipediaURL;
    String instagramURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_quiz_view);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        artistNameTV = findViewById(R.id.aqvArtistName);
        artistBioTV = findViewById(R.id.aqvArtistDescription);
        artistPreviewImage = findViewById(R.id.aqvPreviewImage);
        backButton = findViewById(R.id.aqvBackButton);
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
        latestText = findViewById(R.id.latestTextView);
        latestRelease = findViewById(R.id.latestRelease);

        PackageManager pm = getPackageManager();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            artist = (Artist) extras.getSerializable("currentArtist");
            artistNameTV.setText(artist.getName());
            artistBioTV.setText(artist.getBio());
            Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl())).into(artistPreviewImage);
            if(artist.getExternalLinks() != null)
            {
                initializeExternalLinkButtons();
            }
            if(artist.getLatest() != null)
            {
                reference.child("albums").child(artist.getLatest()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        latest = (Album) snapshot.getValue(Album.class);
                        Picasso.get().load(ItemService.getSmallestPhotoUrl(latest.getPhotoUrl())).into(latestImage);
                        latestTitle.setText(latest.getName());
                        latestType.setText(latest.getType().toString());
                        latestYear.setText(latest.getYear());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else
            {
                latestText.setVisibility(View.GONE);
                latestRelease.setVisibility(View.GONE);
            }
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        spotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pm.getPackageInfo("com.spotify.music", 0);
                    isSpotifyInstalled = true;
                } catch (PackageManager.NameNotFoundException e) {
                    isSpotifyInstalled = false;
                }

                if(isSpotifyInstalled)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(artist.getId()));
                    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + getBaseContext().getPackageName()));
                    startActivity(intent);
                }
                else
                {
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
    }

    private void initializeExternalLinkButtons()
    {
        for(ExternalLink link : artist.getExternalLinks())
        {
            if(link.getType() == ExternalLinkType.FACEBOOK)
            {
                facebook.setVisibility(View.VISIBLE);
                facebookURL = link.getUrl();
            }
            else if(link.getType() == ExternalLinkType.INSTAGRAM)
            {
                instagram.setVisibility(View.VISIBLE);
                instagramURL = link.getUrl();
            }
            else if(link.getType() == ExternalLinkType.TWITTER)
            {
                twitter.setVisibility(View.VISIBLE);
                twitterURL = link.getUrl();
            }
            else if(link.getType() == ExternalLinkType.WIKIPEDIA)
            {
                wikipedia.setVisibility(View.VISIBLE);
                wikipediaURL = link.getUrl();
            }
        }
    }

    public String getArtistIdAsSpotifyUrl(String artistId)
    {
        String id = artistId.substring(15);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/artist/%s", id);
    }
}