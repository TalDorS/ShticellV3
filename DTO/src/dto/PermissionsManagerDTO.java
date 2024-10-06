package dto;

import enums.PermissionStatus;

import java.util.HashMap;
import java.util.Map;

public class PermissionsManagerDTO {
    private String owner;
    private final Map<String, PermissionStatus> writers;
    private final Map<String, PermissionStatus> readers;

    // Default constructor
    public PermissionsManagerDTO() {
        writers = new HashMap<>();
        readers = new HashMap<>();
    }

    // Constructor to initialize from the PermissionsManager entity
    public PermissionsManagerDTO(String owner, Map<String, PermissionStatus> writers, Map<String, PermissionStatus> readers) {
        this.owner = owner;
        this.writers = new HashMap<>(writers);
        this.readers = new HashMap<>(readers);
    }

    // Getters and Setters
    public String getOwner() {
        return owner;
    }

    public Map<String, PermissionStatus> getWriters() {
        return writers;
    }

    public Map<String, PermissionStatus> getReaders() {
        return readers;
    }
}
