package exceptions.engineexceptions;

public class CellUpdateException extends Exception {
    public CellUpdateException(String message) {
        super(message);
    }
    public CellUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
