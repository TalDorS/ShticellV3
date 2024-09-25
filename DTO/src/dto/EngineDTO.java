package dto;

import java.util.Collections;
import java.util.Map;

// This class is for creating the engine data transfer object
public class EngineDTO {
    private final Map<Integer, VersionDTO> versions;
    private final int currentVersionNumber;

    public EngineDTO(Map<Integer, VersionDTO> versions, int currentVersion) {
        this.versions = Collections.unmodifiableMap(versions); // Ensure immutability
        this.currentVersionNumber = currentVersion;
    }

    // Getter for versions
    public Map<Integer, VersionDTO> getVersions() {
        return versions;
    }

    // Getter for currentVersion
    public int getCurrentVersionNumber() {
        return currentVersionNumber;
    }

    public SpreadsheetDTO getCurrentSpreadsheet() {
        if (currentVersionNumber == 0) {
            return null;
        }

        return versions.get(currentVersionNumber).getSpreadsheet();
    }

    public SpreadsheetDTO getSpreadsheetByVersion(int versionNumber) throws IndexOutOfBoundsException {

        if (versionNumber <= 0 || versionNumber > currentVersionNumber) {
            throw new IndexOutOfBoundsException("The version number is invalid");
        }

        return versions.get(versionNumber).getSpreadsheet();    //minus 1 due to user compatibility
    }
}