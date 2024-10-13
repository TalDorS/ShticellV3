package enums;

public enum PermissionStatus {
    PENDING,    // When a request is made and awaiting approval
    APPROVED,   // When the request is approved by the owner
    REJECTED,   // When the request is rejected by the owner
    NONE        // When the user is the owner of the spreadsheet
}
