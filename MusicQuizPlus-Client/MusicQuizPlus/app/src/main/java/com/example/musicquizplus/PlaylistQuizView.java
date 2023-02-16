package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOError;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.Quiz;
import model.User;
import model.item.Playlist;
import model.item.Track;
import model.type.QuizType;
import service.FirebaseService;

public class PlaylistQuizView extends AppCompatActivity implements Serializable {

    ImageView coverImage;
    TextView title;
    TextView owner;
    RecyclerView listView;
    Button startQuiz;
    Playlist playlist;
    PlaylistQuizAdapter playlistQuizAdapter;
    Handler mainHandler = new Handler();
//    List<Track> playlistTracks = new ArrayList<>();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_quiz_view);

        coverImage = findViewById(R.id.pqvCoverImage);
        title = findViewById(R.id.pqvTitle);
        owner = findViewById(R.id.pqvPlaylistOwner);
        listView = findViewById(R.id.pqvRecyclerView);
        startQuiz = findViewById(R.id.pqvStartButton);

    }


    @Override
    public void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            playlist = (Playlist) extras.getSerializable("currentPlaylist");

            if(playlist.getName().length() >= 19)
            {
                title.setTextSize(16);
            }

            title.setText(playlist.getName());
            owner.setText(playlist.getOwner());
//            playlistTracks = playlist.getTracksListFromMap();

            new FetchImage(playlist.getPhotoUrl().get(0).getUrl(), coverImage, title, playlist.getName(), mainHandler).start();
        }

        final Quiz[] playlistQuiz = new Quiz[1];
        // TODO: Get the user from the root of the app
        // User user = new User();

        new Thread(new Runnable() {
            public void run() {

                playlist.initCollection(reference);
                // TODO: Pass in our DatabaseReference
               // playlistQuiz[0] = new Quiz(playlist, user, db, firebaseUser);

            }
        }).start();

        Playlist finalPlaylist = playlist;

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActiveQuiz.class);
                finalPlaylist.getTracks().clear();
                //intent.putExtra("currentPlaylist", finalPlaylist);
                intent.putExtra("playlistQuiz", playlistQuiz[0]);
                startActivity(intent);
            }
        });

    }

}