package versions.permissions;

import enums.PermissionStatus;
import enums.PermissionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PermissionsManager {
    private final String owner;
    private final Map<String, PermissionStatus> writers;
    private final Map<String, PermissionStatus> readers;

    public PermissionsManager(String username) {
        this.owner = username;
        writers = new HashMap<>();
        readers = new HashMap<>();
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, PermissionStatus> getWriters() {
        return writers;
    }

    public Map<String, PermissionStatus> getReaders() {
        return readers;
    }

    public void addWriter(String username) {
        writers.put(username, PermissionStatus.PENDING);
    }

    public void removeWriter(String username) {
        writers.remove(username);
    }

    public void addReader(String username) {
        readers.put(username, PermissionStatus.PENDING);
    }

    public void removeReader(String username) {
        readers.remove(username);
    }

    public void approveWriter(String username) {
        if (writers.containsKey(username)) {
            writers.put(username, PermissionStatus.APPROVED);  // Approve the writer
        }
    }

    public void approveReader(String username) {
        if (readers.containsKey(username)) {
            readers.put(username, PermissionStatus.APPROVED);  // Approve the reader
        }
    }

    public boolean isWriterApproved(String username) {
        return writers.getOrDefault(username, PermissionStatus.PENDING) == PermissionStatus.APPROVED;
    }

    public boolean isReaderApproved(String username) {
        return readers.getOrDefault(username, PermissionStatus.PENDING) == PermissionStatus.APPROVED;
    }

    public PermissionType getUserPermission(String username) {
        if (owner.equals(username)) {
            return PermissionType.OWNER;
        }
        if (writers.containsKey(username) && writers.get(username).equals(PermissionStatus.APPROVED)) {
            return PermissionType.WRITER;
        }
        if (readers.containsKey(username) && readers.get(username).equals(PermissionStatus.APPROVED)) {
            return PermissionType.READER;
        }

        return PermissionType.NONE;
    }

    public void askForPermission(String username, PermissionType permissionType) {
        // Check the permission type and add the user to the appropriate map with status PENDING
        if (permissionType == PermissionType.WRITER) {
            // Add the user as a writer with a PENDING status
            if (!writers.containsKey(username)) {
                addWriter(username);
            }
        } else if (permissionType == PermissionType.READER) {
            // Add the user as a reader with a PENDING status
            if (!readers.containsKey(username)) {
                addReader(username);
            }
        }
    }

    public void handlePermissionRequest(String applicantName, String handlerName, PermissionStatus permissionStatus, PermissionType permissionType) {
        // Check if the handler is the owner
        if (!owner.equals(handlerName)) {
            throw new IllegalStateException("Only the owner can handle permission requests.");
        }

        // Handle the permission request based on the permission type
        if (permissionType == PermissionType.WRITER) {
            // Approve or reject the writer
            if (writers.containsKey(applicantName)) {
                writers.put(applicantName, permissionStatus);
                // If the user is approved as a writer, remove them from the readers map
                if (permissionStatus == PermissionStatus.APPROVED) {
                    readers.remove(applicantName);
                }
            }
        } else if (permissionType == PermissionType.READER) {
            // Approve or reject the reader
            if (readers.containsKey(applicantName)) {
                readers.put(applicantName, permissionStatus);
                // If the user is approved as a reader, remove them from the writers map
                if (permissionStatus == PermissionStatus.APPROVED) {
                    writers.remove(applicantName);
                }
            }
        }
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