package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BadgeViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;
    private TextView text;

    public BadgeViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public ImageView getImage() {
        return image;
    }
}
