package enums;

public enum PermissionType {
    OWNER,  // The user who created the spreadsheet
    READER, // A user with read-only access
    WRITER,  // A user with write access
    NONE // A user with no access, default
}
