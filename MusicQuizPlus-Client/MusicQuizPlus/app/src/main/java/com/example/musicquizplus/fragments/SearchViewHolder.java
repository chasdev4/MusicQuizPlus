package com.example.musicquizplus.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicquizplus.R;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private TextView subtitle;
    private ImageView image;

    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.itemTitle);
        subtitle = itemView.findViewById(R.id.itemSubtitle);
        image = itemView.findViewById(R.id.image);
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public TextView getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.setText(subtitle);
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
}
