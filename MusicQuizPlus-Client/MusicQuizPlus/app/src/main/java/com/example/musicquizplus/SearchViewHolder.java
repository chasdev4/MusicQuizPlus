package com.example.musicquizplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicquizplus.R;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private TextView title;
    private TextView subtitle;
    private ImageView image;
    private ToggleButton toggleButton;

    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        title = itemView.findViewById(R.id.itemTitle);
        subtitle = itemView.findViewById(R.id.itemSubtitle);
        image = itemView.findViewById(R.id.image);
        toggleButton = itemView.findViewById(R.id.heart_toggle_button);
    }

    public void setChecked(boolean checked) {
        if (toggleButton == null) {
            return;
        }
        toggleButton.setChecked(checked);
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

    public View getItemView() {
        return itemView;
    }

    public ToggleButton getToggleButton() { return toggleButton; }
}
