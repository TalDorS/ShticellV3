package functionsimpl.functions.mathfunctions;

import api.Expression;
import api.Function;
import expressionimpls.FunctionExpression;

import java.io.Serializable;
import java.util.List;

// Function to calculate the absolute value of a number
public class AbsFunction implements Function, Serializable {
    private final int expectedArguments = 1;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("ABS function requires exactly 1 argument.");
        }

        Object arg = arguments.get(0).evaluate();

        if (arg instanceof Number) {
            return Math.abs(((Number) arg).doubleValue());
        } else {
            return "NaN";
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
