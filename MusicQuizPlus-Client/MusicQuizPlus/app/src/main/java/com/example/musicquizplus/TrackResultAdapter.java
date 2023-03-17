package com.example.musicquizplus;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.TrackResult;
import model.User;
import model.item.Album;
import model.type.HeartResponse;
import model.type.Role;
import service.FirebaseService;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;

public class TrackResultAdapter extends RecyclerView.Adapter<TrackResultViewHolder> {
    private Context context;
    private Activity activity;
    private User user;
    private List<Album> collection;
    private Runnable showPopUp;
    private Runnable hidePopUp;
    private Runnable updatePopUpTextTrue;
    private Runnable updatePopUpTextFalse;


    public TrackResultAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        collection = new ArrayList<>();
    }

    @NonNull
    @Override
    public TrackResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrackResultViewHolder(
                LayoutInflater.from(context).inflate(R.layout.album_item, parent, false)
        );

    }

    @Override
    public void onBindViewHolder(@NonNull TrackResultViewHolder holder, int position) {
        Album album = collection.get(position);
        holder.setTitle(album.getName());
        holder.setSubtitle("Album");
        holder.setChecked(user.getAlbumIds().containsValue(album.getId()));
        Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl()))
                .placeholder(R.drawable.placeholder).into(holder.getImage());
        holder.getToggleButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignIn googleSignIn = new GoogleSignIn();
                FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
                if (firebaseUser == null) {
                    holder.setChecked(false);
                    SignUpPopUp signUpPopUp = new SignUpPopUp(activity, context, context.getString(R.string.logged_out_artists));
                    signUpPopUp.createAndShow();
                } else {
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                    SpotifyService spotifyService = new SpotifyService(context.getString(R.string.SPOTIFY_KEY));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (holder.getToggleButton().isChecked()) {
                                updatePopUpTextTrue.run();
                            } else {
                                updatePopUpTextFalse.run();
                            }
                            showPopUp.run();

                            HeartResponse response = null;
                            if (holder.getToggleButton().isChecked()) {
                                response = AlbumService.heart(firebaseUser, db, album, spotifyService, hidePopUp);
                            } else {
                                response = AlbumService.unheart(firebaseUser, db, album, hidePopUp);
                            }
                            if (response != HeartResponse.OK) {
                                hidePopUp.run();
                                HeartResponse finalResponse = response;
                                ((SearchActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlbumService.showError(finalResponse, context);
                                    }
                                });
                            }
                        }
                    }).start();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCollection(List<Album> collection) {
        this.collection = collection;
    }

    public void setShowPopUp(Runnable showPopUp) {
        this.showPopUp = showPopUp;
    }

    public void setHidePopUp(Runnable hidePopUp) {
        this.hidePopUp = hidePopUp;
    }

    public void setUpdatePopUpTextTrue(Runnable updatePopUpTextTrue) {
        this.updatePopUpTextTrue = updatePopUpTextTrue;
    }

    public void setUpdatePopUpTextFalse(Runnable updatePopUpTextFalse) {
        this.updatePopUpTextFalse = updatePopUpTextFalse;
    }
}
