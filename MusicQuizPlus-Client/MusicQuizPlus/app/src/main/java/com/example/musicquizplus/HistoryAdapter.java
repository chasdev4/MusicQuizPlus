package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import model.item.Track;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    List<Track> list = new ArrayList<>();
    int switchOn;
    Context context;
    View photoView;
    HistoryViewHolder viewHolder;

    //ClickListiner listiner;

    //public ImageGalleryAdapter2(List<Track> list, Context context,ClickListiner listiner)
    public HistoryAdapter(List<Track> list, Context context, int switchOn)
    {
        this.list = list;
        this.context = context;
        this.switchOn = switchOn;
        //this.listiner = listiner;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (switchOn)
        {
            case 0:
                //if switchOn is 0, its for history view
                photoView = inflater.inflate(R.layout.history_listview_contents, parent, false);
                viewHolder = new HistoryViewHolder(photoView, 0);
                break;
            case 1:
                //if switchOn is 1, its for playlist quiz preview
                photoView = inflater.inflate(R.layout.playlist_quiz_listview_contents, parent, false);
                viewHolder = new HistoryViewHolder(photoView, 1);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder viewHolder, final int position)
    {
        switch (switchOn)
        {
            case 0:
                //if switchOn is 0, its for history view
                viewHolder.historyTrackTitle.setText(list.get(position).getName());
                viewHolder.historyArtist.setText(list.get(position).getArtistName());
                viewHolder.historyAlbum.setText(list.get(position).getAlbumName());
                viewHolder.historyYear.setText(list.get(position).getYear());
                break;
            case 1:
                //if switchOn is 1, its for playlist quiz preview
                viewHolder.playlistTrackTitle.setText(list.get(position).getName());
                viewHolder.playlistArtist.setText(list.get(position).getArtistName());
                viewHolder.playlistAlbum.setText(list.get(position).getAlbumName());
                viewHolder.playlistYear.setText(list.get(position).getYear());
                break;
        }
        //final index = viewHolder.getAdapterPosition();

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