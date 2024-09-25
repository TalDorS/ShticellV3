package functionsimpl.functions.mathfunctions;

import api.Expression;
import api.Function;
import expressionimpls.FunctionExpression;

import java.io.Serializable;
import java.util.List;

// Function to divide two numbers
public class DivideFunction implements Function, Serializable {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("DIVIDE function requires exactly 2 arguments.");
        }

        Object arg1 = arguments.get(0).evaluate();
        Object arg2 = arguments.get(1).evaluate();

        if (arg1 instanceof Number && arg2 instanceof Number) {
            double denominator = ((Number) arg2).doubleValue();

            if (denominator == 0) {
                return "NaN"; // Division by zero results in NaN
            }

            return ((Number) arg1).doubleValue() / denominator;
        } else {
            return "NaN";
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
