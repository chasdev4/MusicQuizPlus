package model;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class GoogleSignIn {
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private final FirebaseAuth auth;

    private final static String TAG = "GoogleSignIn.java";

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
}
