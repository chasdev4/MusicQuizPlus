package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            score = extras.getInt("quizScore");
            accuracy = extras.getString("quizAccuracy");
        }

        String scoreString = String.valueOf(score);
        userScore.setText(scoreString);
        userAccuracy.setText(accuracy);

    }
}