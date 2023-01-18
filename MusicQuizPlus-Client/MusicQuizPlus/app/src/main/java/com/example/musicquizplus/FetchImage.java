package com.example.musicquizplus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Handler;

public class FetchImage extends Thread{

    String URL;
    Bitmap bitmap;
    ImageView imView;
    TextView txView;
    String title;
    android.os.Handler mainHandler;
    FetchImage(String URL, ImageView imView, TextView txView, String title, android.os.Handler mainHandler){
        this.URL = URL;
        this.imView = imView;
        this.txView = txView;
        this.title = title;
        this.mainHandler = mainHandler;
    }


    public void run() {
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
                imView.setImageBitmap(bitmap);
                txView.setText(title);
            }
        });

    }
}
