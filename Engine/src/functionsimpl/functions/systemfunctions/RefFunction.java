package functionsimpl.functions.systemfunctions;

import api.Expression;
import api.Function;
import expressionimpls.ReferenceExpression;

import java.io.Serializable;
import java.util.List;

// Function to return the effective value of a cell reference
public class RefFunction implements Function, Serializable {
    private final int expectedArguments = 1;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("REF function requires exactly one argument.");
        }
        if (!(arguments.get(0) instanceof ReferenceExpression)) {
            throw new IllegalArgumentException("REF function requires a reference expression argument (e.g. {REF,A1}).");
        }

        // Evaluate the argument to get the cell's effective value
        Object argValue = arguments.get(0).evaluate();

        if ((argValue instanceof String || argValue instanceof Number || argValue instanceof Boolean)) {
            return argValue; // already evaluated as a string reference
        }

        // If the argument is not a valid cell reference, return undefined
        return "!UNDEFINED!";
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}