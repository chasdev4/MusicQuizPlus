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
        trackTitle = itemView.findViewById(R.id.historyTrackTitle);
        trackArtist = itemView.findViewById(R.id.historyArtist);
        trackAlbum = itemView.findViewById(R.id.historyAlbum);
        trackYear = itemView.findViewById(R.id.historyYear);
        albumCover = itemView.findViewById(R.id.historyPreviewImage);
        view  = itemView;
    }
}