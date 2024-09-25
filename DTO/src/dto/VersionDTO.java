package dto;

// This class is for creating the version data transfer object
public class VersionDTO {
    private final int versionNumber;
    private final int changedCellsCount;
    private final SpreadsheetDTO spreadsheet;

    public VersionDTO(int versionNumber, int changedCellsCount, SpreadsheetDTO spreadsheet) {
        this.versionNumber = versionNumber;
        this.changedCellsCount = changedCellsCount;
        this.spreadsheet = spreadsheet;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public SpreadsheetDTO getSpreadsheet() {
        return spreadsheet;
    }
}
