package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

import java.util.Map;

import model.GoogleSignIn;
import model.TrackResult;
import model.User;
import model.type.Role;
import model.type.Source;
import service.FirebaseService;
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
    private View loadingPopUp;

    private TrackResult trackResult;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_result);
        context = this;

        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        loadingPopUp = findViewById(R.id.track_result_saving);

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        trackResult.changeTab();
                        User user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);

                        trackResultAdapter.setUser(user);

                        if (i == R.id.title_match_tab) {
                            trackResultAdapter.setCollection(trackResult.getTitleMatch());
                        } else {
                            trackResultAdapter.setCollection(trackResult.getSuggested());
                        }


                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                trackResultAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

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

    private void hidePopUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPopUp.setVisibility(View.GONE);
            }
        });
    }

    private void showPopUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPopUp.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updatePopUpText(boolean b) {
        ((TextView) loadingPopUp.findViewById(R.id.loading_text)).setText(
                b ? R.string.saving_message
                        : R.string.renoving_message
        );
    }

    private void setupRecyclerView() {
        trackResultAdapter = new TrackResultAdapter(this, this);
        trackResultAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onDataChange();
            }
        });
        trackResultAdapter.setHidePopUp(() -> hidePopUp());
        trackResultAdapter.setShowPopUp(() -> showPopUp());
        trackResultAdapter.setUpdatePopUpTextTrue(() -> updatePopUpText(true));
        trackResultAdapter.setUpdatePopUpTextFalse(() -> updatePopUpText(false));

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
        } else {
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
            user = (User) extras.getSerializable("user");
            Gson gson = new Gson();
            trackResult = gson.fromJson(jsonTrack, TrackResult.class);
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    user = FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    String albumKey = null;
                    String albumValue = null;
                    for (Map.Entry<String, String> albumId : user.getAlbumIds().entrySet()) {
                        if (albumId.getValue().equals(trackResult.getAlbumId())) {
                            albumKey = albumId.getKey();
                            albumValue = albumId.getValue();
                        }
                    }

//                String artistKey = reference.child("users").child(firebaseUser.getUid()).child("artistIds").push().getKey();
                    Intent intent = getIntent();

                    intent.putExtra("albumKey", albumKey);
                    intent.putExtra("albumValue", albumValue);
                    intent.putExtra("user", user);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }).start();

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}