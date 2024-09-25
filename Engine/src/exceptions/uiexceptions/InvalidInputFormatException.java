package exceptions.uiexceptions;

public class InvalidInputFormatException extends Exception {
    public InvalidInputFormatException(String input) {
        super("Invalid input format: '" + input + "'. Please enter a valid number.");
    }

    public InvalidInputFormatException(String input, String message) {
        super("Invalid input format: '" + input + "'. " + message);
    }
}