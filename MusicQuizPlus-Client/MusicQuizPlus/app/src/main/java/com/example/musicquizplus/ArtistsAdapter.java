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

import java.util.ArrayList;
import java.util.List;

import model.item.Artist;

public class ArtistsAdapter extends ArrayAdapter<Artist> {

    Handler mainHandler = new Handler();

    List<Artist> artists;

    int custom_layout_id;

    public ArtistsAdapter(@NonNull Context context, int resource, @NonNull List<Artist> artists) {
        super(context, resource, artists);
        this.artists = artists;
        custom_layout_id = resource;
    }


    @Override public int getCount() {
        return artists.size();
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
        ImageView imageView = v.findViewById(R.id.gridViewCover);
        TextView textView = v.findViewById(R.id.gridViewName);

        // get the item using the position param
        Artist item = artists.get(position);

        String url = item.getPhotoUrl().get(0).getUrl();
        String title = item.getName();

        if(title.length() >= 19)
        {
            textView.setTextSize(16);
        }

        new FetchImage(url, imageView, textView, title, mainHandler).start();
        return v;
    }

}