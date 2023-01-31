package model;

// SUMMARY
// The GeneratedQuizHistory model is used to track which generated quizzes the user has taken

import java.util.Map;

public class GeneratedQuizHistory {
    private final Map<String, String> quizIds;

    public GeneratedQuizHistory(Map<String, String> quizIds) {
        this.quizIds = quizIds;
    }

    public Map<String, String> getQuizIds() {
        return quizIds;
    }
}
