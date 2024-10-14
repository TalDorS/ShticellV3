package utils.exceptions;

public class InvalidColumnException extends Exception {
    public InvalidColumnException(String cellId, char column, char maxColumn) {
        super("Invalid column in cell ID: " + cellId + ". The column '" + column + "' is out of bounds. " +
                "Valid columns are from 'A' to '" + maxColumn + "'. Please correct the column.");
    }

    public InvalidColumnException(String cellId) {
        super("Column: " + cellId + " contains non-numerical data.");
    }
}
