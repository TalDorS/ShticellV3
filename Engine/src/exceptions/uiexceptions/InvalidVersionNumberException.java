package exceptions.uiexceptions;

public class InvalidVersionNumberException extends Exception{
    public InvalidVersionNumberException(String message) {
        super(message);
    }

    public InvalidVersionNumberException() {
        super("Invalid version number");
    }
}
