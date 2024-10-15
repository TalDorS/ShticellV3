package dto;

import enums.PermissionStatus;
import enums.PermissionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class is for creating the permissions manager data transfer object
public class PermissionsManagerDTO {
    private String owner;  // The owner of the spreadsheet
    private final Map<String, PermissionType> permissions;  // Maps usernames to permission types
    private final List<PermissionRequestDTO> requestHistory;  // Holds all the permission requests

    // Default constructor
    public PermissionsManagerDTO() {
        this.permissions = new HashMap<>();
        this.requestHistory = new ArrayList<>();
    }

    // Constructor to initialize from the PermissionsManager entity
    public PermissionsManagerDTO(String owner, Map<String, PermissionType> permissions, List<PermissionRequestDTO> requestHistory) {
        this.owner = owner;
        this.permissions = new HashMap<>(permissions);
        this.requestHistory = new ArrayList<>(requestHistory);
    }

    // Getters and Setters
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, PermissionType> getPermissions() {
        return permissions;
    }

    public List<PermissionRequestDTO> getRequestHistory() {
        return requestHistory;
    }

    // Method to add a permission request (useful when converting from the PermissionsManager class)
    public void addPermissionRequest(PermissionRequestDTO request) {
        requestHistory.add(request);
    }

    // Method to add/update a permission
    public void addPermission(String username, PermissionType permissionType) {
        permissions.put(username, permissionType);
    }
}

