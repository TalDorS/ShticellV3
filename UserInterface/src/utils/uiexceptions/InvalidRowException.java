package utils.uiexceptions;

public class InvalidRowException extends Exception {
    public InvalidRowException(String cellId, int row, int maxRow) {
        super("Invalid row in cell ID: " + cellId + ". The row '" + row + "' is out of bounds. " +
                "Valid rows are from 1 to " + maxRow + ". Please correct the row.");
    }
}
