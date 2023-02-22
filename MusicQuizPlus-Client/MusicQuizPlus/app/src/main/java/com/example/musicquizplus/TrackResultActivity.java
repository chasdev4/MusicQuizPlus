package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import model.GoogleSignIn;
import model.TrackResult;
import model.User;
import service.ItemService;
import service.SpotifyService;

public class TrackResultActivity extends AppCompatActivity {

    private TextView title;
    private TextView subtitle;
    private ImageView image;
    private ImageView noResults;
    private TextView noResultsText;
    private Context context;
    private TrackResultAdapter trackResultAdapter;
    private RecyclerView recyclerView;
    private RadioGroup radioGroup;
    private ImageButton backToTop;

    private TrackResult trackResult;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;
    private SpotifyService spotifyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_result);
        context = this;

        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        title = findViewById(R.id.track_result_title);
        subtitle = findViewById(R.id.track_result_subtitle);
        image = findViewById(R.id.track_result_image);
        noResults = findViewById(R.id.track_result_no_results);
        noResults.setVisibility(View.GONE);
        noResultsText = findViewById(R.id.track_result_no_results_text);
        noResultsText.setVisibility(View.GONE);

        radioGroup = findViewById(R.id.track_result_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                trackResult.changeTab();
                if (i == R.id.title_match_tab) {
                    trackResultAdapter.setCollection(trackResult.getTitleMatch());
                }
                else {
                    trackResultAdapter.setCollection(trackResult.getSuggested());
                }
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        trackResultAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        backToTop = findViewById(R.id.track_result_back_to_top);

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
                onDataChange();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(trackResultAdapter);
        onDataChange();
    }

    private void onDataChange() {
        if (trackResultAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.GONE);
            noResultsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String jsonTrack = extras.getString("track");
            String jsonUser = extras.getString("user");
            Gson gson = new Gson();
            trackResult = gson.fromJson(jsonTrack, TrackResult.class);
            user = gson.fromJson(jsonUser, User.class);
            trackResultAdapter.setUser(user);
            trackResultAdapter.setCollection(trackResult.getTitleMatch());
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    trackResultAdapter.notifyDataSetChanged();
                }
            });
            title.setText(trackResult.getName());
            subtitle.setText(ItemService.formatTrackResultSubtitle(trackResult.getArtistName()));
            Picasso.get().load(trackResult.getImageUrl()).placeholder(R.drawable.placeholder).into(image);
            recyclerView.setVisibility(View.VISIBLE);
            Log.d("TAG", "onStart: ");
        }
    }
}