package com.example.musicquizplus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import service.FirebaseService;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;
import service.firebase.PlaylistService;
import service.firebase.UserService;
import utils.FormatUtil;
import utils.LogUtil;

public class ParentOfFragments extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MediaPlayer mediaPlayer;
    private ToggleButton muteButton, toolTipsToggleButton;
    private TextView userLevel;
    private ImageView userCustomAvatar, invisibleImageRight, invisibleGridView, invisibleImageLeft;
    private ImageButton backToTop;
    private Button pageTitle;
    private ImageButton settingsButton;
    private ImageButton searchButton;
    private ConstraintLayout root;
    View userAvatar;
    Boolean showToolTipsBool;

    private View.OnClickListener playlistsBackToTopListener;
    private View.OnClickListener artistsBackToTopListener;
    private View.OnClickListener historyBackToTopListener;

    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private User user;
    private GoogleSignIn googleSignIn = new GoogleSignIn();
    private Map<String, String> defaultPlaylistIds;

    private boolean ignoreMuteAction;
    private ToolTipsManager toolTipsManager;
    private ToolTip.Builder builder;
    private int playlistTrack, artistTrack, historyTrack;
    public boolean restart;
    public int playlistFragToolTips, artistFragToolTips, historyFragToolTips;
    private String currentDate, playlistFragToolTipsDate, artistFragToolTipsDate, historyFragToolTipsDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_of_fragments);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = df.format(c);

        playlistTrack = 0;
        artistTrack = 0;
        historyTrack = 0;
        ignoreMuteAction = true;
        toolTipsManager = new ToolTipsManager();
        invisibleImageRight = findViewById(R.id.invisibleImageRight);
        invisibleImageLeft = findViewById(R.id.invisibleImageLeft);
        root = findViewById(R.id.parentOfFragsRoot);
        invisibleGridView = findViewById(R.id.invisibleGridView);
        toolTipsToggleButton = findViewById(R.id.toolTipsToggleButton);

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

        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        backToTop = findViewById(R.id.backToTop);

        searchButton = findViewById(R.id.mainSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    Intent intent = new Intent(view.getContext(), SearchActivity.class);
                    intent.putExtra("user", user);
                    view.getContext().startActivity(intent);
                }
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
                        toolTipsManager.dismissAll();

                        break;
                    case 1:
                        if (artistsBackToTopListener != null) {
                            backToTop.setOnClickListener(artistsBackToTopListener);
                        }
                        toolTipsManager.dismissAll();

                        if(showToolTipsBool && !currentDate.equals(artistFragToolTipsDate))
                        {
                            startArtistFragmentToolTips();
                            artistFragToolTips++;
                            artistFragToolTipsDate = currentDate;
                        }
                        break;
                    case 2:
                        if (historyBackToTopListener != null) {
                            backToTop.setOnClickListener(historyBackToTopListener);
                        }
                        toolTipsManager.dismissAll();

                        if(showToolTipsBool && !currentDate.equals(historyFragToolTipsDate))
                        {
                            startHistoryFragmentToolTips();
                            historyFragToolTips++;
                            historyFragToolTipsDate = currentDate;
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

        toolTipsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toolTipsToggleButton.isChecked())
                {
                    restart = true;
                    playlistFragToolTips = 0;
                    artistFragToolTips = 0;
                    historyFragToolTips = 0;

                    showToolTipsBool = true;
                    Toast.makeText(getBaseContext(), "Helping Hints Turned On", Toast.LENGTH_SHORT).show();

                    if(tabLayout.getSelectedTabPosition() == 0)
                    {
                        playlistTrack = 0;
                        startPlaylistFragmentToolTips();
                        playlistFragToolTips++;
                        playlistFragToolTipsDate = currentDate;
                    }
                    else if(tabLayout.getSelectedTabPosition() == 1)
                    {
                        artistTrack = 0;
                        startArtistFragmentToolTips();
                        artistFragToolTips++;
                        artistFragToolTipsDate = currentDate;
                    }
                    else if(tabLayout.getSelectedTabPosition() == 2)
                    {
                        historyTrack = 0;
                        startHistoryFragmentToolTips();
                        historyFragToolTips++;
                        historyFragToolTipsDate = currentDate;
                    }
                }
                else
                {
                    toolTipsManager.dismissAll();
                    showToolTipsBool = false;
                    Toast.makeText(getBaseContext(), "Helping Hints Turned Off", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startPlaylistFragmentToolTips()
    {
        toolTipsManager.dismissAll();

        if(muteButton.getVisibility() != View.VISIBLE && settingsButton.getVisibility() != View.VISIBLE)
        {
            muteButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
        }

        if(tabLayout.getSelectedTabPosition() == 0 && toolTipsToggleButton.isChecked() && playlistFragToolTips < 3)
        {
            if(playlistTrack == 0)
            {
                if(firebaseUser != null)
                {
                    builder = new ToolTip.Builder(this, userCustomAvatar, root, "Click Here To\nView Your Profile", ToolTip.POSITION_BELOW);
                    builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                    builder.setAlign(ToolTip.ALIGN_RIGHT);
                    builder.setTextAppearance(R.style.TooltipTextAppearance);
                    toolTipsManager.show(builder.build());
                    playlistTrack++;
                    new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
                }
                else
                {
                    builder = new ToolTip.Builder(this, userCustomAvatar, root, "Click Here To\nCreate An Account", ToolTip.POSITION_BELOW);
                    builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                    builder.setAlign(ToolTip.ALIGN_RIGHT);
                    builder.setTextAppearance(R.style.TooltipTextAppearance);
                    toolTipsManager.show(builder.build());
                    playlistTrack++;
                    new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
                }
            }
            else if(playlistTrack == 1)
            {
                builder = new ToolTip.Builder(this, invisibleGridView, root, "Click A Playlist To Be Quizzed On", ToolTip.POSITION_ABOVE);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                playlistTrack++;
                new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
            }
            else if(playlistTrack == 2)
            {
                builder = new ToolTip.Builder(this, invisibleImageRight, root, "Swipe From Right To Left For Artist View", ToolTip.POSITION_LEFT_TO);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                playlistTrack++;
                new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
            }
            else if(playlistTrack == 3)
            {
                builder = new ToolTip.Builder(this, searchButton, root, "Search for Your Favorite Music", ToolTip.POSITION_LEFT_TO);
                builder.setAlign(ToolTip.ALIGN_CENTER);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                playlistTrack++;
                new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
            }
            else if(playlistTrack == 4)
            {
                muteButton.setVisibility(View.INVISIBLE);
                settingsButton.setVisibility(View.INVISIBLE);
                builder = new ToolTip.Builder(this, toolTipsToggleButton, root, "Click To Toggle Hints On or Off", ToolTip.POSITION_LEFT_TO);
                builder.setAlign(ToolTip.ALIGN_LEFT);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                playlistTrack++;
                new Handler().postDelayed(this::startPlaylistFragmentToolTips, 3000);
            }
        }
    }

    private void startArtistFragmentToolTips()
    {
        toolTipsManager.dismissAll();

        if(tabLayout.getSelectedTabPosition() == 1 && toolTipsToggleButton.isChecked() && artistFragToolTips < 3)
        {
            if(artistTrack == 0)
            {
                builder = new ToolTip.Builder(this, invisibleGridView, root, "See Your Saved Artists Here", ToolTip.POSITION_ABOVE);
                builder.setAlign(ToolTip.ALIGN_CENTER);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                artistTrack++;
                new Handler().postDelayed(this::startArtistFragmentToolTips, 3000);
            }
            else if(artistTrack == 1)
            {
                builder = new ToolTip.Builder(this, invisibleImageRight, root, "Swipe From Right To Left For History View", ToolTip.POSITION_LEFT_TO);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                artistTrack++;
                new Handler().postDelayed(this::startArtistFragmentToolTips, 3000);
            }
            else if(artistTrack == 2)
            {
                builder = new ToolTip.Builder(this, searchButton, root, "Search For An Artist\nTo Save For Quizzing", ToolTip.POSITION_LEFT_TO);
                builder.setAlign(ToolTip.ALIGN_CENTER);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                artistTrack++;
                new Handler().postDelayed(this::startArtistFragmentToolTips, 3000);
            }
            else if(artistTrack == 3)
            {
                builder = new ToolTip.Builder(this, invisibleImageLeft, root, "Swipe From Left To Right For Playlist View", ToolTip.POSITION_RIGHT_TO);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                artistTrack++;
                new Handler().postDelayed(this::startArtistFragmentToolTips, 3000);
            }
        }
    }

    private void startHistoryFragmentToolTips()
    {
        toolTipsManager.dismissAll();

        if(tabLayout.getSelectedTabPosition() == 2 && toolTipsToggleButton.isChecked() && historyFragToolTips < 3)
        {
            if(historyTrack == 0)
            {
                builder = new ToolTip.Builder(this, invisibleGridView, root, "See Your Track History From\nPrevious Quizzes Here", ToolTip.POSITION_ABOVE);
                builder.setAlign(ToolTip.ALIGN_CENTER);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                historyTrack++;
                new Handler().postDelayed(this::startHistoryFragmentToolTips, 3000);
            }
            else if(historyTrack == 1)
            {
                builder = new ToolTip.Builder(this, invisibleImageLeft, root, "Swipe From Left To Right For Artists View", ToolTip.POSITION_RIGHT_TO);
                builder.setAlign(ToolTip.ALIGN_CENTER);
                builder.setBackgroundColor(getResources().getColor(R.color.mqBlue));
                builder.setTextAppearance(R.style.TooltipTextAppearance);
                toolTipsManager.show(builder.build());
                historyTrack++;
                new Handler().postDelayed(this::startHistoryFragmentToolTips, 3000);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        restart = false;

        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("ToolTipsData", MODE_PRIVATE);
        showToolTipsBool = sh.getBoolean("showToolTipsBool", true);
        playlistFragToolTips = sh.getInt("playlistFragToolTips", 0);
        artistFragToolTips = sh.getInt("artistFragToolTips", 0);
        historyFragToolTips = sh.getInt("historyFragToolTips", 0);
        playlistFragToolTipsDate = sh.getString("playlistFragToolTipsDate", "");
        artistFragToolTipsDate = sh.getString("artistFragToolTipsDate", "");
        historyFragToolTipsDate = sh.getString("historyFragToolTipsDate", "");
        int pqvNum = sh.getInt("pqvToolTips", 0);
        int aqvNum = sh.getInt("aqvToolTips", 0);
        int searchNum = sh.getInt("searchToolTips", 0);

        if(playlistFragToolTips == 3 && artistFragToolTips == 3 && historyFragToolTips == 3 && pqvNum == 3 && aqvNum == 3 && searchNum == 3)
        {
            showToolTipsBool = false;
        }

        if(showToolTipsBool)
        {
            toolTipsToggleButton.setChecked(true);
            if(!currentDate.equals(playlistFragToolTipsDate))
            {
                new Handler().postDelayed(this::startPlaylistFragmentToolTips, 2500);
                playlistFragToolTips++;
                playlistFragToolTipsDate = currentDate;
            }
        }
        else
        {
            toolTipsToggleButton.setChecked(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();

        // Creating a shared pref object
        SharedPreferences sharedPreferences = getSharedPreferences("ToolTipsData", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        if(restart)
        {
            if(playlistFragToolTips == 1)
            {
                playlistFragToolTipsDate = currentDate;
            }
            else
            {
                playlistFragToolTipsDate = "";
            }
            artistFragToolTipsDate = "";
            historyFragToolTipsDate = "";
            myEdit.putInt("pqvToolTips", 0);
            myEdit.putString("pqvToolTipsDate", "");
            myEdit.putInt("aqvToolTips", 0);
            myEdit.putString("aqvToolTipsDate", "");
            myEdit.putInt("searchToolTips", 0);
            myEdit.putString("searchToolTipsDate", "");
        }

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putInt("playlistFragToolTips", playlistFragToolTips);
        myEdit.putInt("artistFragToolTips", artistFragToolTips);
        myEdit.putInt("historyFragToolTips", historyFragToolTips);
        myEdit.putString("playlistFragToolTipsDate", playlistFragToolTipsDate);
        myEdit.putString("artistFragToolTipsDate", artistFragToolTipsDate);
        myEdit.putString("historyFragToolTipsDate", historyFragToolTipsDate);
        myEdit.putBoolean("showToolTipsBool", showToolTipsBool);
        myEdit.apply();
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
                        CountDownLatch cdl = new CountDownLatch(1);
                        defaultPlaylistIds = PlaylistService.getDefaultPlaylistIds(db);
                        user = UserService.createUser(firebaseUser, db, defaultPlaylistIds);

                        db.child("users").child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                cdl.countDown();
                            }
                        });

                        try {
                            cdl.await();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
                    user.initArtists(db, firebaseUser, false);
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