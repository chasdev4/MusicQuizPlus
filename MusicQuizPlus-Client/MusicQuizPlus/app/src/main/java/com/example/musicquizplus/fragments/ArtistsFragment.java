package com.example.musicquizplus.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.musicquizplus.ArtistQuizView;
import com.example.musicquizplus.ParentOfFragments;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.GoogleSignIn;
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

        if(firebaseUser == null)
        {
            gridView.setVisibility(View.GONE);
            noUser.setVisibility(View.VISIBLE);
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
            gridView.setVisibility(View.VISIBLE);
            noUser.setVisibility(View.GONE);
            FirebaseService.retrieveData(gridView, getContext(), "artists", Artist.class);
        }

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