package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryViewHolder extends RecyclerView.ViewHolder {
    TextView historyTrackTitle;
    TextView historyAlbum;
    TextView historyArtist;
    TextView historyYear;
    ImageView historyPreviewImage;
    TextView playlistTrackTitle;
    TextView playlistAlbum;
    TextView playlistArtist;
    TextView playlistYear;
    ImageView playlistPreviewImage;
    View view;

    HistoryViewHolder(View itemView, int switchOn)
    {
        super(itemView);

        switch (switchOn)
        {
            case 0:
                //if switchOn is 0, its for history view
                historyTrackTitle = itemView.findViewById(R.id.historyTrackTitle);
                historyArtist = itemView.findViewById(R.id.historyArtist);
                historyAlbum = itemView.findViewById(R.id.historyAlbum);
                historyYear = itemView.findViewById(R.id.historyYear);
                historyPreviewImage = itemView.findViewById(R.id.historyPreviewImage);
                break;

            case 1:
                //if switchOn is 1, its for playlist quiz preview
                playlistTrackTitle = itemView.findViewById(R.id.quizViewTrackTitle);
                playlistArtist = itemView.findViewById(R.id.quizViewTrackArtist);
                playlistAlbum = itemView.findViewById(R.id.quizViewTrackAlbum);
                playlistYear = itemView.findViewById(R.id.quizViewTrackYear);
                playlistPreviewImage = itemView.findViewById(R.id.quizViewPreviewImage);
                break;
        }
        view  = itemView;
    }
}