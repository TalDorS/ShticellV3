package user;

import versions.VersionsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {
    private String name; // Client's name
   // private Map<String, VersionsManager> cachedFilesVersions; // Maps file names (filepath)to their versions manager

    public User(String name) {
        this.name = name;
        //this.cachedFilesVersions = new HashMap<>();
    }

    public String getUserName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

//    public Map<String, VersionsManager> getCachedFilesVersions() {
//        return cachedFilesVersions;
//    }

//    public void addCachedFile(String filePath, VersionsManager versionsManager) {
//        cachedFilesVersions.put(filePath, versionsManager);
//    }

//    public void removeCachedFile(String filePath) {
//        cachedFilesVersions.remove(filePath);
//    }

}

