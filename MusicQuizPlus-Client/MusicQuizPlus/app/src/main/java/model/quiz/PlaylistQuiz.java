package model.quiz;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Answer;
import model.Question;
import model.Quiz;
import model.User;
import model.ValidationObject;
import model.item.Playlist;
import model.item.Track;
import model.type.QuizType;
import model.type.Severity;
import utils.FormatUtil;
import utils.ValidationUtil;

public class PlaylistQuiz extends Quiz {
    private Playlist playlist;

    private final String TAG = "PlaylistQuiz.java";


    public PlaylistQuiz(Playlist playlist, User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        super(user, firebaseUser, type, id, queryId, numQuestions);
        this.playlist = playlist;
        init();
    }

    private void init() {
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(playlist, Playlist.class, Severity.HIGH));
            }
        };
        final String methodName = FormatUtil.formatMethodName("getQuestions");
        // Null check
        if (ValidationUtil.nullCheck(validationObjects, TAG, methodName)) {
            return;
        }

        // Check to see if the trackId's are known, they absolutely should be
        if (!playlist.isTrackIdsKnown()) {
            Log.e(TAG, String.format("%s %s track Id's are unknown.", methodName, playlist.getId()));
            return;
        }

        // Check the number of tracks available
        int numTracks = playlist.getTracks().size();

        // If there are less tracks than questions available, resize question array
        if (numTracks < getNumQuestions()) {
            setNumQuestions(numTracks);
        }

        // Check to see if numTracks was reassigned or equal to numQuestions
        // If they're equal the user will be quizzed on the entire track set
        if (numTracks == getNumQuestions()) {
            for (int i = 0; i < numTracks; i++) {
                Track track = playlist.getTracks().get(i);

                // Create an inner loop to get 4 answers
                for (int j = 0; j < 4; j++) {

                    Answer answer = new Answer(text, );
                }
            }
        }
        else
        {
            for (int i = 0; i < getNumQuestions(); i++) {

            }
        }




    }
}
