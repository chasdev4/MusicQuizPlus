package com.example.musicquizplus.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicquizplus.ArtistQuizView;
import com.example.musicquizplus.ParentOfFragments;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.GoogleSignIn;
import model.User;
import model.item.Artist;
import service.FirebaseService;

public class ArtistsFragment extends Fragment {

    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private GridView gridView;
    private Button googleSignInBtn;
    private View noUser;
    private ImageButton backToTop;
    private View.OnClickListener backToTopListener;
    private ImageView noCurrentArtists;
    private TextView noCurrentArtistsText;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        gridView = view.findViewById(R.id.artistGridView);
        noUser = view.findViewById(R.id.artistNoCurrentUser);
        backToTop = ((ParentOfFragments)getActivity()).findViewById(R.id.backToTop);
        googleSignInBtn = view.findViewById(R.id.googleSignInButton);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();
        noCurrentArtists = view.findViewById(R.id.noCurrentArtists);
        noCurrentArtistsText = view.findViewById(R.id.noCurrentArtistsText);

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

                Adapter artistAdapter = adapterView.getAdapter();
                Artist clickedOnArtist = (Artist) artistAdapter.getItem(i);
                Intent intent = new Intent(view.getContext(), ArtistQuizView.class);
                intent.putExtra("currentUser", user);
                intent.putExtra("currentArtist", clickedOnArtist);
                startActivity(intent);
            }
        });

        createBackToTopListener();
        ParentOfFragments main = ((ParentOfFragments)getActivity());

        main.setArtistsBackToTopListener(backToTopListener);
        if (!main.isBackToTopListenerSet()) {
            main.getBackToTop().setOnClickListener(backToTopListener);
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(new Runnable() {
            public void run() {

                if(firebaseUser != null)
                {
                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    latch.countDown();

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridView.setVisibility(View.VISIBLE);
                            noCurrentArtists.setVisibility(View.VISIBLE);
                            noCurrentArtistsText.setVisibility(View.VISIBLE);
                            noUser.setVisibility(View.GONE);
                        }
                    });

                    if(user.getArtistIds().size() > 0)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                noCurrentArtists.setVisibility(View.GONE);
                                noCurrentArtistsText.setVisibility(View.GONE);
                            }
                        });
                        List<String> artistIds = new ArrayList<>(user.getArtistIds().values());
                        FirebaseService.populateGridViewByArtistIDs(db, getActivity(), getContext(), gridView, artistIds);
                    }

                }
                else
                {
                    user = new User();
                    user.initGuest(getActivity());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridView.setVisibility(View.GONE);
                            noCurrentArtists.setVisibility(View.GONE);
                            noCurrentArtistsText.setVisibility(View.GONE);
                            noUser.setVisibility(View.VISIBLE);
                            googleSignInBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    GoogleSignIn signInWGoogle = new GoogleSignIn();
                                    signInWGoogle.signInWithGoogle(view, getActivity(), view.getContext());
                                }
                            });
                        }
                    });
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