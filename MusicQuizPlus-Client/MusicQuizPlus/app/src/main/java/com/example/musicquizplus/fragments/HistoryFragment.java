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
import android.widget.ProgressBar;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

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
    private TextView noUserHeader, noCurrentArtistsText;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private User user;
    private ImageView noCurrentArtists;
    private HistoryAdapter adapter;
    List<Track> list = new ArrayList<>();
    List<Track> history = new ArrayList<>();
    private ImageButton backToTop;
    private View.OnClickListener backToTopListener;
    private ProgressBar pgb;

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
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        pgb = view.findViewById(R.id.historyProgressBar);
        noCurrentArtistsText = view.findViewById(R.id.noCurrentArtistsText);
        noCurrentArtists = view.findViewById(R.id.noCurrentArtists);

        if(firebaseUser == null)
        {
            //userLevel.setText(getString(R.string.guest));
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
        populateView();
    }

    public void populateView() {

        if (firebaseUser != null) {
            new Thread(new Runnable() {
                public void run() {

                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);

                    if (firebaseUser != null && user.getHistoryIds().size() != 0) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pgb.setVisibility(View.VISIBLE);
                            }
                        });

                        user.initHistory(db);
                        if(user.getHistory().size() > 50)
                        {
                            history = getFiftyItems();
                        }
                        else
                        {
                            history = user.getHistory();
                        }

                        //List<Track> userTrackHistory = FirebaseService.getTracksListWithPhotoUrls(user.getHistory(), db);
                        CountDownLatch cdl = new CountDownLatch(history.size());

                        for (Track track : history) {
                            if (track.isAlbumKnown()) {
                                //get photourl from album
                                db.child("albums").child(track.getAlbumId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Album album = (Album) dataSnapshot.getValue(Album.class);
                                        track.setPhotoUrl(album.getPhotoUrl());
                                        list.add(track);
                                        cdl.countDown();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }

                                });
                            } else if (track.getPlaylistIds().size() > 0) {
                                //get photourl from playlist
                                String id = track.getPlaylistIds().entrySet().iterator().next().getValue();
                                db.child("playlists").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Playlist playlist = (Playlist) dataSnapshot.getValue(Playlist.class);
                                        track.setPhotoUrl(playlist.getPhotoUrl());
                                        list.add(track);
                                        cdl.countDown();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }

                                });
                            }
                        }
                        try{
                            cdl.await();
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                noCurrentArtists.setVisibility(View.VISIBLE);
                                noCurrentArtistsText.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pgb.setVisibility(View.GONE);
                            adapter = new HistoryAdapter(user, history, null, getContext(), 0);
                            historyRecyclerView.setAdapter(adapter);
                            historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        }
                    });
                            //List<String> historyIds = user.getHistoryIds();
                            //FirebaseService.populateRecyclerViewByHistoryIDs(user, db, getActivity(), getContext(), historyRecyclerView, historyIds);
                }
            }).start();
        }
    }

    private List<Track> getFiftyItems()
    {
        List<Track> fiftyHistoryItems = new ArrayList<>();
        LinkedList<Track> temp = user.getHistory();

        for (int j = 0; j < 50; j++) {
            fiftyHistoryItems.add(temp.getLast());
            temp.removeLast();
        }

        return fiftyHistoryItems;
    }
}