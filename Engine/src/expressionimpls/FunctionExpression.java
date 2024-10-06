package expressionimpls;

import api.Expression;
import api.Function;

import java.io.Serializable;
import java.util.List;

// Represent functions with arguments
public class FunctionExpression implements Expression, Serializable {
    private final String functionName;
    private final List<Expression> arguments;
    private final Function function;

    public FunctionExpression(String functionName, List<Expression> arguments, Function function) {
        this.functionName = functionName;
        this.arguments = arguments;
        this.function = function;
    }

    @Override
    // Evaluate the function expression
    public Object evaluate(){
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }

        // Apply the function to its arguments
        return function.apply(arguments);
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public String getFunctionName() {
        return functionName;
    }
}
