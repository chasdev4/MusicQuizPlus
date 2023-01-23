package com.example.musicquizplus.fragments;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.musicquizplus.ArtistsView;
import com.example.musicquizplus.R;

import java.util.Objects;

import model.item.Artist;
import service.FirebaseService;


public class ArtistsFragment extends Fragment {

    private View popupSignUpView = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        GridView gridView = view.findViewById(R.id.artistGridView);
        TextView userLevel = view.findViewById(R.id.userLevel);
        View noUser = view.findViewById(R.id.artistNoCurrentUser);
        ImageButton backToTop = view.findViewById(R.id.backToTop);
        View artistsUserAvatar = view.findViewById(R.id.artistsUserAvatar);


        boolean guestAccount;

        if(Objects.equals(userLevel.getText(), "GUEST"))
        {
            guestAccount = true;
            gridView.setVisibility(View.GONE);
            noUser.setVisibility(View.VISIBLE);
        }
        else
        {
            guestAccount = false;
            gridView.setVisibility(View.VISIBLE);
            noUser.setVisibility(View.GONE);
        }

        if(!guestAccount)
        {
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

        backToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridView.setSelection(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        artistsUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Objects.equals(userLevel.getText(), "GUEST")) {

                    // Create a AlertDialog Builder.
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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


        // Inflate the layout for this fragment
        return view;
    }
}