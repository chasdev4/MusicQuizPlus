package model;

import model.type.QuestionType;

public class Question {

    private final QuestionType type;
    private final Answer[] answers;

    public Question(QuestionType type, Answer[] answers) {
        this.type = type;
        this.answers = answers;
    }

    public QuestionType getType() {
        return type;
    }

    public Answer[] getAnswers() {
        return answers;
    }
}
