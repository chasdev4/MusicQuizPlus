package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import model.Badge;
import model.GoogleSignIn;
import model.Quiz;
import model.Results;
import model.User;
import model.Xp;
import model.type.BadgeType;
import service.BadgeService;
import service.FirebaseService;
import utils.FormatUtil;

public class QuizResults extends AppCompatActivity {

    private TextView accuracy;
    private TextView score;
    private TextView earnedXp;
    private Button continueButton;
    private TextView xpLeft;
    private TextView xpRight;
    private ImageView avatar;
    private TextView level;
    private ProgressBar xpBar;
    private User user;
    private TextView badgesLabel;
    private RecyclerView badges;
    private Results results;
    private ValueAnimator valueAnimator;
    private ResultsBadgesAdapter badgesAdapter;
    private int levelsCount;
    private int xpCount;
    private boolean overlap;
    private Xp xp;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);


        accuracy = findViewById(R.id.results_accuracy);
        score = findViewById(R.id.results_score);
        continueButton = findViewById(R.id.results_continue_button);
        earnedXp = findViewById(R.id.results_earned_xp);
        xpLeft = findViewById(R.id.results_xp_left);
        xpRight = findViewById(R.id.results_xp_right);
        avatar = findViewById(R.id.userCustomAvatar);
        xpBar = findViewById(R.id.xp_progress_bar);
        level = findViewById(R.id.userLevel);
        badgesLabel = findViewById(R.id.results_badges_label);
        badges = findViewById(R.id.results_badges_container);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ParentOfFragments.class);
                startActivity(intent);
            }
        });


    }

    private void setupValueAnimator() {
        if (levelsCount < xp.getLevelsProgressed() && !overlap) {
            overlap = true;
        }
        int min = 0;
        if (overlap) {
            xpCount = xp.getCurrentXp();
            xpCount -= xp.getLevels().get(xp.getCurrentLevel() - levelsCount);
        }
        else if (xp.getLevelsProgressed() > 0) {
            min = xp.getPreviousXp();
        }
        levelsCount--;

        if (levelsCount >= 0) {
            valueAnimator = ValueAnimator.ofInt(min, xpBar.getMax());
        }
        else {
            int max = xpCount;
            valueAnimator = ValueAnimator.ofInt(min, max);
        }
        if (overlap) {
            xpBar.setProgress(0);
            valueAnimator.setStartDelay(0);
        }
        else {
            valueAnimator.setStartDelay(1000);
        }
        valueAnimator.setDuration(3000);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (levelsCount >= 0) {
                    int left = xp.getLevels().get(xp.getCurrentLevel() - levelsCount);
                    int right = xp.getLevels().get(xp.getCurrentLevel() + 1 - levelsCount);
                    xpLeft.setText(FormatUtil.formatNumberWithComma(left));
                    xpRight.setText(FormatUtil.formatNumberWithComma(right));
                    xpBar.setMax(right - left);
                    if (firebaseUser != null) {
                        level.setText("Lvl. " + String.valueOf(xp.getCurrentLevel() - levelsCount));

                    }
                    setupValueAnimator();
                }

            }
        });

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                xpBar.setProgress((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

    }

    @Override
    public void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {


            @Override
            public void run() {
                xpBar.setProgress(xp.getPreviousXp());
                xpBar.setMax((xp.getLevels().get(xp.getPreviousLevel() + 1)) - xp.getLevels().get(xp.getPreviousLevel()));
                xpCount = results.getXp();
                levelsCount = xp.getLevelsProgressed();
                xpLeft.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel())));
                xpRight.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel() + 1)));
                setupValueAnimator();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//
        /*
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            new Thread(new Runnable() {
                public void run() {
                    //Testing Results Model
                    Results results = (Results) extras.getSerializable("quizResults");
                    score = results.getScore();
                    accuracy = results.getAccuracy();
                    String scoreString = String.valueOf(score);
                    userScore.setText(scoreString);
                    userAccuracy.setText(accuracy);
                }
            }).start();
        }
         */

        GoogleSignIn googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            results = (Results) extras.getSerializable("quizResults");
            user = results.getUser();
            xp = results.getXpBar();
        }
        if (firebaseUser != null) {
            Picasso.get().load(firebaseUser.getPhotoUrl()).placeholder(R.drawable.default_avatar).into(avatar);
            level.setText("Lvl. " + String.valueOf(xp.getPreviousLevel()));

        } else {
            Picasso.get().load(R.drawable.default_avatar).into(avatar);
            level.setText(getString(R.string.guest));
        }
        Uri photoUrl = firebaseUser != null ? firebaseUser.getPhotoUrl() : null;
        Picasso.get().load(photoUrl).placeholder(R.drawable.default_avatar).into(avatar);

        score.setText(FormatUtil.formatNumberWithComma(results.getScore()));
        accuracy.setText(results.getAccuracy());


        earnedXp.setText("+" + FormatUtil.formatNumberWithComma(results.getXp()) + " XP");
        setupBadges();

    }

    private void setupBadges() {
        badgesAdapter = new ResultsBadgesAdapter(((Context) this), results.getBadges());
        badgesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onDataChange();
            }
        });

        badges.setLayoutManager(new LinearLayoutManager(this));
        badges.setHasFixedSize(true);

        badges.setAdapter(badgesAdapter);

        onDataChange();
    }


    private void onDataChange() {
        if (badgesAdapter.getItemCount() == 0) {
            badgesLabel.setVisibility(View.GONE);
            badges.setVisibility(View.GONE);
        } else {
            badgesLabel.setVisibility(View.VISIBLE);
            badges.setVisibility(View.VISIBLE);
        }
    }
}