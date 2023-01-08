package model;

enum ExternalLinkType {
    FACEBOOK,
    TWITTER,
    INSTAGRAM,
    WIKIPEDIA,
    INVALID
}

public class ExternalLink {
    private ExternalLinkType type;
    private String url;

    public ExternalLink(String externalLinkType, String url) {
        this.url = url;
        this.type = validateType(externalLinkType);
    }

    private ExternalLinkType validateType(String externalLinkType) {
        for (ExternalLinkType type : ExternalLinkType.values()) {
            if (type.name() == externalLinkType) {
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
