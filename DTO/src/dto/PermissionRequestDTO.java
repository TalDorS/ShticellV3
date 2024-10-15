package dto;

import enums.PermissionStatus;
import enums.PermissionType;

import java.util.Objects;

// Class to represent a permission request
public class PermissionRequestDTO {
    private final String username;
    private final PermissionType requestedPermission;
    private PermissionStatus status;

    // Constructor
    public PermissionRequestDTO(String username, PermissionType requestedPermission, PermissionStatus status) {
        this.username = username;
        this.requestedPermission = requestedPermission;
        this.status = status;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public PermissionType getRequestedPermission() {
        return requestedPermission;
    }

    public PermissionStatus getStatus() {
        return status;
    }

    public void setStatus(PermissionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionRequestDTO that = (PermissionRequestDTO) o;
        return Objects.equals(username, that.username) && requestedPermission == that.requestedPermission && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, requestedPermission, status);
    }
}
