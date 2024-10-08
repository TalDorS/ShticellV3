package dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// This class is for creating the cell data transfer object
public class CellDTO {
    private String originalValue;
    private Object effectiveValue;
    private int lastUpdatedVersion;
    private String lastUpdatedBy; // New field for storing the username of the last updater
    private final List<String> dependsOnThemIds; // Cell IDs this cell depends on
    private final List<String> dependsOnMeIds; // Cell IDs that depend on this cell


    public CellDTO(String originalValue, Object effectiveValue, int lastUpdatedVersion,String lastUpdatedBy, List<String> dependsOnThemIds, List<String> dependsOnMeIds) {
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.lastUpdatedVersion = lastUpdatedVersion;
        this.lastUpdatedBy = lastUpdatedBy;
        this.dependsOnThemIds = dependsOnThemIds;
        this.dependsOnMeIds = dependsOnMeIds;
    }


    // Getters and Setters
    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public Object getEffectiveValue() {
        return effectiveValue;
    }

    public void setEffectiveValue(Object effectiveValue) {
        this.effectiveValue = effectiveValue;
    }

    public int getLastUpdatedVersion() {
        return lastUpdatedVersion;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedVersion(int lastUpdatedVersion) {
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    public int getNumOfDependOnMeIds() {return dependsOnMeIds.size();}

    public int getNumOfDependsOnThemIds() {return dependsOnThemIds.size();}

    public List<String> getDependsOnThemIds() {
        return dependsOnThemIds;
    }

    public List<String> getDependsOnMeIds() {
        return dependsOnMeIds;
    }

    // Override toString to show only the keys (IDs) in dependsOnThem and dependsOnMe
    @Override
    public String toString() {
        return  "Original Value = " + originalValue + "\n" +
                "Effective Value = " + effectiveValue + "\n" +
                "Last Update Version = " + lastUpdatedVersion + "\n" +
                "Cells that this cell depends on = " +  listToString(dependsOnThemIds) + "\n" +
                "Cells that depend on this cell = " + listToString(dependsOnMeIds)  + "\n";
    }

    // Helper method to convert the list of IDs to a string
    private String listToString(List<String> list) {
        return list.stream().collect(Collectors.joining(", ", "{", "}"));
    }
}
