package dto;

import java.util.Map;
import java.util.stream.Collectors;

// This class is for creating the cell data transfer object
public class CellDTO {
    private String originalValue;
    private Object effectiveValue;
    private int lastUpdatedVersion;
    private final Map<String, CellDTO> dependsOnThem; // Cells this cell depends on
    private final Map<String, CellDTO> dependsOnMe; // Cells that depend on this cell

    public CellDTO(String originalValue, Object effectiveValue, int lastUpdatedVersion, Map<String, CellDTO> dependsOnThem, Map<String, CellDTO> dependsOnMe) {
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.lastUpdatedVersion = lastUpdatedVersion;
        this.dependsOnThem = dependsOnThem;
        this.dependsOnMe = dependsOnMe;
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

    public void setLastUpdatedVersion(int lastUpdatedVersion) {
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    public int getNumOfDependOnMe() {return dependsOnMe.size();}

    public int getNumOfDependsOnThem() {return dependsOnThem.size();}

    public Map<String, CellDTO> getDependsOnThem() {
        return dependsOnThem;
    }

    public Map<String, CellDTO> getDependsOnMe() {
        return dependsOnMe;
    }

    // Override toString to show only the keys (IDs) in dependsOnThem and dependsOnMe
    @Override
    public String toString() {
        return  "Original Value = " + originalValue + "\n" +
                "Effective Value = " + effectiveValue + "\n" +
                "Last Update Version = " + lastUpdatedVersion + "\n" +
                "Cells that this cell depends on = " + mapKeysToString(dependsOnThem) + "\n" +
                "Cells that depend on this cell = " + mapKeysToString(dependsOnMe) + "\n";
    }

    // Helper method to convert the keys of a map to a string
    private String mapKeysToString(Map<String, CellDTO> map) {
        return map.keySet().stream()
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
