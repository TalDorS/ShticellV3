package functionsimpl.functions.mathfunctions;

import api.Expression;
import api.Function;
import expressionimpls.FunctionExpression;

import java.io.Serializable;
import java.util.List;

// Function to calculate power given two numbers
public class PowFunction implements Function, Serializable {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("POW function requires exactly 2 arguments.");
        }

        Object arg1 = arguments.get(0).evaluate();
        Object arg2 = arguments.get(1).evaluate();

        if (arg1 instanceof Number && arg2 instanceof Number) {
            double base = ((Number) arg1).doubleValue();
            double exponent = ((Number) arg2).doubleValue();

            // Check if both base and exponent are 0, which is undefined
            if (base == 0 && exponent == 0) {
                return "NaN"; // Returning NaN to indicate the operation is undefined
            }

            return Math.pow(base, exponent);
        } else {
            return "NaN";
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
