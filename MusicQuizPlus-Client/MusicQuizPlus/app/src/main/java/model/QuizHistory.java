package model;

import java.util.Map;

// SUMMARY
// The QuizHistory model is used for keeping track of a specific quiz's history of tracks heard

public class QuizHistory {

    private String id;          // Artist or Playlist ID
    private Map<String, String> trackIds;

    public QuizHistory() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(Map<String, String> trackIds) {
        this.trackIds = trackIds;
    }

    public boolean addTrackId(String key, String trackId) {
        if (trackIds.containsValue(trackId)) {
            return false;
        }
        trackIds.put(key, trackId);
        return true;
    }
}
