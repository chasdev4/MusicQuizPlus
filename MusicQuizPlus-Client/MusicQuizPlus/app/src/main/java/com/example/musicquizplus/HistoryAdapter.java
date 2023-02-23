package com.example.musicquizplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import model.PhotoUrl;
import model.item.Track;
import service.ItemService;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    List<Track> trackList;
    int switchOn;
    Context context;
    View photoView;
    HistoryViewHolder viewHolder;
    Track track;
    MediaPlayer mediaPlayer = new MediaPlayer();
    //ClickListiner listiner;

    //public HistoryAdapter(List<Track> list, Context context,ClickListiner listiner)
    public HistoryAdapter(List<Track> trackList, Context context, int switchOn)
    {
        this.trackList = trackList;
        this.context = context;
        this.switchOn = switchOn;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        track = trackList.get(position);

        switch (switchOn)
        {
            case 0:
                //if switchOn is 0, its for history view

                viewHolder.historyTrackTitle.setText(track.getName());

                if (track.getArtistName().length() > 15)
                {
                    String artist = track.getArtistName().substring(0, 12) + "\u2026";
                    viewHolder.historyArtist.setText(artist);
                }
                else
                {
                    viewHolder.historyArtist.setText(track.getArtistName());
                }

                if (track.getAlbumName().length() > 15)
                {
                    String album = track.getAlbumName().substring(0, 12) + "\u2026";
                    viewHolder.historyAlbum.setText(album);
                }
                else
                {
                    viewHolder.historyAlbum.setText(track.getAlbumName());
                }

                viewHolder.historyYear.setText(track.getYear());
                Picasso.get().load(ItemService.getSmallestPhotoUrl(track.getPhotoUrl())).into(viewHolder.historyPreviewImage);

                viewHolder.viewOnSpotify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isSpotifyInstalled;
                        try {
                            context.getPackageManager().getPackageInfo("com.spotify.music", 0);
                            isSpotifyInstalled = true;
                        } catch (PackageManager.NameNotFoundException e) {
                            isSpotifyInstalled = false;
                        }

                        if(isSpotifyInstalled)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(trackList.get(viewHolder.getAdapterPosition()).getId()));
                            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.getPackageName()));
                            context.startActivity(intent);
                        }
                        else
                        {
                            String url = getTrackIdAsUrl(trackList.get(viewHolder.getAdapterPosition()).getId());
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                            browserIntent.setData(Uri.parse(url));
                            context.startActivity(browserIntent);
                        }
                    }
                });

                viewHolder.shareTrack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getTrackIdAsUrl(trackList.get(viewHolder.getAdapterPosition()).getId()));
                        shareIntent.putExtra(Intent.EXTRA_TITLE, "Share Spotify Track");
                        //TODO: Add MQP logo to share menu when available.
                        // Below we're passing a content URI to an image to be displayed
                        //sendIntent.setData(mqpLogoUri);
                        //sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        shareIntent.setType("text/*");
                        context.startActivity(Intent.createChooser(shareIntent, null));
                    }
                });

                break;
            case 1:
                //if switchOn is 1, its for playlist quiz preview
                viewHolder.playlistTrackTitle.setText(track.getName());
                viewHolder.playlistArtist.setText(track.getArtistName());
                viewHolder.playlistAlbum.setText(track.getAlbumName());
                viewHolder.playlistYear.setText(track.getYear());
                viewHolder.playlistAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            mediaPlayer.release();
                        }

                        mediaPlayer = new MediaPlayer();

                        if(viewHolder.playlistAudio.getDrawable().getConstantState().equals(context.getResources().getDrawable(R.drawable.stop_audio).getConstantState()))
                        {
                            viewHolder.playlistAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.play_audio));
                            //viewHolder.playlistAudio.setImageResource();
                        }
                        else
                        {
                            viewHolder.playlistAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.stop_audio));

                            mediaPlayer = playAudio(trackList.get(viewHolder.getAdapterPosition()).getPreviewUrl());
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return trackList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public String getTrackIdAsUrl(String trackId)
    {
        String id = trackId.substring(14);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/track/%s", id);
    }

    private MediaPlayer playAudio(String url)
    {
        MediaPlayer mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaPlayer;
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