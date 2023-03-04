package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeartedArtistsViewHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private ImageView image;

    public HeartedArtistsViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.artist_name);
        image = itemView.findViewById(R.id.artist_image);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public ImageView getImage() {
        return image;
    }
}
