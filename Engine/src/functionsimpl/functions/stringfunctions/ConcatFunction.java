package functionsimpl.functions.stringfunctions;

import api.Expression;
import api.Function;
import expressionimpls.FunctionExpression;

import java.io.Serializable;
import java.util.List;
import functionsimpl.FunctionUtils;

// Function to concatenate two strings
public class ConcatFunction implements Function, Serializable {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("CONCAT function requires exactly 2 arguments.");
        }

        Object arg1 = arguments.get(0).evaluate();
        Object arg2 = arguments.get(1).evaluate();

        if (!(FunctionUtils.isValidValue(arg1) || !(FunctionUtils.isValidValue(arg2)))) {
            return "!UNDEFINED!";
        }
        if (arg1 instanceof String && arg2 instanceof String) {
            return (String) arg1 + (String) arg2;
        } else {
            return "!UNDEFINED!";
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
