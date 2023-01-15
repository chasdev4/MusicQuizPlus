package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Objects;

import service.FirebaseService;

public class HistoryView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_view);

        ListView listView = findViewById(R.id.historyListView);
        TextView userLevel = findViewById(R.id.userLevel);
        View noCurrentUser = findViewById(R.id.historyNoCurrentUser);
        TextView noUserHeader = findViewById(R.id.logged_out_header);

        if(Objects.equals(userLevel.getText(), "GUEST"))
        {
            listView.setVisibility(View.GONE);
            noUserHeader.setText(R.string.guestUserHistory);
            noUserHeader.setTextSize(32);
            noCurrentUser.setVisibility(View.VISIBLE);
        }
        else
        {
            listView.setVisibility(View.VISIBLE);
            noCurrentUser.setVisibility(View.GONE);
        }

        if(Objects.equals(listView.getVisibility(), View.VISIBLE)) {

            //TODO: retreive history from firebase and populate listview

        }

    }
}