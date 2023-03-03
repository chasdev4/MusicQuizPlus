package model;

import java.util.List;

public class Results {

    private User user;
    private Quiz quiz;
    private int score;
    private int xp;
    private int previousXp;
    private String accuracy;
    List<Badge> badges;

    public Results(User user, Quiz quiz, int previousXp, List<Badge> badges)
    {
        this.user = user;
        this.quiz = quiz;
        this.xp = quiz.getXp();
        this.score = quiz.getScore();
        this.accuracy = quiz.getAccuracy();
        this.previousXp = previousXp;
        this.badges = badges;
    }

    public List<Badge> getBadges() { return badges; }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public int getXp() {
        return xp;
    }

    public int getPreviousXp() {
        return previousXp;
    }
}
