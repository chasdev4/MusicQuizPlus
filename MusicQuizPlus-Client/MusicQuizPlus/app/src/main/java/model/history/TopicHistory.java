package model.history;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// SUMMARY
// The TopicHistory model is used for keeping track of a playlist history or individual album history.

public class TopicHistory implements Serializable {
    private Map<String, String> trackIds;
    private int total;
    private int count;

    public TopicHistory(Map<String, String> trackIds, int total, int count) {
        this.trackIds = trackIds;
        this.total = total;
        this.count = count;
    }

    public TopicHistory(int total) {
        this.total = total;
        count=0;
        trackIds = new HashMap<>();
    }

    public TopicHistory() {
        trackIds = new HashMap<>();
        total = 0;
        count = 0;
    }

    //#region Accessors
    public Map<String, String> getTrackIds() {
        return trackIds;
    }
    public int getTotal() { return total; }
    public int getCount() { return count; }
    @Exclude
    public boolean isTrackIdsNull() {
        return trackIds == null;
    }
    //#endregion

    //#region Mutators
    public void setTrackIds(Map<String, String> trackIds) {
        this.trackIds = trackIds;
    }
    public void setTotal(int total) { this.total = total; }
    public void setCount(int count) {this.count = count; }
    public void incrementCount() { count++;}
    public boolean addTrackId(String key, String trackId) {
        if (total != 0 && total == count) {
            return false;
        }
        if (trackIds == null) {
            trackIds = new HashMap<>();
        }
        if (trackIds.containsValue(trackId)) {
            return false;
        }
        trackIds.put(key, trackId);
        return true;
    }
    //#endregion
}
