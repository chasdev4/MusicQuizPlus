package com.example.musicquizplus.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicquizplus.HistoryAdapter;
import com.example.musicquizplus.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import model.GoogleSignIn;
import model.SignUpPopUp;
import model.User;
import model.item.Artist;
import service.FirebaseService;

public class ArtistsFragment extends Fragment {

    private View popupSignUpView = null;
    private User user;
    private TextView userLevel;
    private ImageView userCustomAvatar;
    private GoogleSignIn googleSignIn;
    private FirebaseUser firebaseUser;
    private DatabaseReference db;
    private View artistsUserAvatar;
    private ImageButton backToTop;
    private GridView gridView;
    private Button googleSignInBtn;
    private View noUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        gridView = view.findViewById(R.id.artistGridView);
        userLevel = view.findViewById(R.id.userLevel);
        noUser = view.findViewById(R.id.artistNoCurrentUser);
        backToTop = view.findViewById(R.id.backToTop);
        artistsUserAvatar = view.findViewById(R.id.artistsUserAvatar);
        googleSignInBtn = view.findViewById(R.id.googleSignInButton);
        userCustomAvatar = view.findViewById(R.id.userCustomAvatar);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null)
        {
            userLevel.setText(getString(R.string.guest));
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