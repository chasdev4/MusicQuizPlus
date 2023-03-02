package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.item.Artist;
import service.ItemService;

public class HeartedArtistsAdapter extends RecyclerView.Adapter<HeartedArtistsViewHolder> {

    private final Context context;
    private final List<Artist> artists;

    public HeartedArtistsAdapter(Context context, List<Artist> artists) {
        this.context = context;
        this.artists = artists;
    }

    @NonNull
    @Override
    public HeartedArtistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HeartedArtistsViewHolder(LayoutInflater.from(context).inflate(R.layout.hearted_artists_contents, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull HeartedArtistsViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.setName(artist.getName());
        Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl())).placeholder(R.drawable.placeholder).into(holder.getImage());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }
}
