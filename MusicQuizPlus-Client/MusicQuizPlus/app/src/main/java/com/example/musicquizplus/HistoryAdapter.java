package com.example.musicquizplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import model.item.Album;
import model.item.Track;
import service.FirebaseService;

public class HistoryAdapter extends RecyclerView.Adapter<historyViewHolder> {

    List<Track> list = new ArrayList<>();

    Context context;
    //ClickListiner listiner;

    //public ImageGalleryAdapter2(List<Track> list, Context context,ClickListiner listiner)
    public HistoryAdapter(List<Track> list, Context context)
    {
        this.list = list;
        this.context = context;
        //this.listiner = listiner;
    }

    @Override
    public historyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.playlist_quiz_listview_contents, parent, false);

        historyViewHolder viewHolder = new historyViewHolder(photoView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final historyViewHolder viewHolder, final int position)
    {
        //final index = viewHolder.getAdapterPosition();
        viewHolder.trackTitle.setText(list.get(position).getName());
        viewHolder.trackArtist.setText(list.get(position).getArtistName());
        viewHolder.trackAlbum.setText(list.get(position).getAlbumName());
        viewHolder.trackYear.setText(list.get(position).getYear());
        //Album tracksAlbum = FirebaseService.checkDatabase(FirebaseDatabase.getInstance().getReference(), "albums", list.get(position).getAlbumId(), Album.class);
        //Uri uri = Uri.parse(tracksAlbum.getPhotoUrl().get(0).getUrl());
        //viewHolder.albumCover.setImageURI(uri);

/*
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //listiner.click(index);
            }
        });
 */
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}






/*
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

public class HistoryAdapter extends ArrayAdapter<Track> {

    Handler mainHandler = new Handler();

    List<Track> tracks;

    int custom_layout_id;

    public HistoryAdapter(@NonNull Context context, int resource, @NonNull List<Track> tracks) {
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
        ImageView imageView = v.findViewById(R.id.gridViewCover);
        TextView textView = v.findViewById(R.id.gridViewName);

        // get the item using the position param
        Track item = tracks.get(position);

        //String url = item.getPhotoUrl().get(0).getUrl();
        String title = item.getName();


        if(title.length() >= 19)
        {
            textView.setTextSize(16);
        }

        new FetchImage(null, imageView, textView, title, mainHandler).start();
        return v;
    }

}

 */