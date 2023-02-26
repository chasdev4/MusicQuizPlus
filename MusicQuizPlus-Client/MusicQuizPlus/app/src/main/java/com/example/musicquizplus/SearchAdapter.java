package com.example.musicquizplus;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import model.SearchResult;
import model.TrackResult;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.SearchFilter;
import model.type.Source;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;

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
        return new SearchViewHolder(LayoutInflater.from(context).inflate(layout, parent, false));
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
                holder.setSubtitle(ItemService.formatAlbumSubtitle(album.getArtistsMap().get(album.getArtistId()), album.getYear()));
                FirebaseUser firebaseUser = ((SearchActivity)context).getFirebaseUser();
                if (firebaseUser != null) {
                    DatabaseReference db = ((SearchActivity)context).getDb();
                    holder.setChecked(user.getAlbumIds().containsValue(album.getId()));
                    holder.getToggleButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (holder.getToggleButton().isChecked()) {
                                        SpotifyService spotifyService = ((SearchActivity)context).getSpotifyService();
                                        AlbumService.heart(user, firebaseUser, db, album, spotifyService);
                                    }
                                    else {
                                        AlbumService.unheart(user, firebaseUser, db, album);
                                    }
                                }
                            }).start();

                        }
                    });
                }
                else {
                    holder.setChecked(false);
                    holder.getToggleButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO: Display the sign-up pop up
                        }
                    });
                }
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
                        TrackResult trackResult = ((SearchActivity)context).getTrackResult(track);
                        Intent intent = new Intent(view.getContext(), TrackResultActivity.class);
                        Gson gson = new Gson();
                        String jsonTrack = gson.toJson(trackResult);
                        intent.putExtra("track", jsonTrack);
                        intent.putExtra("user", user);
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
