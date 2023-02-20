package com.example.musicquizplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.SearchResult;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.SearchFilter;
import model.type.Source;
import service.ItemService;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private Context context;
    private List<SearchResult> searchResults;
    private User user;

    public SearchAdapter(Context context, List<SearchResult> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) { this.searchResults = searchResults; }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType) {
            case 1:
                layout = R.layout.item_result;
                break;
            case 2:
                layout = R.layout.album_item;
                break;
        }
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return searchResults.get(position).getType() == SearchFilter.ALBUM ? 2 : 1;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        switch (searchResults.get(position).getType()) {
            case ARTIST:
                Artist artist = searchResults.get(position).getArtist();
                holder.setTitle(artist.getName());
                holder.setSubtitle("Artist");
                Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ArtistsView.class);
                        intent.putExtra("currentArtist", artist);
                        view.getContext().startActivity(intent);
                    }
                });
                break;
            case ALBUM:
                Album album = searchResults.get(position).getAlbum();
                holder.setTitle(album.getName());
                holder.setSubtitle(String.format("Album • %s", album.getYear()));
                holder.setChecked(user.getAlbumIds().containsValue(album.getId()));
                Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());

                break;
            case SONG:
                Track track = searchResults.get(position).getTrack();
                holder.setTitle(track.getName());
                holder.setSubtitle(String.format("Song by %s", track.getArtistName()));
                Picasso.get().load(ItemService.getSmallestPhotoUrl(track.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), TrackResultView.class);
                        intent.putExtra("currentTrack", track);
                        view.getContext().startActivity(intent);
                    }
                });
                break;
            case PLAYLIST:
                Playlist playlist = searchResults.get(position).getPlaylist();
                holder.setTitle(playlist.getName());
                holder.setSubtitle("Playlist");
                Picasso.get().load(ItemService.getSmallestPhotoUrl(playlist.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), PlaylistQuizView.class);
                        intent.putExtra("currentPlaylist", playlist);
                        intent.putExtra("source", Source.SEARCH);
                        view.getContext().startActivity(intent);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return searchResults == null ? 0 : searchResults.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setUser(User user) {
        this.user = user;
    }
}
