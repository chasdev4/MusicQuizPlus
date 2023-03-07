package model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.musicquizplus.MainActivity;
import com.example.musicquizplus.ParentOfFragments;
import com.example.musicquizplus.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import utils.LogUtil;

// SUMMARY
// The GoogleSignIn model stores variables related to Firebase authentication

public class GoogleSignIn {
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private final FirebaseAuth auth;
    private boolean showOneTapUI;

    private final static String TAG = "GoogleSignIn.java";
    private static final int REQ_ONE_TAP = 2;

    public GoogleSignIn() {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public SignInClient getOneTapClient() {
        return oneTapClient;
    }

    public void setOneTapClient(SignInClient signInClient) {
        oneTapClient = signInClient;
    }

    public BeginSignInRequest getSignUpRequest() {
        return signUpRequest;
    }

    public void setSignUpRequest(BeginSignInRequest beginSignInRequest) {
        signUpRequest = beginSignInRequest;
    }

    public void signOut() {
        auth.signOut();
    }

    public void signInWithGoogle(View view, Activity activity, Context context) {
        LogUtil log = new LogUtil(TAG, "signInWithGoogle");
        // Configuration of Google Sign In
        oneTapClient = Identity.getSignInClient(activity);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(context.getString(R.string.SERVER_CLIENT_ID))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        // Begin the Sign In Request
        oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            activity.startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            log.e("Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: (C-Feature) Take the user to a Google Sign In Form to add an account
                        // Note: Might not work or be worth the effort...

                        Snackbar.make(view, "ERROR: No Google accounts associate with this device. Sign In to Google Play Services and try again.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        log.e(e.getLocalizedMessage());
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, Activity activity) {
        LogUtil log = new LogUtil(TAG, "onActivityResult");

        oneTapClient = Identity.getSignInClient(activity);

        // Check the request code
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    // Create an account with a Google ID token
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google.
                        log.d("Got ID token.");

                        // With the Google ID token, exchange it for a Firebase credential,
                        // and authenticate with Firebase using the Firebase credential
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            log.d("signInWithCredential:success");
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            //activity.updateUI(firebaseUser);
                                            Intent intent = new Intent(activity.getBaseContext(), ParentOfFragments.class);
                                            //activity.finish();
                                            activity.startActivity(intent);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            log.w("signInWithCredential:failure", task.getException());
                                            //activity.updateUI(null);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case CommonStatusCodes.CANCELED:
                            log.d("One-tap dialog was closed.");
                            // Don't re-prompt the user.
                            showOneTapUI = false;
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            log.d("One-tap encountered a network error.");
                            // Try again or just ignore.
                            break;
                        default:
                            log.d("Couldn't get credential from result."
                                    + e.getLocalizedMessage());
                            break;
                    }
                }
                break;
        }
    }
}
