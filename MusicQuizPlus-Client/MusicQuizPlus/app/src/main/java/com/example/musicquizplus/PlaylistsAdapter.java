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

import model.item.Playlist;

public class PlaylistsAdapter extends ArrayAdapter<Playlist> {

    Handler mainHandler = new Handler();

    List<Playlist> playlists;

    int custom_layout_id;

    public PlaylistsAdapter(@NonNull Context context, int resource, @NonNull List<Playlist> playlists) {
        super(context, resource, playlists);
        this.playlists = playlists;
        custom_layout_id = resource;
    }


    @Override public int getCount() {
        return playlists.size();
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
        Playlist item = playlists.get(position);

        if (item.getPhotoUrl() != null || item.getPhotoUrl().size() > 0) {
            String url = item.getPhotoUrl().get(0).getUrl();
            String title = item.getName();

            if(title.length() >= 19)
            {
                textView.setTextSize(16);
            }

            new FetchImage(url, imageView, textView, title, mainHandler).start();
        }

        return v;
    }

}