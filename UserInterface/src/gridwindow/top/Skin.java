package gridwindow.top;

public enum Skin {
    DEFAULT("defaultcolor", "Default"),
    DARK("dark", "Dark"),
    RETRO("retro", "Retro");

    private final String directoryName;
    private final String displayName;

    Skin(String directoryName, String displayName) {
        this.directoryName = directoryName;
        this.displayName = displayName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


