package com.example.musicquizplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<GridViewItems> {

    List<GridViewItems> items_list;
    int custom_layout_id;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<GridViewItems> objects) {
        super(context, resource, objects);
        items_list = objects;
        custom_layout_id = resource;
    }

    @Override public int getCount() {
        return items_list.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            // getting reference to the main layout and initializing
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(custom_layout_id, null);
        }

        // initializing the imageview and textview and setting data
        ImageView imageView = v.findViewById(R.id.gridViewPlaylistCover);
        TextView textView = v.findViewById(R.id.gridViewPlaylistName);

        // get the item using the position param
        GridViewItems item = items_list.get(position);

        imageView.setImageResource(item.getImage_id());
        textView.setText(item.getText());
        return v;
    }
}

