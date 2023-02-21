package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import model.TrackResult;
import model.User;
import model.item.Album;
import service.ItemService;

public class TrackResultAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private Context context;
    private TrackResult trackResult;
    private User user;


    public TrackResultAdapter(Context context, TrackResult trackResult) {
        this.context = context;
        this.trackResult = trackResult;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(context).inflate(R.layout.album_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Album album = trackResult.isCurrentPageOne()
                ? trackResult.getTitleMatch().get(position)
                : trackResult.getSuggested().get(position);
        holder.setTitle(album.getName());
        holder.setSubtitle(ItemService.formatAlbumSubtitle(album.getYear()));
        holder.setChecked(user.getAlbumIds().containsValue(album.getId()));
        Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl()))
                .placeholder(R.drawable.placeholder).into(holder.getImage());
    }

    @Override
    public int getItemCount() {
        return trackResult.isCurrentPageOne() ? trackResult.getTitleMatch().size() : trackResult.getSuggested().size();
    }

    public void setUser(User user) {
        this.user = user;
    }
}
