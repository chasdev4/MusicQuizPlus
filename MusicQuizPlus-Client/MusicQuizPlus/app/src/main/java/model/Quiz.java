package model;

import com.google.firebase.auth.FirebaseUser;

import model.type.QuizType;

public class Quiz {

    private final User user;          // Difficulty, level and xp
    private final FirebaseUser firebaseUser;
    private final QuizType type;
    private final String id;          // TODO: Quiz's ID: What if we could save the quiz to firebase? B or C Feature
    private final String queryId;     // This id is used for the firebase query
    private final Question[] questions;

    public Quiz(User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        this.user = user;
        this.firebaseUser = firebaseUser;
        this.type = type;
        this.id = id;
        this.queryId = queryId;
        questions = new Question[numQuestions];
    }

}
