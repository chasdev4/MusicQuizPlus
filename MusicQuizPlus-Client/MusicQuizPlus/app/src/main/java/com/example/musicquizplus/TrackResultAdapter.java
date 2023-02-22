package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import model.TrackResult;
import model.User;
import model.item.Album;
import service.ItemService;

public class TrackResultAdapter extends RecyclerView.Adapter<TrackResultViewHolder> {
    private Context context;
    private User user;
    private List<Album> collection;


    public TrackResultAdapter(Context context, TrackResult trackResult) {
        this.context = context;
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
}
