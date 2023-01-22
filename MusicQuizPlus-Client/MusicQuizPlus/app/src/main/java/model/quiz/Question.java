package model.quiz;

import model.quiz.Answer;
import model.type.QuestionType;

public class Question {

    private final QuestionType type;
    private final Answer[] answers;
    private final int answerIndex;

    public Question(QuestionType type, Answer[] answers, int answerIndex) {
        this.type = type;
        this.answers = answers;
        this.answerIndex = answerIndex;
    }

    public QuestionType getType() {
        return type;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }
}
