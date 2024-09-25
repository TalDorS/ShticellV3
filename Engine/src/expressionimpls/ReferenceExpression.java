package expressionimpls;

import api.Expression;
import cells.Cell;
import spreadsheet.Spreadsheet;

import java.io.Serializable;
import java.util.function.Supplier;

// Represent a reference to a cell in the spreadsheet
public class ReferenceExpression implements Expression, Serializable {
    private final String cellId;
    private transient Supplier<Spreadsheet> spreadsheetSupplier;

    public ReferenceExpression(String cellId, Supplier<Spreadsheet> spreadsheetSupplier) {
        this.cellId = cellId.toUpperCase();
        this.spreadsheetSupplier = spreadsheetSupplier;
    }

    public void setSpreadsheetSupplier(Supplier<Spreadsheet> supplier) {
        spreadsheetSupplier = supplier;
    }

    @Override
    // Evaluate the reference expression
    public Object evaluate() {
        // Get the latest Spreadsheet instance from the supplier
        Spreadsheet spreadsheet = spreadsheetSupplier.get();

        // Retrieve the cell from the spreadsheet
        Cell cell = spreadsheet.getCellById(cellId);

        // Return the effective value of the cell or "!UNDEFINED!" if the cell doesn't exist
        if (cell != null) {
            return cell.getEffectiveValue();
        } else {
            return "!UNDEFINED!";
        }
    }

    public String getCellId() {
        return cellId;
    }
}