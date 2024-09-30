package gridwindow.top;

public enum Animation {
    NONE("none", "None"),
    FADE("fade", "Fading"),
    ROTATE("rotate", "Rotating");

    private final String identifier;
    private final String displayName;

    Animation(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        return displayName;
    }
}
