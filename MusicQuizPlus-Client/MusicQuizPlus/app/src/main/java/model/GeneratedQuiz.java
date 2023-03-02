package model;

import java.io.Serializable;

import model.type.Difficulty;

public class GeneratedQuiz implements Serializable {
    private String quizId;
    private Difficulty difficulty;

    public GeneratedQuiz(String quizId, Difficulty difficulty) {
        this.quizId = quizId;
        this.difficulty = difficulty;
    }

    public GeneratedQuiz() {}

    public String getQuizId() { return quizId; }
    public Difficulty getDifficulty() { return difficulty; }
}
