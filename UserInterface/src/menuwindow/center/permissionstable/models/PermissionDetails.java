package menuwindow.center.permissionstable.models;

import enums.PermissionStatus;
import enums.PermissionType;

import java.util.Objects;

public class PermissionDetails {
    private final String username;
    private final PermissionType permissionType;
    private final int permissionNumber;
    private PermissionStatus permissionStatus;

    public PermissionDetails(String username, int permissionNumber, PermissionType permissionType, PermissionStatus permissionStatus) {
        this.username = username;
        this.permissionNumber = permissionNumber;
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
    }

    public String getUsername() {
        return username;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public PermissionStatus getPermissionStatus() {
        return permissionStatus;
    }

    public void setPermissionStatus(PermissionStatus permissionStatus) {
        this.permissionStatus = permissionStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionDetails that = (PermissionDetails) o;
        return permissionNumber == that.permissionNumber && Objects.equals(username, that.username) && permissionType == that.permissionType && permissionStatus == that.permissionStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, permissionType, permissionNumber, permissionStatus);
    }
}
