package versions;

import spreadsheet.Spreadsheet;

import java.io.Serializable;
import java.util.Objects;

// This class manages and keeps track of the spreadsheet changes
public class Version implements Serializable {
    private int versionNumber;
    private int changedCellsCount;
    private Spreadsheet spreadsheet;

    // Default constructor
    public Version() {
        this.versionNumber = 0;
        this.changedCellsCount = 0;
        this.spreadsheet = new Spreadsheet();
    }

    // Constructor
    public Version(int versionNumber,int numOfCellChanged, Spreadsheet spreadsheetToCopy) {
        this.versionNumber = versionNumber;
        changedCellsCount= numOfCellChanged;
        spreadsheet = new Spreadsheet(spreadsheetToCopy);
        spreadsheet.setVersionNumber(this.versionNumber);
    }

    // Copy constructor
    public Version(Version other) {
        if (other != null) {
            this.versionNumber = other.versionNumber;
            this.changedCellsCount = other.changedCellsCount;
            this.spreadsheet = new Spreadsheet(other.spreadsheet);
        }
    }

    // Deep copy method
    public Version deepCopy() {
        Version copy = new Version();
        copy.versionNumber = this.versionNumber;
        copy.changedCellsCount = this.changedCellsCount;
        copy.spreadsheet = new Spreadsheet(this.spreadsheet);
        return copy;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public void setNumOfCellsChanged(int size) {
        changedCellsCount = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return versionNumber == version.versionNumber && changedCellsCount == version.changedCellsCount && Objects.equals(spreadsheet, version.spreadsheet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionNumber, changedCellsCount, spreadsheet);
    }
}
