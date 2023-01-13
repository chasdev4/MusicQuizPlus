package com.example.musicquizplus;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import model.item.Playlist;

public class CustomAdapter extends ArrayAdapter<Playlist> {

    Handler mainHandler = new Handler();

    List<Playlist> items_list;
    int custom_layout_id;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Playlist> objects) {
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
        Playlist item = items_list.get(position);

        String url = item.getPhotoUrl().get(0).getUrl();
        String title = item.getName();
        new FetchImage(url, imageView, textView, title).start();
        return v;
    }

    class FetchImage extends Thread{

        String URL;
        Bitmap bitmap;
        ImageView imView;
        TextView txView;
        String title;

        FetchImage(String URL, ImageView imView, TextView txView, String title){
            this.URL = URL;
            this.imView = imView;
            this.txView = txView;
            this.title = title;
        }


        public void run() {

/*
            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage("Getting your pic....");
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                }
            });
*/



            InputStream inputStream;
            try {
                inputStream = new URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
/*
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

 */
                    imView.setImageBitmap(bitmap);
                    txView.setText(title);

                }
            });

        }
    }

}