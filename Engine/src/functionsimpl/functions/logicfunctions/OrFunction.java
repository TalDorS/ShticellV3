package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;
import functionsimpl.FunctionUtils;

import java.util.List;

public class OrFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("OR function expects 2 arguments");
        }

        Object exp1 = args.get(0).evaluate();
        Object exp2 = args.get(1).evaluate();

        if (!(FunctionUtils.isValidValue(exp1)) || !(FunctionUtils.isValidValue(exp2)) || !(exp1 instanceof Boolean) || !(exp2 instanceof Boolean)) {
            return "UNKNOWN";
        }

        return (Boolean) exp1 || (Boolean) exp2;
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
