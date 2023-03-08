package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.Badge;
import service.BadgeService;

public class ResultsBadgesAdapter extends RecyclerView.Adapter<ResultsBadgeViewHolder> {

    private Context context;
    private List<Badge> badges;

    public ResultsBadgesAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public ResultsBadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ResultsBadgeViewHolder(LayoutInflater.from(context).inflate(R.layout.badge_item, parent, false));

    }

    @Override
    public int getItemViewType(int position) {
        return badges.get(position).getType().ordinal();
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsBadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);

        int layout = 0;
        switch (badge.getType()) {
            case QUICK_REACTOR_1:
            case QUICK_REACTOR_2:
            case QUICK_REACTOR_3:
            case PERFECT_ACCURACY:
                layout = R.id.included_badge_performance;
                break;
            case ARTIST_QUIZ_MILESTONE_1:
            case ARTIST_QUIZ_MILESTONE_3:
            case ARTIST_QUIZ_MILESTONE_5:
            case ARTIST_QUIZ_MILESTONE_10:
            case ARTIST_QUIZ_MILESTONE_25:
            case ARTIST_QUIZ_MILESTONE_50:
            case PLAYLIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_50:
                layout = R.id.included_badge_milestone;
                break;
            case ARTIST_KNOWLEDGE_1:
            case ARTIST_KNOWLEDGE_2:
            case ARTIST_KNOWLEDGE_3:
            case ARTIST_KNOWLEDGE_4:
                layout = R.id.included_badge_artist_knowledge;
                break;
            case STUDIO_ALBUM_KNOWLEDGE:
            case OTHER_ALBUM_KNOWLEDGE:
            case PLAYLIST_KNOWLEDGE:
                layout = R.id.included_badge_collection_knowledge;
                break;
        }
        holder.setBadge(layout);
        holder.setTint(layout);

        holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), badge.getName(), badge.getNumber()));
        holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), badge.getName(), badge.getNumber()));
        holder.setBonusXp(String.format("+%d XP", BadgeService.getBadgeXp(badge.getType())));

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
        }

        switch (badge.getType()) {
            case PLAYLIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_50:
            case ARTIST_KNOWLEDGE_1:
            case OTHER_ALBUM_KNOWLEDGE:
                holder.setColor(ContextCompat.getColor(context, R.color.mqBlue));
                break;
            case ARTIST_QUIZ_MILESTONE_1:
            case ARTIST_QUIZ_MILESTONE_3:
            case ARTIST_QUIZ_MILESTONE_5:
            case ARTIST_QUIZ_MILESTONE_10:
            case ARTIST_QUIZ_MILESTONE_25:
            case ARTIST_QUIZ_MILESTONE_50:
            case ARTIST_KNOWLEDGE_2:
            case STUDIO_ALBUM_KNOWLEDGE:
                holder.setColor(ContextCompat.getColor(context, R.color.mqRed));
                break;
            case ARTIST_KNOWLEDGE_3:
                holder.setColor(ContextCompat.getColor(context, R.color.silver));
                break;
            case ARTIST_KNOWLEDGE_4:
                holder.setColor(ContextCompat.getColor(context, R.color.gold));
                break;
            case PLAYLIST_KNOWLEDGE:
                holder.setColor(ContextCompat.getColor(context, R.color.spotifyGreen));
                break;
            case PERFECT_ACCURACY:
                holder.getTint().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.trophy_gold));
                break;
            case QUICK_REACTOR_1:
            case QUICK_REACTOR_2:
            case QUICK_REACTOR_3:
                holder.getTint().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.trophy_silver));
                break;
        }

        if (BadgeService.hasThumbnail(badge.getType())) {
            Picasso.get().load(badge.getPhotoUrl()).placeholder(R.drawable.placeholder).into(holder.getImage());
        }


        switch (badge.getType()) {
            case QUICK_REACTOR_1:
                holder.getItemView().findViewById(R.id.quick_reactor_1).setVisibility(View.VISIBLE);
                break;
            case QUICK_REACTOR_2:
                holder.getItemView().findViewById(R.id.quick_reactor_1).setVisibility(View.GONE);
                holder.getItemView().findViewById(R.id.quick_reactor_2).setVisibility(View.VISIBLE);
                break;
            case QUICK_REACTOR_3:
                holder.getItemView().findViewById(R.id.quick_reactor_1).setVisibility(View.GONE);
                holder.getItemView().findViewById(R.id.quick_reactor_3).setVisibility(View.VISIBLE);
                break;
            case PERFECT_ACCURACY:
                holder.getItemView().findViewById(R.id.quick_reactor_1).setVisibility(View.GONE);
                holder.getItemView().findViewById(R.id.perfect_accuracy).setVisibility(View.VISIBLE);
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
