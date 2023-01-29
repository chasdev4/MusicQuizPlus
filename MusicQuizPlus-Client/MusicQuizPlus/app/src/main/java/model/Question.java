package model;

import java.io.Serializable;

import model.type.QuestionType;

// SUMMARY
// The question model stores information about a single question in a quiz

public class Question implements Serializable {

    private final QuestionType type;
    private final String[] answers;
    private final int answerIndex;
    private final String previewUrl;

    public Question(QuestionType type, String[] answers, int answerIndex, String previewUrl) {
        this.type = type;
        this.answers = answers;
        this.answerIndex = answerIndex;
        this.previewUrl = previewUrl;

    }

    public QuestionType getType() {
        return type;
    }

    public String[] getAnswers() {
        return answers;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }




}
