package models;

public class SheetDetails {
    private String sheetName;
    private String uploaderName;
    private String sheetSize;
    private String permission;

    public SheetDetails(String sheetName, String uploaderName, String sheetSize, String permission) {
        this.sheetName = sheetName;
        this.uploaderName = uploaderName;
        this.sheetSize = sheetSize;
        this.permission = permission;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploadername(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getSheetSize() {
        return sheetSize;
    }

    public void setSheetSize(String sheetSize) {
        this.sheetSize = sheetSize;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
