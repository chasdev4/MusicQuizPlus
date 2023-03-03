package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.Question;
import model.Quiz;
import model.Results;
import model.User;
import model.item.Artist;
import service.FirebaseService;
import service.firebase.UserService;

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

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ParentOfFragments.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                FirebaseUser firebaseUser = new GoogleSignIn().getAuth().getCurrentUser();
                user = (User) FirebaseService.checkDatabase(db,
                        "users", firebaseUser.getUid(), User.class);
                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.default_avatar).into(avatar);

                user.initCollections(db);
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Artist artist = user.getArtist("spotify:artist:2w9zwq3AktTeYYMuhMjju8");
                artist.initCollections(db, user);
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch = new CountDownLatch(1);
                artist.initTracks(db);
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                int i = 1;
                Random rnd = new Random();
                Quiz quiz = new Quiz(artist, user, db, firebaseUser);
                Question question = quiz.getFirstQuestion();
                quiz.start();
                while (question != null) {
                    i++;
                    CountDownLatch cdl = new CountDownLatch(1);
//                            int index = ((rnd.nextInt(2)+1) % 2 == 0) ? rnd.nextInt(4) : question.getAnswerIndex();
                    int index = question.getAnswerIndex();
                    try {
                        Thread.sleep(rnd.nextInt(1) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    question = quiz.nextQuestion(index);
                    cdl.countDown();

                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("TAG", "run: ");


                Results results = quiz.end();
                String scoreString = String.valueOf(results.getScore());
                score.setText(scoreString);
                accuracy.setText(results.getAccuracy());
                xpBar.setProgress(results.getPreviousXp());
                xpBar.setMax((int) user.getXpToNextLevel());
                ValueAnimator animator = ValueAnimator.ofInt(0, xpBar.getMax());
                animator.setDuration(3000);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        xpBar.setProgress((Integer) animation.getAnimatedValue());
                    }
                });
                animator.start();

                xpLeft.setText(String.valueOf((int) (user.getXpFromPreviousLevel())));
                xpRight.setText(String.valueOf((int) (user.getXpToNextLevel())));
            }
        });
//        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
//        GoogleSignIn googleSignIn = new GoogleSignIn();
//        FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();

//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {


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


//            }
//        }).start();

//        }
    }
}