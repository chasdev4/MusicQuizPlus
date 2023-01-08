package model;

import model.type.ExternalLinkType;

public class ExternalLink {
    private ExternalLinkType type;
    private String url;

    public ExternalLink(String externalLinkType, String url) {
        this.url = url;
        this.type = validateType(externalLinkType);
    }

    private ExternalLinkType validateType(String externalLinkType) {
        for (ExternalLinkType type : ExternalLinkType.values()) {
            String what = type.toString();
            if (externalLinkType.equals(type.toString())) {
                return type;
            }
        }

        return ExternalLinkType.INVALID;
    }

    public ExternalLinkType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
