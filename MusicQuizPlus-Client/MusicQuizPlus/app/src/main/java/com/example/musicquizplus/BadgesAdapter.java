package com.example.musicquizplus;

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
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import model.Badge;
import model.SearchResult;
import model.User;
import model.item.Playlist;
import model.type.BadgeType;
import model.type.SearchFilter;

public class BadgesAdapter extends RecyclerView.Adapter<BadgeViewHolder> {

    private Context context;
    private List<Badge> badges;

    public BadgesAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = 0;
        BadgeType badgeType = BadgeType.values()[viewType];
        switch (badgeType) {
            case PLAYLIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_50:
            case ARTIST_QUIZ_MILESTONE_1:
            case ARTIST_QUIZ_MILESTONE_3:
            case ARTIST_QUIZ_MILESTONE_5:
            case ARTIST_QUIZ_MILESTONE_10:
            case ARTIST_QUIZ_MILESTONE_25:
            case ARTIST_QUIZ_MILESTONE_50:
                layout = R.layout.badge_milestone;
                break;
            case QUICK_REACTOR_1:
            case QUICK_REACTOR_2:
            case QUICK_REACTOR_3:
            case PERFECT_ACCURACY:
                layout = R.layout.badge_performance;
                break;
            case ARTIST_KNOWLEDGE_1:
            case ARTIST_KNOWLEDGE_2:
            case ARTIST_KNOWLEDGE_3:
            case ARTIST_KNOWLEDGE_4:
                layout = R.layout.badge_artist_knowledge;
                break;
            case PLAYLIST_KNOWLEDGE:
            case STUDIO_ALBUM_KNOWLEDGE:
            case OTHER_ALBUM_KNOWLEDGE:
                layout = R.layout.badge_collection_knowledge;
                break;
        }
        return new BadgeViewHolder(LayoutInflater.from(context).inflate(layout, parent, false));

    }

    @Override
    public int getItemViewType(int position) {
        return badges.get(position).getType().ordinal();
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        switch (badge.getType()) {
            case ARTIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_1:
                holder.setText("1");
                break;
            case ARTIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_3:
                holder.setText("3");
                break;
            case ARTIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_5:
                holder.setText("5");
                break;
            case ARTIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_10:
                holder.setText("10");
                break;
            case ARTIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_25:
                holder.setText("25");
                break;
            case ARTIST_QUIZ_MILESTONE_50:
            case PLAYLIST_QUIZ_MILESTONE_50:
                holder.setText("50");
                break;
            case STUDIO_ALBUM_KNOWLEDGE:
            case OTHER_ALBUM_KNOWLEDGE:
            case PLAYLIST_KNOWLEDGE:
            case ARTIST_KNOWLEDGE_1:
            case ARTIST_KNOWLEDGE_2:
            case ARTIST_KNOWLEDGE_3:
            case ARTIST_KNOWLEDGE_4:
                Picasso.get().load(badge.getPhotoUrl()).into(holder.getImage());
                break;
            case QUICK_REACTOR_1:
            case QUICK_REACTOR_2:
            case QUICK_REACTOR_3:
                break;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }
}
