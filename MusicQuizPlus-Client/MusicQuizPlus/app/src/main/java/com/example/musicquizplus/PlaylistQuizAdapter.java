package com.example.musicquizplus;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import model.item.Track;

public class PlaylistQuizAdapter extends ArrayAdapter<Track> {

    Handler mainHandler = new Handler();

    List<Track> tracks;

    int custom_layout_id;

    public PlaylistQuizAdapter(@NonNull Context context, int resource, @NonNull List<Track> tracks) {
        super(context, resource, tracks);
        this.tracks = tracks;
        custom_layout_id = resource;
    }


    @Override public int getCount() {
        return tracks.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            // getting reference to the main layout and initializing
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(custom_layout_id, null);
        }

        // initializing the imageview and textview and setting data
        ImageView imageView = v.findViewById(R.id.quizViewPreviewImage);
        TextView textView = v.findViewById(R.id.quizViewTrackTitle);
        TextView qvTrackArtist = v.findViewById(R.id.quizViewTrackArtist);
        TextView qvTrackAlbum = v.findViewById(R.id.quizViewTrackAlbum);
        TextView qvTrackYear = v.findViewById(R.id.quizViewTrackYear);

        // get the item using the position param
        Track item = tracks.get(position);

        // TODO: Get Image from Album
        //String url = item.getPhotoUrl().get(0).getUrl();
        // TODO: Get album year
        //qvTrackYear.setText(item.getYear());
        String title = item.getName();
        // TODO: Commented out line of code below is broke
        //qvTrackArtist.setText(item.getArtistIds().get(0));
        qvTrackAlbum.setText(item.getAlbumId());

        if(title.length() >= 19)
        {
            textView.setTextSize(16);
        }

        textView.setText(title);

        //new FetchImage(null, imageView, textView, title, mainHandler).start();
        return v;
    }

}
