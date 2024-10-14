package utils.exceptions;

public class CellNotFoundException extends Exception {
    public CellNotFoundException(String cellId) {
        super("Cell not found: " + cellId + ". The specified cell does not exist in the spreadsheet. " +
                "Please ensure the cell ID is correct and within the spreadsheet's bounds.");
    }
}