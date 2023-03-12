package model;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.musicquizplus.R;


public class SignUpPopUp {

    private Context context;
    private String header;
    private Activity activity;

    public SignUpPopUp(Activity activity, Context context, String header)
    {
        this.activity = activity;
        this.context = context;
        this.header = header;

        //init();
    }

    public void createAndShow()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Sign Up for MusicQuizPlus");
        alertDialogBuilder.setIcon(R.mipmap.logo_with_gradient);
        alertDialogBuilder.setCancelable(false);

        // Init popup dialog view and it's ui controls.
        View popupSignUpView = View.inflate(context, R.layout.logged_out_message, null);
        // Set the inflated layout view object to the AlertDialog builder.
        alertDialogBuilder.setView(popupSignUpView);

        // Create AlertDialog and show.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button signInWithGoogle = alertDialog.findViewById(R.id.googleSignInButton);
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
        signUpHeader.setText(header);
        linkGoogle.setTextSize(14);
        accountBenefits.setTextSize(14);

        signInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignIn googleSignIn = new GoogleSignIn();
                googleSignIn.signInWithGoogle(view, activity, view.getContext());
                alertDialog.cancel();
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
}
