package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import model.Badge;
import model.GoogleSignIn;
import model.Quiz;
import model.Results;
import model.User;
import service.FirebaseService;

public class QuizResults extends AppCompatActivity {

    TextView userAccuracy;
    TextView userScore;
    Button continueBtn;
    int score;
    String accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);

        userAccuracy = findViewById(R.id.userAccuracy);
        userScore = findViewById(R.id.userScore);
        continueBtn = findViewById(R.id.continueFromResults);

        continueBtn.setOnClickListener(new View.OnClickListener() {
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

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        GoogleSignIn googleSignIn = new GoogleSignIn();
        FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            new Thread(new Runnable() {
                public void run() {
                    //Testing Results Model
                    User user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    Quiz quiz = (Quiz) extras.getSerializable("quiz");
                    Results results = new Results(user, quiz);
                    List<Badge> earnedBadges = results.getEarnedBadges(getBaseContext());
                    score = results.getScore();
                    accuracy = results.getAccuracy();
                    String scoreString = String.valueOf(score);
                    userScore.setText(scoreString);
                    userAccuracy.setText(accuracy);
                }
            }).start();
        }
    }
}