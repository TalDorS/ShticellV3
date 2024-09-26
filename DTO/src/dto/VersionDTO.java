package dto;

import java.util.List;

// This class is for creating the version data transfer object
public class VersionDTO {
    private final int versionNumber;
    private final int changedCellsCount;
    private final SpreadsheetDTO spreadsheet;
    private final List<RangeDTO> ranges; // Add a field for ranges


    public VersionDTO(int versionNumber, int changedCellsCount, SpreadsheetDTO spreadsheet, List<RangeDTO> ranges) {
        this.versionNumber = versionNumber;
        this.changedCellsCount = changedCellsCount;
        this.spreadsheet = spreadsheet;
        this.ranges = ranges;
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

    public List<RangeDTO> getRanges() {
        return ranges;
    }
}
