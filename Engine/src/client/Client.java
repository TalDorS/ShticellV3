package client;

import versions.VersionsManager;

import java.util.HashMap;
import java.util.Map;

public class Client {
    private String name; // Client's name
    private Map<String, VersionsManager> cachedFilesVersions; // Maps file names (filepath)to their versions manager

    public Client(String name) {
        this.name = name;
        this.cachedFilesVersions = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, VersionsManager> getCachedFilesVersions() {
        return cachedFilesVersions;
    }

    public void addCachedFile(String filePath, VersionsManager versionsManager) {
        cachedFilesVersions.put(filePath, versionsManager);
    }

    public void removeCachedFile(String filePath) {
        cachedFilesVersions.remove(filePath);
    }

}

