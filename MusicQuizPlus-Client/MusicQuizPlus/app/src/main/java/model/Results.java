package model;

import android.content.Context;

import java.util.List;

public class Results {

    private User user;
    private Quiz quiz;
    private int score;
    private String accuracy;
    private int bonusXP;
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
        bonusXP = badge.getBonusXP();
        return earnedBadges;
    }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public int getBonusXP() { return bonusXP; }
}
