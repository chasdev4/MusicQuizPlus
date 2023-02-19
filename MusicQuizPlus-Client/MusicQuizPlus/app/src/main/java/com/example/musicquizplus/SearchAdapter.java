package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicquizplus.fragments.SearchViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.SearchResult;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import service.ItemService;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private Context context;
    private List<SearchResult> searchResults;

    public SearchAdapter(Context context, List<SearchResult> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
    }


    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(context).inflate(R.layout.item_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        switch (searchResults.get(position).getType()) {
            case ARTIST:
                Artist artist = searchResults.get(position).getArtist();
                holder.setTitle(artist.getName());
                holder.setSubtitle("Artist");
                Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl())).into(holder.getImage());
                break;
            case ALBUM:
                Album album = searchResults.get(position).getAlbum();
                holder.setTitle(album.getName());
                holder.setSubtitle(String.format("Album • %s", album.getYear()));
                Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl())).into(holder.getImage());
                break;
            case SONG:
                Track track = searchResults.get(position).getTrack();
                holder.setTitle(track.getName());
                holder.setSubtitle(String.format("Song by %s", track.getArtistName()));
                Picasso.get().load(ItemService.getSmallestPhotoUrl(track.getPhotoUrl())).into(holder.getImage());
                break;
            case PLAYLIST:
                Playlist playlist = searchResults.get(position).getPlaylist();
                holder.setTitle(playlist.getName());
                holder.setSubtitle("Playlist");
                Picasso.get().load(ItemService.getSmallestPhotoUrl(playlist.getPhotoUrl())).into(holder.getImage());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
