package menuwindow.center.permissionstable.models;

import enums.PermissionStatus;
import enums.PermissionType;

public class PermissionDetails {
    private final String username;
    private final PermissionType permissionType;
    private PermissionStatus permissionStatus;

    public PermissionDetails(String username, PermissionType permissionType, PermissionStatus permissionStatus) {
        this.username = username;
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
}
