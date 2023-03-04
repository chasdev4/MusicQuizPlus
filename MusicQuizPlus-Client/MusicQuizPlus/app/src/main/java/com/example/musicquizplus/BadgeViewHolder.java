package com.example.musicquizplus;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BadgeViewHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private ImageView image;
    private TextView text;
    private ImageView tint;

    public BadgeViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        image = itemView.findViewById(R.id.badge_thumbnail);
        text = itemView.findViewById(R.id.badge_text);
        tint = itemView.findViewById(R.id.badge_tint);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public ImageView getImage() {
        return image;
    }

    public ImageView getTint() {return tint;}

    public void setTint(int color) {
        tint.setColorFilter(color);
    }

    public View getItemView() {
        return itemView;
    }


}
