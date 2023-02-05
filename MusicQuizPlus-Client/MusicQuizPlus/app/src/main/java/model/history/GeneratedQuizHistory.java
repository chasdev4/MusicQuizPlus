package model.history;

// SUMMARY
// The GeneratedQuizHistory model is used to track which generated quizzes the user has taken

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class GeneratedQuizHistory {
    private Map<String, String> quizIds;

    public GeneratedQuizHistory(Map<String, String> quizIds) {
        this.quizIds = quizIds;
    }

    public GeneratedQuizHistory() {}

    public Map<String, String> getQuizIds() {
        return quizIds;
    }
    @Exclude
    public boolean isQuizIdsNull() {
        return quizIds == null;
    }

    public void setQuizIds(Map<String, String> quizIds) {
        this.quizIds = quizIds;
    }
}
