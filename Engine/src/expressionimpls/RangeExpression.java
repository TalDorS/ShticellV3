package expressionimpls;

import api.Expression;
import api.Range;
import cells.Cell;
import spreadsheet.Spreadsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class RangeExpression implements Expression {
    private final String rangeName; // Name of the range
    private final Range range; // The range object itself
    private final Supplier<Spreadsheet> spreadsheetSupplier; // Supplier to get the current spreadsheet

    public RangeExpression(String rangeName, Range range, Supplier<Spreadsheet> spreadsheetSupplier) {
        this.rangeName = rangeName;
        this.range = range;
        this.spreadsheetSupplier = spreadsheetSupplier;
    }

    @Override
    public Object evaluate() {
        List<Object> effectiveValues = new ArrayList<>();
        Spreadsheet spreadsheet = spreadsheetSupplier.get(); // Get the current spreadsheet

        if (spreadsheet == null || range == null) {
            return effectiveValues; // Return an empty list if the spreadsheet or range is null
        }

        // Iterate over all cell IDs in the range
        for (String cellId : range.getCells()) {
            Cell cell = spreadsheet.getCellById(cellId);

            if (cell != null) {
                effectiveValues.add(cell.getEffectiveValue());
            }
        }

        return effectiveValues; // Return the list of effective values of the cells
    }

    public String getRangeName() {
        return rangeName;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeExpression that = (RangeExpression) o;
        return Objects.equals(rangeName, that.rangeName) && Objects.equals(range, that.range) && Objects.equals(spreadsheetSupplier, that.spreadsheetSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeName, range, spreadsheetSupplier);
    }
}
