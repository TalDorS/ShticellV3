package exceptions.uiexceptions;

public class ArgumentOutOfBoundsException extends Exception {
    public ArgumentOutOfBoundsException(String message) {
        super(message);
    }

    public ArgumentOutOfBoundsException(int value, int minValue, int maxValue) {
        super("Invalid input: " + value + ". Please enter a number between " + minValue + " and " + maxValue + ".");
    }
}