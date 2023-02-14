package com.example.musicquizplus.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

import com.example.musicquizplus.HistoryView;
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
import model.User;
import service.FirebaseService;

public class HistoryFragment extends Fragment {

    private View popupSignUpView = null;
    private Button googleSignInBtn;
    private ListView listView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        googleSignInBtn = view.findViewById(R.id.googleSignInButton);
        listView = view.findViewById(R.id.historyListView);
        userLevel = view.findViewById(R.id.userLevel);
        noCurrentUser = view.findViewById(R.id.historyNoCurrentUser);
        noUserHeader = view.findViewById(R.id.logged_out_header);
        backToTop = view.findViewById(R.id.backToTop);
        historyUserAvatar = view.findViewById(R.id.historyUserAvatar);
        userCustomAvatar = view.findViewById(R.id.userCustomAvatar);
        googleSignIn = new GoogleSignIn();
        firebaseUser = googleSignIn.getAuth().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        if(Objects.equals(userLevel.getText(), "GUEST"))
        {
            listView.setVisibility(View.GONE);
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
            listView.setVisibility(View.VISIBLE);
            noCurrentUser.setVisibility(View.GONE);
            //TODO: retreive history from firebase and populate listview
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                int scroll = listView.getFirstVisiblePosition();

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
                listView.setSelection(0);
                backToTop.setVisibility(View.GONE);
            }
        });

        historyUserAvatar.setOnClickListener(new View.OnClickListener() {
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

                    Button signInWithGoogle = alertDialog.findViewById(R.id.googleSignInButton);
                    noThanksLink.setVisibility(View.VISIBLE);
                    noThanksLink.setPaintFlags(noThanksLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    cancelPopUp.setVisibility(View.VISIBLE);
                    signUpHeader.setTextSize(22);
                    signUpHeader.setText(R.string.user_profile_signup_header);
                    linkGoogle.setTextSize(14);
                    accountBenefits.setTextSize(14);

                    signInWithGoogle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GoogleSignIn googleSignIn = new GoogleSignIn();
                            googleSignIn.signInWithGoogle(view, getActivity(), view.getContext());
                        }
                    });

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

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseUser != null)
        {
            new Thread(new Runnable() {
                public void run() {
                    user = (User) FirebaseService.checkDatabase(db, "users", firebaseUser.getUid(), User.class);
                    userLevel.setText(String.format(Locale.ENGLISH, "%s %d", getString(R.string.lvl), user.getLevel()));
                    if(user.getPhotoUrl() != null)
                    {
                        userCustomAvatar.setImageBitmap(getBitmapFromURL(user.getPhotoUrl()));
                    }
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