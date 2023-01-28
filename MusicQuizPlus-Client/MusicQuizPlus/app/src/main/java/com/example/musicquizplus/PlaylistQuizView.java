package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.User;
import model.item.Playlist;
import model.item.Track;

public class PlaylistQuizView extends AppCompatActivity implements Serializable {

    ImageView coverImage;
    TextView title;
    TextView owner;
    ListView quizListView;
    Button startQuiz;
    Playlist playlist;
    PlaylistQuizAdapter playlistQuizAdapter;
    Handler mainHandler = new Handler();
    List<Track> playlistTracks = new ArrayList<>();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_quiz_view);

        coverImage = findViewById(R.id.playlistQuizViewCoverImage);
        title = findViewById(R.id.playlistQuizViewTitle);
        owner = findViewById(R.id.playlistQuizViewOwner);
        quizListView = findViewById(R.id.playlistQuizViewListView);
        startQuiz = findViewById(R.id.playlistQuizViewStartQuizButton);




/*
        for (String track : playlist.getTrackIds())
        {

            Track trackOfPlaylist = FirebaseService.checkDatabase(reference, "tracks", track, Track.class);
            playlistTracks.add(trackOfPlaylist);
            //DatabaseReference dbReference =FirebaseDatabase.getInstance().getReference("tracks");


            mDatabase.equalTo(track).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        Object val = dataSnapshot.getValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




            List<Track> finalPlaylistTracks = playlistTracks;
            dbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        Track dbTrack = dataSnapshot.getValue(Track.class);
                        if(Objects.equals(track, dbTrack.getId()))
                        {
                            finalPlaylistTracks.add(dbTrack);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        }

        playlistQuizAdapter = new PlaylistQuizAdapter(this, R.layout.playlist_quiz_listview_contents, playlistTracks);
        quizListView.setAdapter(playlistQuizAdapter);
*/
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
          //  playlistTracks = playlist.getTracksListFromMap();

            new FetchImage(playlist.getPhotoUrl().get(0).getUrl(), coverImage, title, playlist.getName(), mainHandler).start();
        }

       // final PlaylistQuiz[] playlistQuiz = new PlaylistQuiz[1];
        User user = new User();

        new Thread(new Runnable() {
            public void run() {

                playlist.initCollection(reference);
               // playlistQuiz[0] = new PlaylistQuiz(playlist, user, null, QuizType.PLAYLIST, null, null, 10);

            }
        }).start();


        Playlist finalPlaylist = playlist;

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActiveQuiz.class);
                finalPlaylist.getTracksListFromMap().clear();
                //intent.putExtra("currentPlaylist", finalPlaylist);
              //  intent.putExtra("playlistQuiz", playlistQuiz[0]);
                startActivity(intent);
            }
        });

    }

}