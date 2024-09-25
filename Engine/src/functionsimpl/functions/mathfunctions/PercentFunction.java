package functionsimpl.functions.mathfunctions;

import api.Expression;
import api.Function;

import java.util.List;

public class PercentFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("PERCENT function requires exactly two arguments.");
        }

        // Evaluate the first argument (part)
        Object partValue = arguments.get(0).evaluate();
        // Evaluate the second argument (whole)
        Object wholeValue = arguments.get(1).evaluate();

        // Check if both arguments are numbers
        if (!(partValue instanceof Number) || !(wholeValue instanceof Number)) {
            return "NaN"; // Return "NaN" if either argument is not a number
        }

        double part = ((Number) partValue).doubleValue();
        double whole = ((Number) wholeValue).doubleValue();

        // Return the calculated percentage
        return (part * whole) / 100;
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
