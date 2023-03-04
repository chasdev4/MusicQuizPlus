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
import androidx.core.content.ContextCompat;
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
//        int layout = 0;
//        BadgeType badgeType = BadgeType.values()[viewType];
//        switch (badgeType) {
//            case PLAYLIST_QUIZ_MILESTONE_1:
//            case PLAYLIST_QUIZ_MILESTONE_3:
//            case PLAYLIST_QUIZ_MILESTONE_5:
//            case PLAYLIST_QUIZ_MILESTONE_10:
//            case PLAYLIST_QUIZ_MILESTONE_25:
//            case PLAYLIST_QUIZ_MILESTONE_50:
//            case ARTIST_QUIZ_MILESTONE_1:
//            case ARTIST_QUIZ_MILESTONE_3:
//            case ARTIST_QUIZ_MILESTONE_5:
//            case ARTIST_QUIZ_MILESTONE_10:
//            case ARTIST_QUIZ_MILESTONE_25:
//            case ARTIST_QUIZ_MILESTONE_50:
//                layout = R.layout.badge_milestone;
//                break;
//            case QUICK_REACTOR_1:
//            case QUICK_REACTOR_2:
//            case QUICK_REACTOR_3:
//            case PERFECT_ACCURACY:
//                layout = R.layout.badge_performance;
//                break;
//            case ARTIST_KNOWLEDGE_1:
//            case ARTIST_KNOWLEDGE_2:
//            case ARTIST_KNOWLEDGE_3:
//            case ARTIST_KNOWLEDGE_4:
//                layout = R.layout.badge_artist_knowledge;
//                break;
//            case PLAYLIST_KNOWLEDGE:
//            case STUDIO_ALBUM_KNOWLEDGE:
//            case OTHER_ALBUM_KNOWLEDGE:
//                layout = R.layout.badge_collection_knowledge;
//                break;
//        }
        return new ResultsBadgeViewHolder(LayoutInflater.from(context).inflate(R.layout.badge_item, parent, false));

    }

    @Override
    public int getItemViewType(int position) {
        return badges.get(position).getType().ordinal();
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsBadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), badge.getName(), badge.getNumber()));
        holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), badge.getName(), badge.getNumber()));
        holder.setBonusXp(String.format("+%d XP", BadgeService.getBadgeXp(badge.getType())));
        switch (badge.getType()) {
            case ARTIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_1:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, 0));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, 0));
                holder.setText("1");
                break;
            case ARTIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_3:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, 0));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, 0));
                holder.setText("3");
                break;
            case ARTIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_5:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, 0));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, 0));
                holder.setText("5");
                break;
            case ARTIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_10:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, 0));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, 0));
                holder.setText("10");
                break;
            case ARTIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_25:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, 0));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, 0));
                holder.setText("25");
                break;
            case ARTIST_QUIZ_MILESTONE_50:
            case PLAYLIST_QUIZ_MILESTONE_50:
                holder.setBadgeName(BadgeService.getBadgeText(badge.getType(), null, badge.getNumber()));
                holder.setBadgeDescription(BadgeService.getBadgeDescription(badge.getType(), null, badge.getNumber()));
                holder.setText("50");
                break;
            case STUDIO_ALBUM_KNOWLEDGE:
            case OTHER_ALBUM_KNOWLEDGE:
            case PLAYLIST_KNOWLEDGE:
            case ARTIST_KNOWLEDGE_1:
            case ARTIST_KNOWLEDGE_2:
            case ARTIST_KNOWLEDGE_3:
            case ARTIST_KNOWLEDGE_4:
                Picasso.get().load(badge.getPhotoUrl()).placeholder(R.drawable.placeholder).into(holder.getImage());
                switch (badge.getType()) {
                    case ARTIST_KNOWLEDGE_1:
                    case OTHER_ALBUM_KNOWLEDGE:
                        holder.setTint(ContextCompat.getColor(context, R.color.mqBlue));
                        break;
                    case ARTIST_KNOWLEDGE_2:
                    case STUDIO_ALBUM_KNOWLEDGE:
                        holder.setTint(ContextCompat.getColor(context, R.color.mqRed));
                        break;
                    case ARTIST_KNOWLEDGE_3:
                        holder.setTint(ContextCompat.getColor(context, R.color.silver));
                        break;
                    case ARTIST_KNOWLEDGE_4:
                        holder.setTint(ContextCompat.getColor(context, R.color.gold));
                        break;
                    case PLAYLIST_KNOWLEDGE:
                        holder.setTint(ContextCompat.getColor(context, R.color.spotifyGreen));
                }
                break;
        }
        switch (badge.getType()) {
            case ARTIST_QUIZ_MILESTONE_1:
            case ARTIST_QUIZ_MILESTONE_3:
            case ARTIST_QUIZ_MILESTONE_5:
            case ARTIST_QUIZ_MILESTONE_10:
            case ARTIST_QUIZ_MILESTONE_25:
            case ARTIST_QUIZ_MILESTONE_50:
                holder.setBadge(R.id.included_badge_milestone);
                holder.setTint(ContextCompat.getColor(context, R.color.mqRed));
                break;
            case PLAYLIST_QUIZ_MILESTONE_1:
            case PLAYLIST_QUIZ_MILESTONE_3:
            case PLAYLIST_QUIZ_MILESTONE_5:
            case PLAYLIST_QUIZ_MILESTONE_10:
            case PLAYLIST_QUIZ_MILESTONE_25:
            case PLAYLIST_QUIZ_MILESTONE_50:
                holder.setBadge(R.id.included_badge_milestone);
                holder.setTint(ContextCompat.getColor(context, R.color.mqBlue));
                break;
            case PERFECT_ACCURACY:
                holder.setBadge(R.id.included_badge_performance);
                holder.getTint().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.trophy_gold));
                break;
            case QUICK_REACTOR_1:
            case QUICK_REACTOR_2:
            case QUICK_REACTOR_3:
                holder.setBadge(R.id.included_badge_performance);
                holder.getTint().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.trophy_silver));
                break;
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
