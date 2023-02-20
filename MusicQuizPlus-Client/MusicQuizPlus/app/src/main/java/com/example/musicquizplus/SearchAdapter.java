package com.example.musicquizplus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.SearchResult;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Source;
import service.ItemService;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private Context context;
    private List<SearchResult> searchResults;

    public SearchAdapter(Context context, List<SearchResult> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) { this.searchResults = searchResults; }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        switch (searchResults.get(position).getType()) {
            case ARTIST:
                Artist artist = searchResults.get(position).getArtist();
                holder.setTitle(artist.getName());
                holder.setSubtitle("Artist");
                Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl())).into(holder.getImage());
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
                Picasso.get().load(ItemService.getSmallestPhotoUrl(album.getPhotoUrl())).into(holder.getImage());

                break;
            case SONG:
                Track track = searchResults.get(position).getTrack();
                holder.setTitle(track.getName());
                holder.setSubtitle(String.format("Song by %s", track.getArtistName()));
                Picasso.get().load(ItemService.getSmallestPhotoUrl(track.getPhotoUrl())).into(holder.getImage());
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
                Picasso.get().load(ItemService.getSmallestPhotoUrl(playlist.getPhotoUrl())).into(holder.getImage());
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
}
