package exceptions.engineexceptions;

public class SpreadsheetLoadingException  extends Exception {
    public SpreadsheetLoadingException(String message) {
        super(message);
    }

    public SpreadsheetLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}