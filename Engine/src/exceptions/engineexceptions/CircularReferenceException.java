package exceptions.engineexceptions;

public class CircularReferenceException extends Exception {
    public CircularReferenceException(String message) {
        super(message);
    }
}
