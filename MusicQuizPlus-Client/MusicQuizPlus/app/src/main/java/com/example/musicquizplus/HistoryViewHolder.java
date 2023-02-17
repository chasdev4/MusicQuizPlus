package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryViewHolder extends RecyclerView.ViewHolder {
    TextView trackTitle;
    TextView trackAlbum;
    TextView trackArtist;
    TextView trackYear;
    ImageView albumCover;
    View view;

    HistoryViewHolder(View itemView)
    {
        super(itemView);
        trackTitle = itemView.findViewById(R.id.quizViewTrackTitle);
        trackArtist = itemView.findViewById(R.id.quizViewTrackArtist);
        trackAlbum = itemView.findViewById(R.id.quizViewTrackAlbum);
        trackYear = itemView.findViewById(R.id.quizViewTrackYear);
        albumCover = itemView.findViewById(R.id.quizViewPreviewImage);
        view  = itemView;
    }
}