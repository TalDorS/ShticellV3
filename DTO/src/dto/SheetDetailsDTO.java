package dto;

import java.util.Objects;

public class SheetDetailsDTO {
    private String sheetName;
    private String uploaderName;
    private String sheetSize;
    private String permission;

    public SheetDetailsDTO(String sheetName, String uploaderName, String sheetSize, String permission) {
        this.sheetName = sheetName;
        this.uploaderName = uploaderName;
        this.sheetSize = sheetSize;
        this.permission = permission;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getSheetSize() {
        return sheetSize;
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
        SheetDetailsDTO that = (SheetDetailsDTO) o;
        return Objects.equals(sheetName, that.sheetName) && Objects.equals(uploaderName, that.uploaderName) && Objects.equals(sheetSize, that.sheetSize) && Objects.equals(permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetName, uploaderName, sheetSize, permission);
    }
}