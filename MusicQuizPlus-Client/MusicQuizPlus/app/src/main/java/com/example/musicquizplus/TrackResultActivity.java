package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.ContentInfo;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import model.Search;
import model.TrackResult;
import model.User;
import model.item.Album;
import model.item.Playlist;

public class TrackResultActivity extends AppCompatActivity {

    private Context context;
    private TrackResult trackResult;
    private TrackResultAdapter trackResultAdapter;
    private RecyclerView recyclerView;
    private RadioGroup radioGroup;
    private List<Album> albums;
    private ImageButton backToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_result);

        context = this;
        albums = new ArrayList<>();
        radioGroup = findViewById(R.id.track_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.title_match_tab) {
                    albums = trackResult.getTitleMatch();
                }
                else {
                    albums = trackResult.getSuggested();
                }
            }
        });

        backToTop = findViewById(R.id.trvBackToTop);

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        recyclerView = findViewById(R.id.track_recycler_view);
        setupRecyclerView();
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int scroll = llm.findFirstVisibleItemPosition();

                if (scroll > 0) {
                    backToTop.setVisibility(View.VISIBLE);
                } else {
                    backToTop.setVisibility(View.GONE);
                }

            }
        });
    }

    private void setupRecyclerView() {
        trackResultAdapter = new TrackResultAdapter(context, trackResult);
        trackResultAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
               // onDataChange();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(trackResultAdapter);
        //onDataChange();
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Search search = (Search) extras.getSerializable("search");
            trackResult = search.getTrackResult(s)
        }
    }
}