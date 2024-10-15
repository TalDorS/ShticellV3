package versions.permissions;

import dto.PermissionRequestDTO;
import enums.PermissionStatus;
import enums.PermissionType;

import java.util.*;


public class PermissionsManager {
    private final String owner;
    private final Map<String, PermissionType> permissions;  // Maps a username to their permission type
    private final List<PermissionRequestDTO> requestHistory;  // Holds all permission requests (including duplicates)

    // Constructor
    public PermissionsManager(String owner) {
        this.owner = owner;
        this.permissions = new HashMap<>();
        this.requestHistory = new ArrayList<>();

        // By default, the owner is added to the permissions map with OWNER permission
        permissions.put(owner, PermissionType.OWNER);
    }

    // Getter for owner
    public String getOwner() {
        return owner;
    }

    // Getter for permissions map (thread-safe)
    public synchronized Map<String, PermissionType> getPermissions() {
        return new HashMap<>(permissions);  // Return a copy of the permissions map for safety
    }

    // Getter for request history (thread-safe)
    public synchronized List<PermissionRequestDTO> getRequestHistory() {
        return new ArrayList<>(requestHistory);  // Return a copy of the request history
    }

    // Method to submit a new permission request
    public synchronized void askForPermission(String username, PermissionType permissionType) {
        PermissionRequestDTO newRequest = new PermissionRequestDTO(username, permissionType, PermissionStatus.PENDING);
        requestHistory.add(newRequest);  // Add the request to the history
    }

    public synchronized void handlePermissionRequest(String applicantName, String handlerName, int requestNumber, PermissionStatus status, PermissionType requestedPermission) {
        // Check if the handler is the owner
        if (!owner.equals(handlerName)) {
            throw new IllegalStateException("Only the owner can handle permission requests.");
        }

        // Validate if the request exists in the requestHistory by its number
        if (requestNumber < 0 || requestNumber >= requestHistory.size()) {
            throw new IllegalArgumentException("Invalid request number.");
        }

        // Get the request from the requestHistory
        PermissionRequestDTO request = requestHistory.get(requestNumber);

        // Check if the request corresponds to the applicant
        if (!request.getUsername().equals(applicantName)) {
            throw new IllegalArgumentException("Applicant name does not match the request.");
        }

        // Handle the permission status update
        if (status == PermissionStatus.APPROVED) {
            // Insert or update the user in the permissions map with the approved permission type
            permissions.put(applicantName, requestedPermission);

            // Update the request status to APPROVED in the request history
            request.setStatus(PermissionStatus.APPROVED);
        } else if (status == PermissionStatus.REJECTED) {
            // Only update the request status to REJECTED in the request history
            request.setStatus(PermissionStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Invalid permission status.");
        }
    }

    // Helper method to check if a user has a specific permission
    public synchronized PermissionType getUserPermission(String username) {
        return permissions.getOrDefault(username, PermissionType.NONE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionsManager that = (PermissionsManager) o;
        return Objects.equals(owner, that.owner) && Objects.equals(permissions, that.permissions) && Objects.equals(requestHistory, that.requestHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, permissions, requestHistory);
    }
}

