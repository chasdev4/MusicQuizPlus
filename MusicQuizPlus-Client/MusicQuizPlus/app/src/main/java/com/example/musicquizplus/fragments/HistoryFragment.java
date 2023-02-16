package com.example.musicquizplus.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicquizplus.ArtistsAdapter;
import com.example.musicquizplus.HistoryAdapter;
import com.example.musicquizplus.HistoryView;
import com.example.musicquizplus.MainActivity;
import com.example.musicquizplus.PlaylistsAdapter;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import service.FirebaseService;

public class HistoryFragment extends Fragment {

    private View popupSignUpView = null;
    private Button googleSignInBtn;
    private RecyclerView historyRecyclerView;
    private TextView userLevel;
    private View noCurrentUser;
    private TextView noUserHeader;
    private ImageButton backToTop;
    private View historyUserAvatar;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;
    private ImageView userCustomAvatar;
    HistoryAdapter adapter;
    List<Track> list = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        googleSignInBtn = view.findViewById(R.id.googleSignInButton);
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        userLevel = view.findViewById(R.id.userLevel);
        noCurrentUser = view.findViewById(R.id.historyNoCurrentUser);
        noUserHeader = view.findViewById(R.id.logged_out_header);
        backToTop = view.findViewById(R.id.backToTop);
        historyUserAvatar = view.findViewById(R.id.historyUserAvatar);
        userCustomAvatar = view.findViewById(R.id.userCustomAvatar);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null)
        {
            userLevel.setText(getString(R.string.guest));
            historyRecyclerView.setVisibility(View.GONE);
            noUserHeader.setText(R.string.guestUserHistory);
            noUserHeader.setTextSize(32);
            noCurrentUser.setVisibility(View.VISIBLE);

            googleSignInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoogleSignIn signInWGoogle = new GoogleSignIn();
                    signInWGoogle.signInWithGoogle(view, getActivity(), view.getContext());
                }
            });
        }
        else
        {
            historyRecyclerView.setVisibility(View.VISIBLE);
            noCurrentUser.setVisibility(View.GONE);
            //TODO: retreive history from firebase and populate listview

        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("tracks");
        reference.limitToFirst(50).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Track track = dataSnapshot.getValue(Track.class);
                    list.add(track);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        historyRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) historyRecyclerView.getLayoutManager();
                int scroll = llm.findFirstVisibleItemPosition();

                if(scroll > 0)
                {
                    backToTop.setVisibility(View.VISIBLE);
                }
                else
                {
                    backToTop.setVisibility(View.GONE);
                }

            }
        });

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyRecyclerView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        historyUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseUser == null) {
                    SignUpPopUp signUpPopUp = new SignUpPopUp(getActivity(), getContext(), getString(R.string.user_profile_signup_header));
                    signUpPopUp.createAndShow();
                }
                else
                {
                    //pull up user profile
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateView();
    }

    public void populateView() {
        if (firebaseUser != null)
        {
            new Thread(new Runnable() {
                public void run() {

                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            userLevel.setText(String.format(Locale.ENGLISH, "%s %d", getString(R.string.lvl), user.getLevel()));
                            if(user.getPhotoUrl() != null)
                            {
                                userCustomAvatar.setImageBitmap(getBitmapFromURL(user.getPhotoUrl()));
                            }
                            if (list.size() > 0) {
                                adapter = new HistoryAdapter(list, getContext());
                                historyRecyclerView.setAdapter(adapter);
                                historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            }

                        }
                    });
                }
            }).start();
        }
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}