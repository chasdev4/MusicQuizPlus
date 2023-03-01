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

import com.squareup.picasso.Picasso;

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
    private ImageButton backButton;
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
        backButton = findViewById(R.id.profile_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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

        badges.setLayoutManager(new GridLayoutManager(this, 5));
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
        artists.setLayoutManager(new GridLayoutManager(this, 3));
        artists.setHasFixedSize(true);

        artists.setAdapter(artistsAdapter);

        onDataChange();
    }

    private void onArtistDataChanged() {
        if (artistsAdapter.getItemCount() == 0) {
            // TODO: Hide Section
        }
    }

    private void onDataChange() {
        onBadgeDataChange();
        onArtistDataChanged();
    }
    private void onBadgeDataChange() {
        if (badgesAdapter.getItemCount() == 0) {
            // TODO: Hide Section
        }
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
        }
    }
}