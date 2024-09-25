package exceptions.uiexceptions;

public class MainControllerNotInitializedException extends RuntimeException {
    public MainControllerNotInitializedException(String message) {
        super(message);
    }
}