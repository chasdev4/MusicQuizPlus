package model;

import java.util.HashMap;
import java.util.Map;

// SUMMARY
// The QuizHistory model is used for keeping track of a specific quiz's history of tracks heard

public class QuizHistory {
    private Map<String, String> trackIds;
    private int total;
    private int count;

    public QuizHistory(Map<String, String> trackIds, int total, int count) {
        this.trackIds = trackIds;
        this.total = total;
        this.count = count;
    }

    public QuizHistory() {

    }

    //#region Accessors
    public Map<String, String> getTrackIds() {
        return trackIds;
    }
    public int getTotal() { return total; }
    public int getCount() { return count; }
    //#endregion

    //#region Mutators
    public void setTrackIds(Map<String, String> trackIds) {
        this.trackIds = trackIds;
    }
    public boolean addTrackId(String key, String trackId) {
        if (trackIds == null) {
            trackIds = new HashMap<>();
        }
        if (trackIds.containsValue(trackId)) {
            return false;
        }
        trackIds.put(key, trackId);
        return true;
    }
    public void setTotal(int total) { this.total = total; }
    public void incrementCount() { count++; }
    //#endregion








}
