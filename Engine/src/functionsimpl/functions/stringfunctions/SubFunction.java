package functionsimpl.functions.stringfunctions;

import api.Expression;
import api.Function;
import expressionimpls.ReferenceExpression;
import functionsimpl.FunctionUtils;

import java.io.Serializable;
import java.util.List;

// Function to get a substring of a string
public class SubFunction implements Function, Serializable {
    private final int expectedArguments = 3;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("SUB function requires exactly 3 arguments.");
        }

        Object source = arguments.get(0).evaluate();
        Object startIndexObj = arguments.get(1).evaluate();
        Object endIndexObj = arguments.get(2).evaluate();

        if (source instanceof String && startIndexObj instanceof Number && endIndexObj instanceof Number) {
            String str = (String) source;
            int startIndex = ((Number) startIndexObj).intValue();
            int endIndex = ((Number) endIndexObj).intValue();

            if (!FunctionUtils.isValidValue(str)) {
                return "!UNDEFINED!";
            }
            if (startIndex < 0 || endIndex >= str.length() || startIndex > endIndex) {
                return "!UNDEFINED!";
            }

            return str.substring(startIndex, endIndex + 1);
        } else {
            return "!UNDEFINED!";
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
