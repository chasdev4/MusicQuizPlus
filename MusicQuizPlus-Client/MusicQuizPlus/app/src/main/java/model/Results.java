package model;

import android.content.Context;

import java.util.List;

public class Results {

    private User user;
    private Quiz quiz;
    private int score;
    private String accuracy;
    List<Badge> earnedBadges;

    public Results(User user, Quiz quiz)
    {
        this.user = user;
        this.quiz = quiz;
        this.score = quiz.getScore();
        this.accuracy = quiz.getAccuracy();
    }

    public List<Badge> getEarnedBadges(Context context) {
        Badge badge = new Badge(user, quiz);
        earnedBadges = badge.getEarnedBadges(context);
        return earnedBadges;
    }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }

}
