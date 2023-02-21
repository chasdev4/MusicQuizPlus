package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TrackResultViewHolder  extends RecyclerView.ViewHolder{

    private View itemView;
    private TextView title;
    private TextView subtitle;
    private ImageView image;
    private ToggleButton toggleButton;

    public TrackResultViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        this.title = itemView.findViewById(R.id.itemTitle);
        this.subtitle = itemView.findViewById(R.id.itemSubtitle);
        this.image = itemView.findViewById(R.id.image);
        this.toggleButton = itemView.findViewById(R.id.heart_toggle_button);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.setText(subtitle);
    }

    public void setChecked(boolean checked) {
        toggleButton.setChecked(checked);
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
}
