package com.example.musicquizplus;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

public class HistoryViewHolder extends RecyclerView.ViewHolder {
    TextView historyTrackTitle;
    TextView historyAlbum;
    TextView historyArtist;
    TextView historyYear;
    ImageView historyPreviewImage;
    Button viewOnSpotify;
    Button shareTrack;
    TextView playlistTrackTitle;
    TextView playlistAlbum;
    TextView playlistArtist;
    TextView playlistYear;
    ToggleButton playlistAudio;
    RecyclerView recyclerView;
    ImageView aqvPreviewImage;
    TextView aqvAlbumTitle;
    TextView aqvAlbumType;
    TextView aqvAlbumYear;
    ToggleButton aqvHeartAlbum;

    View view;

    HistoryViewHolder(View itemView, int switchOn)
    {
        super(itemView);

        switch (switchOn)
        {
            case 0:
                //if switchOn is 0, its for history view
                historyTrackTitle = itemView.findViewById(R.id.historyTrackTitle);
                historyTrackTitle.setSelected(true);
                historyArtist = itemView.findViewById(R.id.historyArtist);
                historyAlbum = itemView.findViewById(R.id.historyAlbum);
                historyYear = itemView.findViewById(R.id.historyYear);
                historyPreviewImage = itemView.findViewById(R.id.historyPreviewImage);
                viewOnSpotify = itemView.findViewById(R.id.spotifyView);
                shareTrack = itemView.findViewById(R.id.shareTrack);
                break;

            case 1:
                //if switchOn is 1, its for playlist quiz preview
                playlistTrackTitle = itemView.findViewById(R.id.quizViewTrackTitle);
                playlistTrackTitle.setSelected(true);
                playlistArtist = itemView.findViewById(R.id.quizViewTrackArtist);
                playlistArtist.setSelected(true);
                playlistAlbum = itemView.findViewById(R.id.quizViewTrackAlbum);
                playlistAlbum.setSelected(true);
                playlistYear = itemView.findViewById(R.id.quizViewTrackYear);
                playlistAudio = itemView.findViewById(R.id.playSampleAudio);
                recyclerView = itemView.findViewById(R.id.pqvRecyclerView);
                break;

            case 2:
                //if switchOn is 2, its for artist quiz preview
                aqvPreviewImage = itemView.findViewById(R.id.aqvTrackImage);
                aqvAlbumTitle = itemView.findViewById(R.id.aqvTrackTitle);
                aqvAlbumType = itemView.findViewById(R.id.aqvTrackAlbum);
                aqvAlbumYear = itemView.findViewById(R.id.aqvTrackYear);
                aqvHeartAlbum = itemView.findViewById(R.id.album_heart);
        }
        view  = itemView;
    }
}