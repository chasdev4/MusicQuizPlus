package model;

import android.content.Context;

import java.util.List;

public class Results {

    private User user;
    private Quiz quiz;
    private int score;
    private String accuracy;
    List<Badge> badges;

    public Results(User user, Quiz quiz, List<Badge> badges)
    {
        this.user = user;
        this.quiz = quiz;
        this.score = quiz.getScore();
        this.accuracy = quiz.getAccuracy();
        this.badges = badges;
    }

    public List<Badge> getBadges() { return badges; }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }
}
