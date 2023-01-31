package model;

import java.util.HashMap;
import java.util.Map;

// SUMMARY
// The QuizHistory model is used for keeping track of a specific quiz's history of tracks heard

public class QuizHistory {

//    private String id;          // Topic ID
    private Map<String, String> trackIds;
//    private Map<String, String> quizIds;
    private int total;
    private int count;

    public QuizHistory() {

    }

    //#region Accessors
//    public String getId() {return id;}
    public Map<String, String> getTrackIds() {
        return trackIds;
    }
//    public Map<String, String> getQuizIds() { return quizIds; }
    public int getTotal() { return total; }
    public int getCount() { return count; }
    //#endregion

    //#region Mutators
//    public void setId(String id) {
//        this.id = id;
//    }
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
