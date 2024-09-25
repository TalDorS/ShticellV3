package exceptions.engineexceptions;

public class InvalidCellIdFormatException extends Exception {
    public InvalidCellIdFormatException(String cellId) {
        super("Invalid cell ID format: " + cellId + ". A valid cell ID consists of one uppercase letter " +
                "followed by a positive number (e.g., A1, B2)");
    }

}
