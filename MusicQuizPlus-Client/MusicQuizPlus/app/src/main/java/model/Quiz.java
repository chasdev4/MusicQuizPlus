package model;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import model.item.Track;
import model.quiz.Question;
import model.type.QuestionType;
import model.type.QuizType;

public class Quiz {

    private final User user;          // Difficulty, level and xp
    private final FirebaseUser firebaseUser;
    private final QuizType type;
    private final String id;          // TODO: Quiz's ID: What if we saved the quiz to firebase for reuse? B or C Feature
    private final String queryId;     // This id is used for the firebase query
    private final List<Question> questions;     // Note: Initialize this in PlaylistQuiz and ArtistQuiz to determine the number of tracks available first
    private int numQuestions;
    private int currentQuestionIndex;
    private int score;
    private int numCorrect;

    private final int BASE_SCORE = 100;

    public Quiz(User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        this.user = user;
        this.firebaseUser = firebaseUser;
        this.type = type;
        this.id = id;
        this.queryId = queryId;
        questions = new ArrayList<>();
        this.numQuestions = numQuestions;
        currentQuestionIndex = 0;
        score = 0;
        numCorrect = 0;
    }

    public User getUser() {
        return user;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public QuizType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getQueryId() {
        return queryId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions = numQuestions;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    public void incrementQuestionIndex() {
        currentQuestionIndex++;
    }

    public int getScore() {
        return score;
    }

    public void addToScore(int score) {
        this.score += score;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public void incrementNumCorrect() {
        numCorrect++;
    }

    private Question nextQuestion(int lastSelection) {
        if (lastSelection == getQuestions().get(getCurrentQuestionIndex()).getAnswerIndex()) {
            addToScore(BASE_SCORE);
        }
        incrementQuestionIndex();

        return questions.get(currentQuestionIndex);
    }
}
