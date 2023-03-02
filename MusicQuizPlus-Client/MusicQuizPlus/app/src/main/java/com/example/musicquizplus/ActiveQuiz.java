package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.PhotoUrl;
import model.Question;
import model.Quiz;
import model.Results;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.QuestionType;
import model.type.QuizType;
import service.FirebaseService;
import service.ItemService;

public class ActiveQuiz extends AppCompatActivity implements View.OnClickListener, Serializable {

    Button answerA, answerB, answerC, answerD;
    TextView currentQuestionType;
    ImageView quizImage;
    Quiz quiz;
    QuestionType type;
    List<String> answers;
    int index;
    String audioURL;
    MediaPlayer mediaPlayer;
    Question currentQuestion;
    User user;
    Playlist playlist;
    Artist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_quiz);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            user = (User) extras.getSerializable("currentUser");
            playlist = (Playlist) extras.getSerializable("currentPlaylist");
            artist = (Artist) extras.getSerializable("currentArtist");
        }

        if(artist != null)
        {
            //artist quiz
            quiz = new Quiz(artist, user);
        }
        else
        {
            //playlist quiz
            quiz = new Quiz(playlist, user);
        }

        answerA = findViewById(R.id.answerA);
        answerB = findViewById(R.id.answerB);
        answerC = findViewById(R.id.answerC);
        answerD = findViewById(R.id.answerD);
        currentQuestionType = findViewById(R.id.question);
        quizImage = findViewById(R.id.quizImage);

        answerA.setOnClickListener(this);
        answerB.setOnClickListener(this);
        answerC.setOnClickListener(this);
        answerD.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        currentQuestion = quiz.getFirstQuestion();
        type = currentQuestion.getType();
        answers = currentQuestion.getAnswers();
        audioURL = currentQuestion.getPreviewUrl();

        mediaPlayer = playAudio();

        currentQuestionType.setText(type.toString());
        answerA.setText(answers.get(0));
        answerB.setText(answers.get(1));
        answerC.setText(answers.get(2));
        answerD.setText(answers.get(3));
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
            Results results = quiz.end();
            intent.putExtra("quizResults", results);
            startActivity(intent);
        }
        else if (currentQuestion != null)
        {
            type = currentQuestion.getType();
            answers = currentQuestion.getAnswers();

            audioURL = currentQuestion.getPreviewUrl();
            mediaPlayer = playAudio();

            currentQuestionType.setText(type.toString());
            answerA.setText(answers.get(0));
            answerB.setText(answers.get(1));
            answerC.setText(answers.get(2));
            answerD.setText(answers.get(3));
        }
    }

    private MediaPlayer playAudio()
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