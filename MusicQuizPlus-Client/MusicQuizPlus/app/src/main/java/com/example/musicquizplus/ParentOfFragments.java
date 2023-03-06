package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.musicquizplus.fragments.ArtistsFragment;
import com.example.musicquizplus.fragments.HistoryFragment;
import com.example.musicquizplus.fragments.PlaylistFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import model.Badge;
import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.type.BadgeType;
import service.BadgeService;
import service.FirebaseService;
import service.ItemService;
import utils.FormatUtil;
import utils.LogUtil;

public class ParentOfFragments extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
    private MediaPlayer mediaPlayer;
    private ToggleButton muteButton;
    private TextView userLevel;
    private ImageView userCustomAvatar;
    private ImageButton backToTop;
    private ImageButton searchButton;
    private View userAvatar;
    private Button pageTitle;
    private ImageButton helpButton;

    private View.OnClickListener playlistsBackToTopListener;
    private View.OnClickListener artistsBackToTopListener;
    private View.OnClickListener historyBackToTopListener;

    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private User user;

    private boolean ignoreMuteAction;
    private boolean mediaPlayerInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_of_fragments);
        ignoreMuteAction = true;

        Context context = this;
        helpButton = findViewById(R.id.embeddedHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Help dialog
            }
        });

        muteButton = findViewById(R.id.embeddedVolume);
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ignoreMuteAction) {
                    if (muteButton.isChecked()) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.pause();

                    }
                } else {
                    muteButton.setChecked(!muteButton.isChecked());
                }
            }
        });

        mediaPlayer = MediaPlayer.create(ParentOfFragments.this, R.raw.music);
        mediaPlayer.setLooping(true);
        if (muteButton.isChecked()) {
            mediaPlayer.start();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                ignoreMuteAction = false;
            }
        });

        pageTitle = findViewById(R.id.page_title);
        userLevel = findViewById(R.id.userLevel);
        userCustomAvatar = findViewById(R.id.userCustomAvatar);
        userAvatar = findViewById(R.id.home_user_avatar);
        Activity activity = this;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseUser == null) {

                    SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, getString(R.string.user_profile_signup_header));
                    signUpPopUp.createAndShow();
                } else {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }
        });

        GoogleSignIn googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        backToTop = findViewById(R.id.backToTop);

        searchButton = findViewById(R.id.mainSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SearchActivity.class);
                intent.putExtra("user", user);
                view.getContext().startActivity(intent);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                pageTitle.setText(tab.getText());

                switch (tab.getPosition()) {
                    case 0:
                        if (playlistsBackToTopListener != null) {
                            backToTop.setOnClickListener(playlistsBackToTopListener);
                        }
                        break;
                    case 1:
                        if (artistsBackToTopListener != null) {
                            backToTop.setOnClickListener(artistsBackToTopListener);
                        }
                        break;
                    case 2:
                        if (historyBackToTopListener != null) {
                            backToTop.setOnClickListener(historyBackToTopListener);
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //may not need
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (firebaseUser != null) {

                    user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
                    user.initArtists(db, false);
                    user.initBadges(db);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userLevel.setText(String.format(Locale.ENGLISH, "%s %d", getString(R.string.lvl), user.getLevel()));
                            if (user.getPhotoUrl() != null) {
                                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.default_avatar).into(userCustomAvatar);
                            }
                        }
                    });
                } else {
                    user = new User();
                    user.initGuest(activity);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userLevel.setText(getString(R.string.guest));
                        }
                    });
                }

            }
        }).start();
    }

    public void setPlaylistsBackToTopListener(View.OnClickListener playlistsBackToTopListener) {
        this.playlistsBackToTopListener = playlistsBackToTopListener;
    }

    public void setArtistsBackToTopListener(View.OnClickListener artistsBackToTopListener) {
        this.artistsBackToTopListener = artistsBackToTopListener;
    }

    public void setHistoryBackToTopListener(View.OnClickListener historyBackToTopListener) {
        this.historyBackToTopListener = historyBackToTopListener;
    }

    public boolean isBackToTopListenerSet() {
        return backToTop.hasOnClickListeners();
    }

    public ImageButton getBackToTop() {
        return backToTop;
    }

    public User getUser() { return user; }
}