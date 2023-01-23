package model.quiz;

import model.type.QuestionType;

public class Question {

    private final QuestionType type;
    private final String[] answers;
    private final int answerIndex;

    public Question(QuestionType type, String[] answers, int answerIndex) {
        this.type = type;
        this.answers = answers;
        this.answerIndex = answerIndex;
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
}
