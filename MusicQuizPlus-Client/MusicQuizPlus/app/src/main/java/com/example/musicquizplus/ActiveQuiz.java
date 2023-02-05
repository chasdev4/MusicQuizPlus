package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.Question;
import model.Quiz;
import model.item.Playlist;
import model.item.Track;
import model.type.QuestionType;
import model.type.QuizType;
import service.FirebaseService;

public class ActiveQuiz extends AppCompatActivity implements View.OnClickListener, Serializable {

    Button answerA, answerB, answerC, answerD;
    TextView currentQuestionType;
    Quiz quiz;
    QuestionType type;
    String[] answers;
    int index;
    String audioURL;
    MediaPlayer mediaPlayer;
    Question currentQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_quiz);

        answerA = findViewById(R.id.answerA);
        answerB = findViewById(R.id.answerB);
        answerC = findViewById(R.id.answerC);
        answerD = findViewById(R.id.answerD);
        currentQuestionType = findViewById(R.id.question);

        answerA.setOnClickListener(this);
        answerB.setOnClickListener(this);
        answerC.setOnClickListener(this);
        answerD.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            quiz = (Quiz) extras.getSerializable("playlistQuiz");
            currentQuestion = quiz.getFirstQuestion();
        }

        type = currentQuestion.getType();
        answers = currentQuestion.getAnswers();
        audioURL = currentQuestion.getPreviewUrl();

        mediaPlayer = playAudio(audioURL);

        currentQuestionType.setText(type.toString());
        answerA.setText(answers[0]);
        answerB.setText(answers[1]);
        answerC.setText(answers[2]);
        answerD.setText(answers[3]);
    }

    @Override
    public void onClick(View view) {

        pauseAudio(mediaPlayer);

        Button btnClicked = (Button) view;
        index = findIndex(btnClicked);

        currentQuestion = quiz.nextQuestion(index);

        if(currentQuestion == null)
        {
            Intent intent = new Intent(this, QuizResults.class);
            intent.putExtra("quiz", quiz);
            startActivity(intent);
        }
        else if (currentQuestion != null)
        {
            type = currentQuestion.getType();
            answers = currentQuestion.getAnswers();

            audioURL = currentQuestion.getPreviewUrl();
            mediaPlayer = playAudio(audioURL);

            currentQuestionType.setText(type.toString());
            answerA.setText(answers[0]);
            answerB.setText(answers[1]);
            answerC.setText(answers[2]);
            answerD.setText(answers[3]);
        }
    }

    private MediaPlayer playAudio(String Url)
    {
        MediaPlayer mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(audioURL);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaPlayer;
    }

    private void pauseAudio(MediaPlayer mediaPlayer)
    {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    private int findIndex(Button btnClicked)
    {
        if(btnClicked.getId() == R.id.answerA)
        {
            index = 0;
        }
        else if(btnClicked.getId() == R.id.answerB)
        {
            index = 1;
        }
        else if(btnClicked.getId() == R.id.answerC)
        {
            index = 2;
        }
        else if(btnClicked.getId() == R.id.answerD)
        {
            index = 3;
        }

        return index;
    }
}