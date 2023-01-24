package model.quiz;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import model.Quiz;
import model.User;
import model.ValidationObject;
import model.item.Artist;
import model.item.Playlist;
import model.type.QuizType;
import model.type.Severity;
import utils.FormatUtil;
import utils.ValidationUtil;

public class ArtistQuiz extends Quiz {

    private Artist artist;

    private final String TAG = "ArtistQuiz.java";

    public ArtistQuiz(Artist artist, User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        super(user, firebaseUser, type, id, queryId, numQuestions);
        this.artist = artist;
        init();
    }

    private void init() {
        //#region Null check
        // For logging
        final String methodName = FormatUtil.formatMethodName("init");


        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(artist, Artist.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, TAG, methodName)) {
            return;
        }
        //#endregion

        // Check to see if the tracks are known, they absolutely should be
        if (artist.getTracks().size() == 0) {
            Log.e(TAG, String.format("%s %s tracks are unknown.", methodName, playlist.getId()));
            return;
        }
    }
}
