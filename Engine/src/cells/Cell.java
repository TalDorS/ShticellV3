package cells;

import api.Expression;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Represents a cell in the spreadsheet
public class Cell implements Serializable {
    private String originalValue;
    private transient Expression expression; // This represents the parsed expression
    private Object effectiveValue; // The result of evaluating the expression
    private Map<String, Cell> dependsOnThem; // Cells this cell depends on
    private Map<String, Cell> dependsOnMe; // Cells that depend on this cell
    private int lastUpdatedVersion; // The version number when the cell was last updated
    private String lastUpdatedBy;

    // Constructor
    public Cell(String originalValue, Expression expression, int lastUpdatedVersion,String lastUpdatedBy) {
        this.originalValue = originalValue;
        this.expression = expression;
        this.dependsOnThem = new HashMap<>();
        this.dependsOnMe = new HashMap<>();
        this.lastUpdatedVersion = lastUpdatedVersion;
        this.lastUpdatedBy = lastUpdatedBy;
        setEffectiveValue();
    }

    // Constructor that accepts all required fields
    public Cell(String originalValue, Object effectiveValue, Expression expression, int lastUpdatedVersion, String lastUpdatedBy) {
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.expression = expression;
        this.lastUpdatedVersion = lastUpdatedVersion;
        this.lastUpdatedBy = lastUpdatedBy;
        this.dependsOnThem = new HashMap<>();
        this.dependsOnMe = new HashMap<>();
    }

    // Deep copy constructor
    public Cell(Cell original) {
        this.originalValue = original.originalValue;
        this.expression = original.expression;
        this.dependsOnThem = new HashMap<>(original.dependsOnThem);
        this.dependsOnMe = new HashMap<>(original.dependsOnMe);
        this.lastUpdatedVersion = original.lastUpdatedVersion;
        this.lastUpdatedBy = original.lastUpdatedBy;
        setEffectiveValue();
    }

    // Empty constructor
    public Cell() {
        this.originalValue = "";  // Initialize with an empty string
        this.expression = null;   // No expression set by default
        this.effectiveValue = ""; // Initialize with an empty string as effective value
        this.dependsOnThem = new HashMap<>(); // Initialize with empty sets
        this.dependsOnMe = new HashMap<>();
        this.lastUpdatedVersion = 0; // Initialize with version 0
        this.lastUpdatedBy = "";
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        // Convert "true" or "false" to uppercase
        if (Objects.equals(originalValue, "true") || Objects.equals(originalValue, "false")) {
            originalValue = originalValue.toUpperCase();
        }

        // Regular expression to detect cell references (e.g., "A1", "B2", etc.)
        String cellReferencePattern = "([A-Za-z]+\\d+)";

        // Check if the original value matches the pattern for a cell reference
        if (originalValue.matches(".*" + cellReferencePattern + ".*")) {
            // Convert cell references to uppercase
            originalValue = originalValue.toUpperCase();
        }

        this.originalValue = originalValue;
    }

    public Object getEffectiveValue() {
        return effectiveValue;
    }

    public void setEffectiveValue() {
        if (expression == null) {
            effectiveValue = originalValue;
            return;
        }

        try {
            Object evaluatedValue = expression.evaluate();

            if (evaluatedValue instanceof Number) {
                double doubleValue = ((Number) evaluatedValue).doubleValue();

                // Check if the double value is an integer
                if (doubleValue == Math.floor(doubleValue)) {
                    effectiveValue = (int) doubleValue; // Cast to int if it is an integer
                } else {
                    doubleValue = ((Number) evaluatedValue).doubleValue();

                    // Format the double value to have a maximum of two decimal places for display purposes
                    effectiveValue = Math.round(doubleValue * 100.0) / 100.0; // Rounds to 2 decimal places
                }
            } else {
                effectiveValue = evaluatedValue;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
        setEffectiveValue();
    }

    public Expression getExpression() {
        return expression;
    }

    public Map<String, Cell> getDependsOnThem() {
        return dependsOnThem;
    }

    public void setDependsOnThem(Map<String, Cell> dependsOnThem) { this.dependsOnThem = new HashMap<>(dependsOnThem); }

    public Map<String, Cell> getDependsOnMe() {
        return dependsOnMe;
    }

    public void setDependsOnMe(Map<String, Cell> dependsOnMe) { this.dependsOnMe = new HashMap<>(dependsOnMe); }

    public void addDependsOnThem(String cellId, Cell newDependsOnThem) {
        dependsOnThem.put(cellId, newDependsOnThem);
    }

    public void addDependsOnMe(String cellId, Cell newDependsOnThem) {
        dependsOnMe.put(cellId, newDependsOnThem);
    }

    public void removeDependsOnThem(String cellId) {
        dependsOnThem.remove(cellId);
    }

    public void removeDependsOnMe(String cellId) {
        dependsOnMe.remove(cellId);
    }

    public int getLastUpdatedVersion() { return lastUpdatedVersion; }

    public String getLastUpdatedBy() { return lastUpdatedBy; }

    public void setLastUpdatedVersion(int lastUpdatedVersion) { this.lastUpdatedVersion = lastUpdatedVersion; }

    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return lastUpdatedVersion == cell.lastUpdatedVersion && Objects.equals(originalValue, cell.originalValue) &&
                Objects.equals(expression, cell.expression) && Objects.equals(effectiveValue, cell.effectiveValue) &&
                Objects.equals(dependsOnThem, cell.dependsOnThem) && Objects.equals(dependsOnMe, cell.dependsOnMe) &&
                Objects.equals(lastUpdatedBy, cell.lastUpdatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalValue, expression, effectiveValue, dependsOnThem, dependsOnMe, lastUpdatedVersion, lastUpdatedBy);
    }
}
