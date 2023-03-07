package model;

import android.content.Context;

import java.io.Serializable;
import java.util.List;

public class Results implements Serializable {

    private User user;
    private int xp;
    private int score;
    private String accuracy;
    List<Badge> badges;
    private Xp xpBar;


    public Results(User user, Quiz quiz, int previousXp, int previousLevel, List<Badge> badges) {

        this.score = quiz.getScore();
        this.accuracy = quiz.getAccuracy();
        this.badges = badges;
        this.xp = quiz.getXp();
        this.user = user;
        this.xpBar = new Xp(previousLevel, user.getLevel(), 100, previousXp, user.getXp(), user.getLevels());
    }

    public Results(int score, int currentLevel, int previousLevel, int previousXp, int currentXp, String accuracy, List<Badge> badges, int quizXp) {

        this.score = score;
        this.accuracy = accuracy;
        this.badges = badges;
        xp = quizXp;
        user = new User();
        this.xpBar = new Xp(previousLevel, currentLevel, 100, previousXp, currentXp, user.getLevels());

    }

    public List<Badge> getBadges() { return badges; }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public Xp getXpBar() {
        return xpBar;
    }

    public int getXp() {
        return xp;
    }

    public User getUser() {
        return user;
    }
}
