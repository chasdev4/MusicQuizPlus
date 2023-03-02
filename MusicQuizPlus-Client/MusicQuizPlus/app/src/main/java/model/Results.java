package model;

import android.content.Context;

import java.io.Serializable;
import java.util.List;

public class Results implements Serializable {

    private User user;
    private int score;
    private String accuracy;
    List<Badge> badges;

    public Results(User user, Quiz quiz, List<Badge> badges)
    {
        this.user = user;
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
