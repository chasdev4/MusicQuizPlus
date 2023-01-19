package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import service.FirebaseService;

public class QuizView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_view);

        ImageView coverImage = findViewById(R.id.quizViewCoverImage);
        TextView title = findViewById(R.id.quizViewTitle);
        TextView owner = findViewById(R.id.quizViewOwner);
        ListView quizListView = findViewById(R.id.quizViewListView);

        Playlist playlist = null;
        PlaylistQuizAdapter playlistQuizAdapter = null;
        Handler mainHandler = new Handler();
        List<Track> playlistTracks = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


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
            playlistTracks = playlist.getTracks();

            new FetchImage(playlist.getPhotoUrl().get(0).getUrl(), coverImage, title, playlist.getName(), mainHandler).start();
        }

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
}