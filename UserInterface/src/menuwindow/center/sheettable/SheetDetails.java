package menuwindow.center.sheettable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetDetails that = (SheetDetails) o;
        return Objects.equals(sheetName, that.sheetName) && Objects.equals(uploaderName, that.uploaderName) && Objects.equals(sheetSize, that.sheetSize) && Objects.equals(permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetName, uploaderName, sheetSize, permission);
    }
}
