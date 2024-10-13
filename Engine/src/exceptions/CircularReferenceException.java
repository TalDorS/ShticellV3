package exceptions;

public class CircularReferenceException extends Exception {
    public CircularReferenceException(String message) {
        super(message);
    }
}
