package model.quiz;

import com.google.firebase.auth.FirebaseUser;

import model.Quiz;
import model.User;
import model.item.Artist;
import model.type.QuizType;

public class ArtistQuiz extends Quiz {

    private Artist artist;

    public ArtistQuiz(Artist artist, User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        super(user, firebaseUser, type, id, queryId, numQuestions);
        this.artist = artist;
    }
}
