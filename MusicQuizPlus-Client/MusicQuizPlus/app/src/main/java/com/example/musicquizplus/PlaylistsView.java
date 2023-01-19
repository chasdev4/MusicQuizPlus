package com.example.musicquizplus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import model.PhotoUrl;
import model.item.Playlist;
import service.FirebaseService;

public class PlaylistsView extends AppCompatActivity {

    private View popupSignUpView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists_view);

        GridView gridView = findViewById(R.id.playlistGridView);
        TextView userLevel = findViewById(R.id.userLevel);
        ImageButton backToTop = findViewById(R.id.backToTop);
        View playlistUserAvatar = findViewById(R.id.playlistUserAvatar);


        if(Objects.equals(userLevel.getText(), "GUEST")) {
            FirebaseService.retrieveData(gridView, this, "playlists", Playlist.class);
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

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridView.setSelection(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        playlistUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Objects.equals(userLevel.getText(), "GUEST")) {

                    // Create a AlertDialog Builder.
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlaylistsView.this);
                    // Set title, icon, can not cancel properties.
                    alertDialogBuilder.setTitle("Sign Up for MusicQuizPlus");
                    alertDialogBuilder.setIcon(R.drawable.magicstar);
                    alertDialogBuilder.setCancelable(false);

                    // Init popup dialog view and it's ui controls.
                    popupSignUpView = View.inflate(view.getContext(), R.layout.logged_out_message, null);
                    // Set the inflated layout view object to the AlertDialog builder.
                    alertDialogBuilder.setView(popupSignUpView);

                    // Create AlertDialog and show.
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ImageButton cancelPopUp = alertDialog.findViewById(R.id.closeDialogButton);
                    TextView noThanksLink = alertDialog.findViewById(R.id.noThanksHyperLink);
                    TextView signUpHeader = alertDialog.findViewById(R.id.logged_out_header);
                    TextView linkGoogle = alertDialog.findViewById(R.id.link_google);
                    TextView accountBenefits = alertDialog.findViewById(R.id.account_benefits);
                    View entireGuestMessage = alertDialog.findViewById(R.id.entireGuestUserMessage);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 1300);

                    entireGuestMessage.setLayoutParams(params);
                    alertDialog.getWindow().setLayout(1000, 1500); //Controlling width and height.

                    noThanksLink.setVisibility(View.VISIBLE);
                    noThanksLink.setPaintFlags(noThanksLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    cancelPopUp.setVisibility(View.VISIBLE);
                    signUpHeader.setTextSize(22);
                    signUpHeader.setText(R.string.user_profile_signup_header);
                    linkGoogle.setTextSize(14);
                    accountBenefits.setTextSize(14);

                    noThanksLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });

                    cancelPopUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });
                }
                else
                {
                    //pull up user profile
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Adapter playlistAdapter = adapterView.getAdapter();
                Playlist clickedOnPlaylist = (Playlist) playlistAdapter.getItem(i);
                Intent intent = new Intent(view.getContext(), QuizView.class);
                intent.putExtra("currentPlaylist", clickedOnPlaylist);
                startActivity(intent);
            }
        });

    }
}