package com.example.musicquizplus.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.musicquizplus.ActiveQuiz;
import com.example.musicquizplus.ParentOfFragments;
import com.example.musicquizplus.PlaylistQuizView;
import com.example.musicquizplus.PlaylistsAdapter;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.User;
import model.item.Playlist;
import service.FirebaseService;

public class PlaylistFragment extends Fragment {

    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private GridView gridView;
    private ImageButton backToTop;
    private View.OnClickListener backToTopListener;
    private DatabaseReference reference;
    private List<String> defaultPlaylistIDs = new ArrayList<>();
    private List<String> firstLaunchPlaylistIds = new ArrayList<>();
    private User user;
    private ProgressBar pgb;
    ParentOfFragments main;
    private boolean firstLaunchEver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        gridView = view.findViewById(R.id.playlistGridView);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        backToTop = ((ParentOfFragments)getActivity()).findViewById(R.id.backToTop);
        reference = FirebaseDatabase.getInstance().getReference();
        pgb = view.findViewById(R.id.playlistProgressBar);
        main = ((ParentOfFragments)getActivity());
        firstLaunchEver = main.getFirstLaunchEver();

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int scroll = gridView.getFirstVisiblePosition();

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


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Adapter playlistAdapter = adapterView.getAdapter();
                Playlist clickedOnPlaylist = (Playlist) playlistAdapter.getItem(i);
                if (clickedOnPlaylist.getId() != null) {
                    Intent intent = new Intent(view.getContext(), PlaylistQuizView.class);
                    intent.putExtra("currentPlaylist", clickedOnPlaylist);
                    intent.putExtra("currentUser", ((ParentOfFragments) getActivity()).getUser());

                    if(firstLaunchEver)
                    {
                        intent.putExtra("firstLaunchEver", firstLaunchEver);
                    }

                    startActivity(intent);
                }
            }
        });
        createBackToTopListener();

        main.setPlaylistsBackToTopListener(backToTopListener);
        if (!main.isBackToTopListenerSet()) {
            main.getBackToTop().setOnClickListener(backToTopListener);
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = getContext();
        Activity activity = getActivity();
        if (user != null && gridView.getCount() == 0) {
            gridView.setAdapter(new PlaylistsAdapter(getContext(), R.layout.gridview_contents, new ArrayList<>()));
        }
        new Thread(new Runnable() {
            public void run() {
                if(firebaseUser != null)
                {
                    user = (User) FirebaseService.checkDatabase(reference, "users", firebaseUser.getUid(), User.class);
                    DatabaseReference userRef = reference.child("users").child(firebaseUser.getUid());
                    user.initPlaylists(reference, userRef);
//                    while(user == null)
//                    {
//                        user = ((ParentOfFragments) getActivity()).getUser();
//                    }
                    /*
                    latch.countDown();

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                     */
                }

                CountDownLatch cdl = new CountDownLatch(1);

                if(firebaseUser != null && user.getPlaylistIds().size() != 0)
                {
                    List<String> playlistIDs = new ArrayList<>(user.getPlaylistIds().values());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pgb.setVisibility(View.GONE);
                        }
                    });
                    if(gridView.getCount() == 0)
                    {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gridView.setAdapter(new PlaylistsAdapter(context, R.layout.gridview_contents, user.getPlaylistsAsList()));
                            }
                        });

//                        FirebaseService.populateGridViewByPlaylistIDs(reference, getActivity(), getContext(), gridView, playlistIDs);
                    }
                }
                else
                {
                    user = new User();
                    user.initGuest(getActivity());
                    new Thread(new Runnable() {
                        public void run() {
                            reference.child("default_playlists").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        defaultPlaylistIDs.add(dataSnapshot.getValue(String.class));
                                    }
                                    cdl.countDown();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            try {
                                cdl.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(defaultPlaylistIDs.size() > 0)
                            {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pgb.setVisibility(View.GONE);
                                    }
                                });
                                if(gridView.getCount() == 0)
                                {
                                    if(firstLaunchEver)
                                    {
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DX1lVhptIYRda");
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DWXRqgorJj26U");
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DX4o1oenSJRJd");
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DXbTxeAdrVG2l");
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DX4UtSsGT1Sbe");
                                        firstLaunchPlaylistIds.add("spotify:playlist:37i9dQZF1DWTJ7xPn4vNaz");

                                        FirebaseService.populateGridViewByPlaylistIDs(reference, getActivity(), getContext(), gridView, firstLaunchPlaylistIds);
                                    }
                                    else
                                    {
                                        FirebaseService.populateGridViewByPlaylistIDs(reference, getActivity(), getContext(), gridView, defaultPlaylistIDs);
                                    }

                                }
                            }
                        }
                    }).start();
                }
            }
        }).start();
    }

    public void createBackToTopListener() {
        backToTopListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridView.setSelection(0);
                backToTop.setVisibility(View.GONE);
            }
        };
    }
}