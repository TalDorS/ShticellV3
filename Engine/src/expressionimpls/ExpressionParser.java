package expressionimpls;

import api.Expression;
import api.Function;
import api.Range;
import functionsimpl.FunctionFactory;
import exceptions.engineexceptions.InvalidExpressionException;
import ranges.RangesManager;
import spreadsheet.Spreadsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// This class is responsible for parsing a string input into an Expression object.
public class ExpressionParser {

    // Parse the input string into an Expression object
    public static Expression parse(String input, Supplier<Spreadsheet> spreadsheetSupplier, RangesManager rangesManager) throws InvalidExpressionException {
        // Check if input is a boolean
        if (input.equalsIgnoreCase("TRUE") || input.equalsIgnoreCase("FALSE")) {
            return new LiteralExpression(Boolean.parseBoolean(input));
        }

        // Check if it's a function expression
        if (isFunctionExpression(input)) {
            return parseFunction(input, spreadsheetSupplier, rangesManager);
        }

        // Check if it's a numeric literal
        try {
            double value = Double.parseDouble(input);
            return new LiteralExpression(value);
        } catch (NumberFormatException e) {
            // Not a number, move on to next check
        }

        // If none of the above, treat it as a normal string literal
        return new LiteralExpression(input);
    }

    // Parse a function expression like {PLUS,5,6}
    private static Expression parseFunction(String input, Supplier<Spreadsheet> spreadsheetSupplier, RangesManager rangesManager) throws InvalidExpressionException {
        // Remove the curly braces
        String innerContent = input.substring(1, input.length() - 1);

        // Find the first comma not inside a nested function, to separate the function name from the arguments
        int firstCommaIndex = findCommaOutsideBraces(innerContent);
        if (firstCommaIndex == -1) {
            throw new InvalidExpressionException("Invalid function expression: " + input);
        }

        // Extract the function name
        String functionName = innerContent.substring(0, firstCommaIndex).toUpperCase();

        // Parse the arguments (if any)
        String argsContent = innerContent.substring(firstCommaIndex + 1).toUpperCase();
        List<Expression> arguments = parseArguments(argsContent, spreadsheetSupplier, rangesManager, functionName);

        // Create the appropriate function expression
        Function function = FunctionFactory.getFunction(functionName);

        if (function == null) {
            throw new InvalidExpressionException("Unknown function: " + functionName);
        }

        return new FunctionExpression(functionName, arguments, function);
    }

    // Parse arguments for the function, handling nested functions
    private static List<Expression> parseArguments(String argsContent, Supplier<Spreadsheet> spreadsheetSupplier, RangesManager rangesManager, String functionName) throws InvalidExpressionException {
        List<Expression> arguments = new ArrayList<>();
        int start = 0;
        int braceDepth = 0;

        for (int i = 0; i < argsContent.length(); i++) {
            char c = argsContent.charAt(i);
            if (c == '{') {
                braceDepth++;
            } else if (c == '}') {
                braceDepth--;
            } else if (c == ',' && braceDepth == 0) {
                String arg = argsContent.substring(start, i);
                // If the function is REF, check if the argument is a valid cell reference
                if (functionName.equals("REF") && isValidCellReference(arg)) {
                    arguments.add(new ReferenceExpression(arg, spreadsheetSupplier));
                } else if (rangesManager.getRange(arg) != null) {
                    Range range = rangesManager.getRange(arg);
                    arguments.add(new RangeExpression(arg, range, spreadsheetSupplier));
                } else {
                    arguments.add(parse(arg, spreadsheetSupplier, rangesManager));
                }
                start = i + 1;
            }
        }

        // Add the last argument after the loop
        String arg = argsContent.substring(start);
        if (functionName.equals("REF") && isValidCellReference(arg)) {
            arguments.add(new ReferenceExpression(arg, spreadsheetSupplier));
        } else if (rangesManager.getRange(arg) != null) { // Check if it is a range
            Range range = rangesManager.getRange(arg);
            arguments.add(new RangeExpression(arg, range, spreadsheetSupplier));
        } else {
            arguments.add(parse(arg, spreadsheetSupplier, rangesManager));
        }

        return arguments;
    }

    // Find the first comma that is outside any braces
    private static int findCommaOutsideBraces(String input) {
        int braceDepth = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                braceDepth++;
            } else if (c == '}') {
                braceDepth--;
            } else if (c == ',' && braceDepth == 0) {
                return i;
            }
        }

        return -1; // No suitable comma found
    }

    // Check if the input is a valid cell reference
    private static boolean isValidCellReference(String input) {
        return input.matches("^[A-Z]+[0-9]+$");
    }

    // Check if the input is a function expression
    private static boolean isFunctionExpression(String input) {
        return input.startsWith("{") && input.endsWith("}");
    }
}
