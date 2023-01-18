package model;

import android.util.Log;

import model.type.ExternalLinkType;

// SUMMARY
// The ExternalLink model stores an external link attached to an Artist

public class ExternalLink {
    private ExternalLinkType type;
    private String url;

    private final static String TAG = "ExternalLink.java";

    public ExternalLink(String externalLinkType, String url) {
        this.url = url;
        this.type = validateType(externalLinkType);
    }

    public ExternalLink() {

    }

    private ExternalLinkType validateType(String externalLinkType) {
        for (ExternalLinkType type : ExternalLinkType.values()) {
            if (externalLinkType.equals(type.toString())) {
                return type;
            }
        }
        Log.v(TAG, String.format("External Link Type \"%s\" is invalid or unsupported.", externalLinkType));
        return ExternalLinkType.INVALID;
    }

    public ExternalLinkType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
