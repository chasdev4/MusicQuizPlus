package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
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
    private RecyclerView badges;
    private Results results;
    private ValueAnimator valueAnimator;
    private ResultsBadgesAdapter badgesAdapter;

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
        badges = findViewById(R.id.results_badges_container);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ParentOfFragments.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Xp xp = results.getXpBar();

                xpBar.setProgress(xp.getPreviousXp());
                xpBar.setMax((xp.getLevels().get(xp.getPreviousLevel()+1)) - xp.getLevels().get(xp.getPreviousLevel()));
                valueAnimator = ValueAnimator.ofInt(xp.getPreviousXp(), xp.getLevels().get(xp.getCurrentLevel()+1) - xp.getCurrentXp());
                valueAnimator.setDuration(5000);
                valueAnimator.setStartDelay(1000);
                xpLeft.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel())));
                xpRight.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel()+1)));

                valueAnimator.addPauseListener(new Animator.AnimatorPauseListener() {
                    @Override
                    public void onAnimationPause(Animator animator) {

                    }

                    @Override
                    public void onAnimationResume(Animator animator) {

                    }
                });
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    int i = 0;
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (xpBar.getProgress() >= xpBar.getMax()) {
                            xpBar.setProgress(0);
                            xpLeft.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel()+i)));
                            xpRight.setText(FormatUtil.formatNumberWithComma(xp.getLevels().get(xp.getPreviousLevel()+i+1)));
                            xpBar.setMax((xp.getLevels().get(xp.getPreviousLevel() + i)) - xp.getLevels().get(xp.getPreviousLevel())+i);
                            i++;
                        }
                            xpBar.setProgress((Integer) animation.getAnimatedValue());
                    }
                });
                valueAnimator.start();


            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//

        // TODO: Get the user from the results or an intent

                Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = (User) extras.getSerializable("user");
}
        Picasso.get().load("https://lh3.googleusercontent.com/a/AEdFTp4zKQcEBLE0NQ9_exBatpU9TVwsnPygjk3StY9JiA=s96-c").placeholder(R.drawable.default_avatar).into(avatar);

        int scoreVal = 1200;
        int xp = 1600;
        int levelVal = 1;
        int previousXp = 0;
        int previousLevel = 1;
        String accuracyString = "8/10";

        results = new Results(scoreVal, xp, levelVal, previousXp, previousLevel, accuracyString, user.getBadgesAsList());
        score.setText(FormatUtil.formatNumberWithComma(results.getScore()));
        accuracy.setText(results.getAccuracy());

        level.setText("Lvl. 1");

        earnedXp.setText("+" + FormatUtil.formatNumberWithComma(xp) + " XP");
        setupBadges();

//        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
//        GoogleSignIn googleSignIn = new GoogleSignIn();
//        FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();


        //Testing Results Model
//                    User user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);

//                    Quiz quiz = (Quiz) extras.getSerializable("quiz");


//                    Quiz quiz = new Quiz(user.getArtist("spotify:artist:0cQbJU1aAzvbEmTuljWLlF"), user, db, firebaseUser);
//                    Question question = quiz.getFirstQuestion();
//                    quiz.start();
//                    Random rnd = new Random();
//                    while (quiz.nextQuestion(rnd.nextInt(2) % 2 == 0 ? question.getAnswerIndex() : 1) != null) {
//
//                    }

    }

    private void setupBadges() {
        badgesAdapter = new ResultsBadgesAdapter(((Context)this), results.getBadges());
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
            badges.setVisibility(View.GONE);
        }
        else {
            badges.setVisibility(View.VISIBLE);
        }
    }

}