package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import model.item.Playlist;
import model.quiz.PlaylistQuiz;
import model.type.QuizType;

public class ActiveQuiz extends AppCompatActivity implements View.OnClickListener{

    Button answerA, answerB, answerC, answerD;

    int score = 0;
    int numQuestions;
    Playlist playlist;
    PlaylistQuiz playlistQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_quiz);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            playlist = (Playlist) extras.getSerializable("currentPlaylist");
        }
/*
        if(playlist.getTracks().size() > 10)
        {
            numQuestions = 10;
        }
        else if (playlist.getTracks().size() <= 10 && playlist.getTracks().size() > 5)
        {
            numQuestions = 5;
        }
        else
        {
            numQuestions = playlist.getTracks().size();
        }

 */

        //playlistQuiz = new PlaylistQuiz(playlist, null, null, QuizType.PLAYLIST, null, null, numQuestions);

        answerA = findViewById(R.id.answerA);
        answerB = findViewById(R.id.answerB);
        answerC = findViewById(R.id.answerC);
        answerD = findViewById(R.id.answerD);

        answerA.setOnClickListener(this);
        answerB.setOnClickListener(this);
        answerC.setOnClickListener(this);
        answerD.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

    }


}