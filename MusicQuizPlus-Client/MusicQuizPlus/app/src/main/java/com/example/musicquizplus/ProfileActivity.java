package com.example.musicquizplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.concurrent.CountDownLatch;

import model.User;
import service.ItemService;

public class ProfileActivity extends AppCompatActivity {

    private User user;
    private AppCompatTextView name;
    private ImageView avatar;
    private TextView level;
    private LinearLayout profileArea;
    private RecyclerView badges;
    private RecyclerView artists;
    private TextView badgeCount;
    private TextView artistCount;
    private ImageButton backToTop;
    private BadgesAdapter badgesAdapter;
    private HeartedArtistsAdapter artistsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.profile_user_name);
        avatar = findViewById(R.id.userCustomAvatar);
        level = findViewById(R.id.userLevel);
        badgeCount = findViewById(R.id.badge_count);
        artistCount = findViewById(R.id.artist_count);
        backToTop = findViewById(R.id.profile_back_to_top_button);
        profileArea = findViewById(R.id.profile_area);
        profileArea.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (profileArea.getScrollY() > 0) {
                    backToTop.setVisibility(View.VISIBLE);
                }
                else {
                    backToTop.setVisibility(View.GONE);
                }
            }
        });
        badges = findViewById(R.id.profile_badges_container);
        artists = findViewById(R.id.profile_hearted_artists_container);
        badges.setVisibility(View.INVISIBLE);
        artists.setVisibility(View.INVISIBLE);

    }

    private void setupRecyclerViews() {
        badgesAdapter = new BadgesAdapter(((Context)this), user.getBadgesAsList());
        badgesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onBadgeDataChange();
            }
        });

        badges.setLayoutManager(new GridLayoutManager(this, 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        badges.setHasFixedSize(true);

        badges.setAdapter(badgesAdapter);

        artistsAdapter = new HeartedArtistsAdapter(((Context)this), user.getArtistsAsList());
        artistsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onArtistDataChanged();
            }
        });
        artists.setLayoutManager(new GridLayoutManager(this, 3){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        artists.setHasFixedSize(true);

        artists.setAdapter(artistsAdapter);

        onDataChange();
    }

    private void onArtistDataChanged() {
        if (artistsAdapter.getItemCount() == 0) {
            artists.setVisibility(View.GONE);
        }
        else {
            artists.setVisibility(View.VISIBLE);
        }
        artistCount.setText(String.valueOf(artistsAdapter.getItemCount()));
    }

    private void onDataChange() {
        onBadgeDataChange();
        onArtistDataChanged();
    }
    private void onBadgeDataChange() {
        if (badgesAdapter.getItemCount() == 0) {
            badges.setVisibility(View.GONE);
        }
        else {
            badges.setVisibility(View.VISIBLE);
        }
        badgeCount.setText(String.valueOf(badgesAdapter.getItemCount()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = (User) extras.getSerializable("user");
            if (user == null) {
                finish();
            }

            name.setText(user.getName());
            level.setText(ItemService.formatUserLevel(user.getLevel()));
            Picasso.get().load(user.getPhotoUrl()).into(avatar);
            setupRecyclerViews();
            badges.setVisibility(View.VISIBLE);
            artists.setVisibility(View.VISIBLE);
        }
    }
}