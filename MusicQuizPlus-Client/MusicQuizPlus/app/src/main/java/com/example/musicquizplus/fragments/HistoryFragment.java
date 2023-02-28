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
import com.example.musicquizplus.ParentOfFragments;
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
import model.Quiz;
import model.SignUpPopUp;
import model.User;
import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import service.FirebaseService;

public class HistoryFragment extends Fragment {

    private Button googleSignInBtn;
    private RecyclerView historyRecyclerView;
    private TextView userLevel;
    private View noCurrentUser;
    private TextView noUserHeader;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private User user;
    private ImageView userCustomAvatar;
    HistoryAdapter adapter;
    List<Track> list = new ArrayList<>();
    private ImageButton backToTop;
    private View.OnClickListener backToTopListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        googleSignInBtn = view.findViewById(R.id.googleSignInButton);
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        userLevel = view.findViewById(R.id.userLevel);
        noCurrentUser = view.findViewById(R.id.historyNoCurrentUser);
        noUserHeader = view.findViewById(R.id.logged_out_header);
        backToTop = ((ParentOfFragments)getActivity()).findViewById(R.id.backToTop);
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
        }

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

        //TODO: retreive history from firebase and populate listview
        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        db.child("tracks").limitToFirst(50).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Track track = dataSnapshot.getValue(Track.class);
                    //list.add(track);
                    if(track.isAlbumKnown())
                    {
                        //get photourl from album
                        db.child("albums").child(track.getAlbumId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Album album = (Album) dataSnapshot.getValue(Album.class);
                                //String url = album.getPhotoUrl().get(0).getUrl();
                                //imageUrlList.add(url);
                                // TODO: Solve reason for crash. Null check prevents crash but motivations not understood
                                if (album != null) {
                                    track.setPhotoUrl(album.getPhotoUrl());
                                    list.add(track);
                                    if (list.size() == 50) {
                                        populateView();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }

                        });
                    }
                    else if(track.getPlaylistIds().size() > 0)
                    {
                        //get photourl from playlist
                        String id = track.getPlaylistIds().entrySet().iterator().next().getValue();
                        db.child("playlists").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Playlist playlist = (Playlist) dataSnapshot.getValue(Playlist.class);
                                //String url = playlist.getPhotoUrl().get(0).getUrl();
                                track.setPhotoUrl(playlist.getPhotoUrl());
                                list.add(track);
                                if(list.size() == 50)
                                {
                                    populateView();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }

                        });
                    }
                    else
                    {
                        //TODO: Get bitmap and use placeholder image made by Charles
                        //String placeholder = "https://i.pinimg.com/originals/30/7e/28/307e285cde65e9af6a931a546094379c.jpg";
                        list.add(track);
                        if(list.size() == 50)
                        {
                            populateView();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        createBackToTopListener();
        ParentOfFragments main = ((ParentOfFragments)getActivity());

        main.setHistoryBackToTopListener(backToTopListener);
        if (!main.isBackToTopListenerSet()) {
            main.getBackToTop().setOnClickListener(backToTopListener);
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void createBackToTopListener() {
        backToTopListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyRecyclerView.scrollToPosition(0);
                backToTop.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

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
                            adapter = new HistoryAdapter(list, null, getContext(), 0);
                            historyRecyclerView.setAdapter(adapter);
                            historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                        }
                    });
                }
            }).start();
        }
    }

    private static Bitmap getBitmapFromURL(String src) {
        Bitmap image = null;
        try {
            URL url = new URL(src);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
        return image;
        /*
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

         */
    }
}