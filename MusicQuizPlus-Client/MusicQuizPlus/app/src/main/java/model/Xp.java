package model;

import java.util.Map;

public class Xp {
    private int previousLevel;
    private int currentLevel;
    private int maxLevel;
    private int previousXp;
    private int currentXp;
    private Map<Integer, Integer> levels;

    private static int DURATION = 3000;

    public Xp(int previousLevel, int currentLevel, int maxLevel, int previousXp, int currentXp, Map<Integer, Integer> levels) {
        this.previousLevel = previousLevel;
        this.currentLevel = currentLevel;
        this.maxLevel = maxLevel;
        this.previousXp = previousXp;
        this.currentXp = currentXp;
        this.levels = levels;
    }

    // Returns a list of durations the xp bar has to iterate
    public int getLevelsProgressed() {
        if (currentLevel == maxLevel) {
            return 0;
        }
        return currentLevel - previousLevel;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public Map<Integer, Integer> getLevels() {
        return levels;
    }

    public int getPreviousXp() {
        return previousXp;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getDuration(int tempXp, int tempLevel, int maxDuration, int levelsProgressed) {
        if (tempXp == 0 && levelsProgressed > 1) {
            return maxDuration;
        }
        double percent = tempXp / levels.get(tempLevel + 1);
        double progress = ((percent > 1) ? 1 : percent);
        return (int)(maxDuration * percent);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}
