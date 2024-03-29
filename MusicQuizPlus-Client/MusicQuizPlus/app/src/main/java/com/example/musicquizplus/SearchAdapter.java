package com.example.musicquizplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
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
import model.SignUpPopUp;
import model.TrackResult;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.HeartResponse;
import model.type.Role;
import model.type.SearchFilter;
import model.type.Source;
import service.FirebaseService;
import service.ItemService;
import service.SpotifyService;
import service.firebase.AlbumService;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private Context context;
    private SearchActivity activity;
    private List<SearchResult> searchResults;
    private User user;
    private Runnable showPopUp;
    private Runnable hidePopUp;
    private Runnable updatePopUpTextTrue;
    private Runnable updatePopUpTextFalse;
    private Runnable showPlayNow;
    private Runnable hidePlayNow;

    public SearchAdapter(Context context, SearchActivity activity, List<SearchResult> searchResults) {
        this.context = context;
        this.activity = activity;
        this.searchResults = searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

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
        FirebaseUser firebaseUser = ((SearchActivity) context).getFirebaseUser();
        switch (searchResults.get(position).getType()) {
            case ARTIST:
                Artist artist = searchResults.get(position).getArtist();
                holder.setTitle(artist.getName());
                holder.setSubtitle("Artist");
                holder.getItemView().setBackgroundColor(user.getArtistIds().containsValue(artist.getId())
                        || (activity.getUser() != null && activity.getUser().getArtistIds().containsValue(artist.getId()))
                        ? ContextCompat.getColor(context, R.color.mqPurpleRed)
                        : ContextCompat.getColor(context, R.color.mqPurple2));
                holder.getBanner().findViewById(R.id.item_result_banner_bg).setBackgroundColor(ContextCompat.getColor(context, R.color.mqRed));
                holder.getBanner().setVisibility(user.getArtistIds().containsValue(artist.getId()) ? View.VISIBLE : View.GONE);
//                holder.getHeartedIcon().setVisibility(user.getArtistIds().containsValue(artist.getId()) ? View.VISIBLE : View.GONE);
                Picasso.get().load(ItemService.getSmallestPhotoUrl(artist.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ArtistQuizView.class);
                        intent.putExtra("currentArtist", artist);
                        intent.putExtra("source", Source.SEARCH);
                        intent.putExtra("currentUser", user);
                        intent.putExtra("pos", holder.getAdapterPosition());
                        ((SearchActivity) view.getContext()).startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                    }
                });

                break;
            case ALBUM:
                Album album = searchResults.get(position).getAlbum();
                holder.setTitle(album.getName());
                holder.setSubtitle(ItemService.formatAlbumSubtitle(album.getArtistsMap().get(album.getArtistId()), album.getYear()));
                if (firebaseUser != null) {
                    DatabaseReference db = ((SearchActivity) context).getDb();
                    holder.getItemView().setBackgroundColor(user.getAlbumIds().containsValue(album.getId())
                            ? ContextCompat.getColor(context, R.color.mqPurpleBlue)
                            : ContextCompat.getColor(context, R.color.mqPurple2));
                    holder.getBanner().setVisibility(user.getAlbumIds().containsValue(album.getId()) ? View.VISIBLE : View.GONE);
                    holder.setChecked(user.getAlbumIds().containsValue(album.getId()));
                    holder.getToggleButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (holder.getToggleButton().isChecked()) {
                                holder.getItemView().setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleBlue));
                                holder.getBanner().setVisibility(View.VISIBLE);
                            } else {
                                holder.getItemView().setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple2));
                                holder.getBanner().setVisibility(View.GONE);
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (holder.getToggleButton().isChecked()) {
                                        updatePopUpTextTrue.run();
                                    } else {
                                        updatePopUpTextFalse.run();
                                    }
                                    showPopUp.run();
                                    SharedPreferences sharedPref = ((SearchActivity) context).getPreferences(Context.MODE_PRIVATE);
                                    boolean playNowEnabled = !sharedPref
                                            .getBoolean(((SearchActivity) context)
                                                    .getString(R.string.playNowHidden), false);
                                    HeartResponse response = null;
                                    if (holder.getToggleButton().isChecked()) {
                                        SpotifyService spotifyService = ((SearchActivity) context).getSpotifyService();
                                        response = AlbumService.heart(firebaseUser, db, album, spotifyService, hidePopUp);
                                    } else {
                                        response = AlbumService.unheart(firebaseUser, db, album, hidePopUp);
                                    }
                                    if (response != HeartResponse.OK) {
                                        hidePopUp.run();

                                        if (response == HeartResponse.ITEM_EXISTS) {
                                            holder.getToggleButton().setChecked(true);
                                            holder.getItemView().setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurpleRed));

                                        } else if (response == HeartResponse.NO_ALBUM_TRACKS) {
                                            ((SearchActivity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    holder.getBanner().setVisibility(View.GONE);
                                                    holder.getToggleButton().setChecked(false);
                                                    holder.getToggleButton().setEnabled(false);
                                                    holder.getToggleButton().setVisibility(View.GONE);
                                                    holder.getImage().setColorFilter(ContextCompat.getColor(context, R.color.disabled));
                                                    holder.getItemView().setBackgroundColor(ContextCompat.getColor(context, R.color.disabledPurple));
                                                    holder.getTitle().setTextColor(ContextCompat.getColor(context, R.color.disabledForeground));
                                                    holder.getSubtitle().setTextColor(ContextCompat.getColor(context, R.color.disabledForeground));
                                                    holder.setSubtitle("Unavailable");
                                                }
                                            });

                                        }
                                        else {
                                            ((SearchActivity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    holder.getItemView().setBackgroundColor(ContextCompat.getColor(context, R.color.mqPurple2));
                                                    holder.getBanner().setVisibility(View.GONE);
                                                    holder.setChecked(false);
                                                }
                                            });

                                        }

                                        if (response != HeartResponse.ITEM_EXISTS) {
                                            HeartResponse finalResponse = response;
                                            ((SearchActivity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlbumService.showError(finalResponse, context);
                                                }
                                            });
                                        }
                                    } else {
                                        ((SearchActivity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//
                                                if (holder.getToggleButton().isChecked()
                                                        && playNowEnabled) {
                                                    showPlayNow.run();
                                                    ((SearchActivity) context).
                                                            setPlayNowArtistId(album.getArtistId());
                                                } else {
                                                    Toast toast = null;
                                                    if (holder.getToggleButton().isChecked()) {
                                                        toast = Toast.makeText(context,
                                                                String.format("\"%s\" was saved.",
                                                                        album.getName()), Toast.LENGTH_LONG);
                                                    } else {
                                                        toast = Toast.makeText(context,
                                                                String.format("\"%s\" was removed.",
                                                                        album.getName()), Toast.LENGTH_LONG);
                                                    }
                                                    toast.show();
                                                }

                                            }
                                        });

                                    }
                                }
                            }).start();
                        }
                    });
                    holder.getItemView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.getItemView().setClickable(false);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Artist currentArtist = null;
                                    if (holder.getToggleButton().isChecked() && user.getArtistIds().containsValue(album.getArtistId())) {
                                        currentArtist = user.getArtist(album.getArtistId());
                                    }
                                    if (!holder.getToggleButton().isChecked() || currentArtist == null) {
                                        SpotifyService spotifyService = new SpotifyService(((SearchActivity) context).getString(R.string.SPOTIFY_KEY));
                                        currentArtist = spotifyService.artistOverview(album.getArtistId());
                                    }
                                    Intent intent = new Intent(view.getContext(), ArtistQuizView.class);
                                    intent.putExtra("currentArtist", currentArtist);
                                    intent.putExtra("source", Source.SEARCH);
                                    intent.putExtra("currentUser", user);
                                    intent.putExtra("pos", holder.getAdapterPosition());
                                    ((SearchActivity) view.getContext()).startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                                }
                            }).start();
                        }
                    });
                } else {
                    holder.getToggleButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.setChecked(false);
                            SignUpPopUp popUp = new SignUpPopUp(activity, context, context.getString(R.string.logged_out_artists));
                            popUp.createAndShow();
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
                holder.getItemView().setBackgroundColor(user.getAlbumIds().containsValue(track.getAlbumId())
                        ? ContextCompat.getColor(context, R.color.mqPurpleBlue)
                        : ContextCompat.getColor(context, R.color.mqPurple2));
                holder.getBanner().findViewById(R.id.item_result_banner_bg).setBackgroundColor(ContextCompat.getColor(context, R.color.mqBlue));
                holder.getBanner().setVisibility(user.getAlbumIds().containsValue(track.getAlbumId()) ? View.VISIBLE : View.GONE);
//                holder.getHeartedIcon().setVisibility(user.getAlbumIds().containsValue(track.getAlbumId()) ? View.VISIBLE : View.GONE);
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TrackResult trackResult = ((SearchActivity) context).getTrackResult(track);
                        Intent intent = new Intent(view.getContext(), TrackResultActivity.class);
                        Gson gson = new Gson();
                        String jsonTrack = gson.toJson(trackResult);
                        intent.putExtra("track", jsonTrack);
                        intent.putExtra("user", user);
                        intent.putExtra("pos", holder.getAdapterPosition());
                        ((SearchActivity) view.getContext()).startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                    }
                });
                break;
            case PLAYLIST:
                Playlist playlist = searchResults.get(position).getPlaylist();
                holder.setTitle(playlist.getName());
                holder.setSubtitle("Playlist");
                holder.getItemView().setBackgroundColor(user.getPlaylistIds().containsValue(playlist.getId())
                        ? ContextCompat.getColor(context, R.color.mqPurpleGreen)
                        : ContextCompat.getColor(context, R.color.mqPurple2));
                holder.getBanner().findViewById(R.id.item_result_banner_bg).setBackgroundColor(ContextCompat.getColor(context, R.color.spotifyGreen));
                holder.getBanner().setVisibility(user.getPlaylistIds().containsValue(playlist.getId()) ? View.VISIBLE : View.GONE);
//                holder.getHeartedIcon().setVisibility(user.getPlaylistIds().containsValue(playlist.getId()) ? View.VISIBLE : View.GONE);
                Picasso.get().load(ItemService.getSmallestPhotoUrl(playlist.getPhotoUrl()))
                        .placeholder(R.drawable.placeholder).into(holder.getImage());
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), PlaylistQuizView.class);
                        intent.putExtra("currentPlaylist", playlist);
                        intent.putExtra("currentUser", user);
                        intent.putExtra("source", Source.SEARCH);
                        intent.putExtra("pos", holder.getAdapterPosition());
                        ((SearchActivity) view.getContext()).startActivityForResult(intent, Activity.RESULT_FIRST_USER);
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

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void clear() {
        searchResults.clear();
    }

    public User getUser() {
        return user;
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

    public Runnable getShowPlayNow() {
        return showPlayNow;
    }

    public void setShowPlayNow(Runnable showPlayNow) {
        this.showPlayNow = showPlayNow;
    }

    public Runnable getHidePlayNow() {
        return hidePlayNow;
    }

    public void setHidePlayNow(Runnable hidePlayNow) {
        this.hidePlayNow = hidePlayNow;
    }


}
