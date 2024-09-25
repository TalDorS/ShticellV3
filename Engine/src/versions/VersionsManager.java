package versions;

import cells.Cell;
import spreadsheet.Spreadsheet;

import java.io.Serializable;
import java.util.*;

public class VersionsManager implements Serializable {
    private final Map<Integer, Version> versions;
    private int currentVersionNumber;

    public VersionsManager() {
        this.versions = new HashMap<>();
        this.currentVersionNumber = 0;
    }

    public void clearVersions() {
        versions.clear();
        currentVersionNumber = 0;
    }

    public Spreadsheet getCurrentSpreadsheet() {
        if (currentVersionNumber == 0) {
            return null;
        }

        return versions.get(currentVersionNumber).getSpreadsheet();
    }

    public Spreadsheet getSpreadsheetByVersion(int versionNumber) throws IndexOutOfBoundsException {
        if (versionNumber <= 0 || versionNumber > currentVersionNumber) {
            throw new IndexOutOfBoundsException("The version number is invalid");
        }
        return versions.get(versionNumber).getSpreadsheet();
    }

    public void deleteLatestVersion() {
        if (currentVersionNumber == 0) {
            throw new IllegalStateException("No versions available to delete.");
        }

        // Remove the latest version from the map
        versions.remove(currentVersionNumber);

        // Decrement the currentVersion to point to the previous version
        currentVersionNumber--;

        // If all versions were removed, reset the version number to 0
        if (currentVersionNumber == 0) {
            clearVersions();
        }
    }

    public void saveNewVersion(String cellId, Spreadsheet spreadsheet) {
        int numOfCellsChanged = getNumOfCellsChanged(spreadsheet, cellId);
        Version newVersion = new Version(currentVersionNumber + 1, numOfCellsChanged, spreadsheet);

        versions.put(currentVersionNumber + 1, newVersion);
        currentVersionNumber++;
    }

    public Map<Integer, Version> getVersions() {
        return versions;
    }

    public int getCurrentVersion() {
        return currentVersionNumber;
    }

    public void setCurrentVersionNumber(int currentVersionNumber) {
        this.currentVersionNumber = currentVersionNumber;
    }

    // Helper method to get the number of cells that depend on a cell
    private int getNumOfCellsChanged(Spreadsheet currentSpreadsheet, String cellId) {
        Stack<String> stack = new Stack<>();    // Create a stack for the calculations
        Set<String> visited = new HashSet<>();  // Create a list for those already visited

        stack.push(cellId);    // Add first cell to stack
        visited.add(cellId);   // Add first cell to visited

        int totalNumOfDependants  = 1;     // Including the cell we change

        while (!stack.isEmpty()) {
            String currentCellId = stack.pop();
            Cell currentCell = currentSpreadsheet.getCells().get(currentCellId);
            if (currentCell == null) {
                continue;
            }

            // Iterate over the cells that depend on the current cell
            for (String dependantId : currentCell.getDependsOnMe().keySet()) {
                if (!visited.contains(dependantId)) {   // If we haven't visited this cell yet
                    stack.push(dependantId);            // Add it to the stack for processing
                    visited.add(dependantId);           // Mark it as visited
                    totalNumOfDependants++;             // Increment the count
                }
            }
        }

        return totalNumOfDependants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionsManager that = (VersionsManager) o;
        return currentVersionNumber == that.currentVersionNumber && Objects.equals(versions, that.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versions, currentVersionNumber);
    }
}
