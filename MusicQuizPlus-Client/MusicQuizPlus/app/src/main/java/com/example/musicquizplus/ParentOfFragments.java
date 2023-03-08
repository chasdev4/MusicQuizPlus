package com.example.musicquizplus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import service.FirebaseService;
import service.ItemService;
import service.firebase.PlaylistService;
import service.firebase.UserService;
import utils.FormatUtil;
import utils.LogUtil;

public class ParentOfFragments extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MediaPlayer mediaPlayer;
    private ToggleButton muteButton;
    private TextView userLevel;
    private ImageView userCustomAvatar;
    private ImageButton backToTop;
    private Button pageTitle;
    private ImageButton settingsButton;

    private View.OnClickListener playlistsBackToTopListener;
    private View.OnClickListener artistsBackToTopListener;
    private View.OnClickListener historyBackToTopListener;

    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private User user;
    private GoogleSignIn googleSignIn = new GoogleSignIn();
    private Map<String, String> defaultPlaylistIds;

    private boolean ignoreMuteAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_of_fragments);
        ignoreMuteAction = true;

        Context context = this;
        ImageButton helpButton = findViewById(R.id.embeddedHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "Help Button Is Coming Soon...", Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton = findViewById(R.id.embeddedSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "Settings Button Is Coming Soon...", Toast.LENGTH_SHORT).show();
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
        View userAvatar = findViewById(R.id.home_user_avatar);
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

        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        backToTop = findViewById(R.id.backToTop);

        ImageButton searchButton = findViewById(R.id.mainSearchButton);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignIn.onActivityResult(requestCode, resultCode, data, this);
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
                    if(user == null)
                    {
                        defaultPlaylistIds = PlaylistService.getDefaultPlaylistIds(db);
                        UserService.createUser(firebaseUser, db, defaultPlaylistIds);
                    }
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