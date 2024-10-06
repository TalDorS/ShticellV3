package versions.permissions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PermissionsManager {
    private final String owner;
    private final Set<String> writers;
    private final Set<String> readers;

    public PermissionsManager(String username) {
        this.owner = username;
        writers = new HashSet<>();
        readers = new HashSet<>();
    }

    public String getOwner() {
        return owner;
    }

    public Set<String> getWriters() {
        return writers;
    }

    public Set<String> getReaders() {
        return readers;
    }

    public void addWriter(String username) {
        writers.add(username);
    }

    public void removeWriter(String username) {
        writers.remove(username);
    }

    public void addReader(String username) {
        readers.add(username);
    }

    public void removeReader(String username) {
        readers.remove(username);
    }

    public String getUserPermission(String username) {
        if (owner.equals(username)) {
            return "OWNER";
        }
        if (writers.contains(username)) {
            return "WRITER";
        }
        if (readers.contains(username)) {
            return "READER";
        }

        return "NO PERMISSION";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionsManager that = (PermissionsManager) o;
        return Objects.equals(writers, that.writers) && Objects.equals(readers, that.readers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(writers, readers);
    }
}
