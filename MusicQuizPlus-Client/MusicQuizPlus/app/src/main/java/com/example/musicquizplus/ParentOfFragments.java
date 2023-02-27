package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import service.FirebaseService;

public class ParentOfFragments extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
    private MediaPlayer mediaPlayer;
    private ToggleButton muteButton;
    private TextView userLevel;
    private ImageView userCustomAvatar;
    private View playlistUserAvatar;
    private Button pageTitle;

    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_of_fragments);
        mediaPlayer = new MediaPlayer();

        muteButton = findViewById(R.id.embeddedVolume);
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (muteButton.isChecked()) {
//                    mediaPlayer.setVolume(100,100);
                    mediaPlayer.start();
                } else {
//                    mediaPlayer.setVolume(0, 0);
                    mediaPlayer.pause();

                }
            }
        });

        pageTitle = findViewById(R.id.page_title);
        userLevel = findViewById(R.id.userLevel);
        userCustomAvatar = findViewById(R.id.userCustomAvatar);
        playlistUserAvatar = findViewById(R.id.playlistUserAvatar);
        Activity activity = this;
        Context context = this;
                playlistUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseUser == null) {

                    SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, getString(R.string.user_profile_signup_header));
                    signUpPopUp.createAndShow();
                }
                else
                {
                    //pull up user profile
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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                pageTitle.setText(tab.getText());
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
    protected void onStart() {
        super.onStart();
        final String[] url = {null};
        final DataSnapshot[] dataSnapshot = {null};
        CountDownLatch cdl = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.child("menu_music").child("0").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataSnapshot[0] = snapshot;
                        cdl.countDown();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                url[0] = dataSnapshot[0].getValue(String.class);

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                String finalUrl = url[0];

                try {
                    mediaPlayer.setDataSource(finalUrl);
                    mediaPlayer.prepare();
                    if (muteButton.isChecked()) {
                        mediaPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (firebaseUser != null)
                {
                    new Thread(new Runnable() {
                        public void run() {
                            user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                            userLevel.setText(String.format(Locale.ENGLISH, "%s %d", getString(R.string.lvl), user.getLevel()));
                                    if(user.getPhotoUrl() != null)
                                    {
                                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.default_avatar).into(userCustomAvatar);
                                    }
                                }
                            });
                        }
                    }).start();
                }
                else {
                    userLevel.setText(getString(R.string.guest));
                }

            }



        }).start();
    }
}