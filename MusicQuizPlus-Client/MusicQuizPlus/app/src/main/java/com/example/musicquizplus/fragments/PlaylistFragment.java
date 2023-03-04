package com.example.musicquizplus.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.musicquizplus.ParentOfFragments;
import com.example.musicquizplus.PlaylistQuizView;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;

import model.GoogleSignIn;
import model.item.Playlist;
import service.FirebaseService;

public class PlaylistFragment extends Fragment {

    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private GridView gridView;
    private ImageButton backToTop;
    private View.OnClickListener backToTopListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        gridView = view.findViewById(R.id.playlistGridView);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        backToTop = ((ParentOfFragments)getActivity()).findViewById(R.id.backToTop);

        if (firebaseUser != null)
        {
            FirebaseService.retrieveData(gridView, getContext(), "playlists", Playlist.class);
        }
        else
        {
            //TODO: Populate GridView with Default Playlists
            FirebaseService.retrieveData(gridView, getContext(), "playlists", Playlist.class);
//            userLevel.setText(getString(R.string.guest));
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

                Adapter playlistAdapter = adapterView.getAdapter();
                Playlist clickedOnPlaylist = (Playlist) playlistAdapter.getItem(i);
                Intent intent = new Intent(view.getContext(), PlaylistQuizView.class);
                intent.putExtra("currentPlaylist", clickedOnPlaylist);
                intent.putExtra("user", ((ParentOfFragments) getActivity()).getUser());
                startActivity(intent);
            }
        });
        createBackToTopListener();
        ParentOfFragments main = ((ParentOfFragments)getActivity());

        main.setPlaylistsBackToTopListener(backToTopListener);
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