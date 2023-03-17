package com.example.musicquizplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Album;
import model.item.Track;
import model.type.HeartResponse;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    List<Track> trackList;
    List<Album> albumList;
    int switchOn;
    Context context;
    View photoView;
    HistoryViewHolder viewHolder;
    Track track;
    Album album;
    MediaPlayer mediaPlayer = new MediaPlayer();
    int old;
    User user;
    private Track currentTrack;
    private Runnable showPopUp;
    private Runnable hidePopUp;
    private Runnable updatePopUpTextTrue;
    private Runnable updatePopUpTextFalse;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
    private boolean lastChoice;

    public HistoryAdapter(User user, List<Track> trackList, List<Album> albumList, Context context, int switchOn) {
        this.user = user;
        this.trackList = trackList;
        this.albumList = albumList;
        this.context = context;
        this.switchOn = switchOn;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (switchOn) {
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
            case 2:
                //if switchOn is 2, its for artist quiz preview
                photoView = inflater.inflate(R.layout.artist_quiz_contents, parent, false);
                viewHolder = new HistoryViewHolder(photoView, 2);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder viewHolder, int position) {

        switch (switchOn) {
            case 0:
                //if switchOn is 0, its for history view
                track = trackList.get(position);

                viewHolder.historyTrackTitle.setText(track.getName());

                if (track.getArtistName().length() > 15) {
                    String artist = track.getArtistName().substring(0, 12) + "\u2026";
                    viewHolder.historyArtist.setText(artist);
                } else {
                    viewHolder.historyArtist.setText(track.getArtistName());
                }

                if (track.getAlbumName().length() > 15) {
                    String album = track.getAlbumName().substring(0, 12) + "\u2026";
                    viewHolder.historyAlbum.setText(album);
                } else {
                    viewHolder.historyAlbum.setText(track.getAlbumName());
                }

                viewHolder.historyYear.setText(track.getYear());
                if (track.getPhotoUrl() != null) {
                    Picasso.get().load(ItemService.getSmallestPhotoUrl(track.getPhotoUrl())).placeholder(R.drawable.placeholder).into(viewHolder.historyPreviewImage);
                }
                else {
                    Picasso.get().load(R.drawable.placeholder).into(viewHolder.historyPreviewImage);
                }
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

                        if (isSpotifyInstalled) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(trackList.get(viewHolder.getAdapterPosition()).getId()));
                            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.getPackageName()));
                            context.startActivity(intent);
                        } else {
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
                track = trackList.get(position);

                if (((PlaylistQuizView)viewHolder.view.getContext()).isChecked()) {
                    viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleGreen));
                }
                else {
                    viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple2));
                }

                if (currentTrack != null && track != null) {
                    viewHolder.setTrackId(track.getId());
                    if (currentTrack.getId().equals(track.getId())) {
                        viewHolder.playlistAudio.setChecked(true);
                    } else {
                        viewHolder.playlistAudio.setChecked(false);
                    }
                }
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

                        int pos = viewHolder.getAdapterPosition();

                        mediaPlayer = new MediaPlayer();

                        if (viewHolder.playlistAudio.isChecked()) {
                            mediaPlayer = playAudio(trackList.get(viewHolder.getAdapterPosition()).getPreviewUrl());
                            currentTrack = trackList.get(pos);
                        }


                        if (old != pos) {

                            View v = ((PlaylistQuizView)context).listView.getLayoutManager().findViewByPosition(old);
                            try {
                                ((ToggleButton) v.findViewById(R.id.playSampleAudio)).setChecked(false);
                            }
                            catch (NullPointerException e) {
                                Log.e("HistoryAdapter.java", e.getMessage().toString() );
                            }
                            //notifyDataSetChanged();
                            //set image at position old to stop
                            //View v = viewHolder.recyclerView.getChildAt(old);
                            //View itemView =  viewHolder.recyclerView.findViewHolderForAdapterPosition(old).itemView;
                            //ImageButton btn = itemView.findViewById(R.id.playSampleAudio);
                            //btn.setImageDrawable(context.getResources().getDrawable(R.drawable.play_audio));
                            //viewHolder.playlistAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.play_audio));
                            //notifyItemChanged(old);
                        }

                        old = pos;
                    }
                });
                break;

            case 2:
                //if switchOn is 2, its for artist quiz preview
                Album album = albumList.get(viewHolder.getAdapterPosition());
                SpotifyService spotifyService = new SpotifyService(context.getString(R.string.SPOTIFY_KEY));

                Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl())).into(viewHolder.aqvPreviewImage);
                viewHolder.aqvAlbumTitle.setText(album.getName());
                viewHolder.aqvAlbumType.setText(album.getType().toString());
                viewHolder.aqvAlbumYear.setText(album.getYear());
                if (user != null) {
                    lastChoice = user.getAlbumIds().containsValue(album.getId());
                    viewHolder.aqvHeartAlbum.setChecked(lastChoice);
                    if (lastChoice) {
                        viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleRed));
                    }
                    else {
                        viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple2));
                    }
                } else {
                    lastChoice = false;
                    viewHolder.aqvHeartAlbum.setChecked(false);
                }
                viewHolder.aqvHeartAlbum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (user != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (viewHolder.aqvHeartAlbum.isChecked()) {
                                        updatePopUpTextTrue.run();
                                    }
                                    else {
                                        updatePopUpTextFalse.run();
                                    }
                                    showPopUp.run();
                                    HeartResponse response = null;
                                    if (viewHolder.aqvHeartAlbum.isChecked()) {
                                        response = AlbumService.heart(user, firebaseUser, reference, album, spotifyService, hidePopUp);
                                          if (response != HeartResponse.OK) {
                                              viewHolder.aqvHeartAlbum.setChecked(false);
                                              // TODO: Handle errors
                                          }
                                          else {
                                              viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleRed));

                                          }

                                    } else {
                                        response = AlbumService.unheart(user, firebaseUser, reference, album, hidePopUp);
                                        if (response != HeartResponse.OK) {
                                            viewHolder.aqvHeartAlbum.setChecked(true);
                                            // TODO: Handle errors
                                        }
                                        else {
                                            viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple2));

                                        }
                                    }
                                }
                            }).start();

                        } else {
                            SignUpPopUp signUpPopUp = new SignUpPopUp(new Activity(), context, "Get Up And Dance! You Can Save This Album By Joining");
                            signUpPopUp.createAndShow();
                        }
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        if (switchOn == 2) {
            return albumList.size();
        }
        return trackList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public String getTrackIdAsUrl(String trackId) {
        String id = trackId.substring(14);
        return String.format(Locale.ENGLISH, "https://open.spotify.com/track/%s", id);
    }

    private MediaPlayer playAudio(String url) {
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

    public void setHidePopUp(Runnable hidePopUp) {
        this.hidePopUp = hidePopUp;
    }

    public void setShowPopUp(Runnable showPopUp) {
        this.showPopUp = showPopUp;
    }

    public void setUpdatePopUpTextTrue(Runnable updatePopUpTextTrue) {
        this.updatePopUpTextTrue = updatePopUpTextTrue;
    }

    public void setUpdatePopUpTextFalse(Runnable updatePopUpTextFalse) {
        this.updatePopUpTextFalse = updatePopUpTextFalse;
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