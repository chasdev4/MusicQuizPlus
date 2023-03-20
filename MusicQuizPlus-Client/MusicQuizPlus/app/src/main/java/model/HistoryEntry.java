package model;

import java.io.Serializable;

public class HistoryEntry implements Serializable {
    private String id;
    private String source;

    public HistoryEntry(String id, String photoUrl) {
        this.id = id;
        this.source = photoUrl;
    }

    public HistoryEntry() {}

    public String getSource() {
        return source;
    }

    public String getId() {
        return id;
    }

    public void setSource(String albumId) {
        this.source = albumId;
    }
}
