package model;

import java.util.List;

public class Results {

    private User user;
    private Quiz quiz;
    private int score;
    private String accuracy;
    List<Badge> badges;
    private Xp xpBar;

//    public Results(User user, Quiz quiz, int previousXp, int previousLevel, List<Badge> badges)
//    {
//        this.user = user;
//        this.quiz = quiz;
//        this.xp = quiz.getXp();
//        this.score = quiz.getScore();
//        this.accuracy = quiz.getAccuracy();
//        this.previousXp = previousXp;
//        this.previousLevel = previousLevel;
//        this.badges = badges;
//    }

    // Debug constructor
    public Results(int score, int xp, int level, int previousXp, int previousLevel, String accuracy, List<Badge> badges) {

        this.score = score;
        this.accuracy = accuracy;
        this.badges = badges;
        User user = new User();
        this.xpBar = new Xp(previousLevel, level, 100, previousXp, xp, user.getLevels());
    }

    public List<Badge> getBadges() { return badges; }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }

//    public int getXp() {
//        return xp;
//    }
//
//    public int getPreviousXp() {
//        return previousXp;
//    }

//    public int getPreviousLevel() {
//        return xpBar.getPreviousLevel();
//    }
//
//    public int getCurrentLevel() {
//        return xpBar.getCurrentLevel();
//    }

    public Xp getXpBar() {
        return xpBar;
    }
}
