package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ResultsBadgeViewHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private View badge;
    private ImageView image;
    private TextView text;
    private ImageView tint;
    private TextView badgeName;
    private TextView badgeDescription;
    private TextView bonusXp;


    public ResultsBadgeViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        text = itemView.findViewById(R.id.badge_text);
        badge = itemView.findViewById(R.id.included_badge);
        badgeName = itemView.findViewById(R.id.badge_name);
        badgeDescription = itemView.findViewById(R.id.badge_description);
        bonusXp = itemView.findViewById(R.id.badge_bonus_xp);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public ImageView getImage() {
        return image;
    }

    public ImageView getTint() {return tint;}

    public void setTint(int id) {
        tint = badge.findViewById(id).findViewById(R.id.badge_tint);
    }
    public void setColor(int color) {
        tint.setColorFilter(color);
    }

    public View getItemView() {
        return itemView;
    }

    public void setBadge(int id) {
        this.badge.findViewById(id).setVisibility(View.VISIBLE);
        image = this.badge.findViewById(id).findViewById(R.id.badge_thumbnail);
    }

    public void setBadgeName(String badgeName) {
        this.badgeName.setText(badgeName);
    }

    public void setBadgeDescription(String badgeDescription) {
        this.badgeDescription.setText(badgeDescription);
    }

    public void setBonusXp(String bonusXp) {
        this.bonusXp.setText(bonusXp);
    }
}
