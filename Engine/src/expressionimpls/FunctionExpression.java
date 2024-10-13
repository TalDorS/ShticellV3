package expressionimpls;

import api.Expression;
import api.Function;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionExpression that = (FunctionExpression) o;
        return Objects.equals(functionName, that.functionName) && Objects.equals(arguments, that.arguments) && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, arguments, function);
    }
}
