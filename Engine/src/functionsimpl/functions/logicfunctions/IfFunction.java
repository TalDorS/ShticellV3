package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;
import functionsimpl.FunctionUtils;

import java.util.List;

public class IfFunction implements Function {
    private final int expectedArguments = 3;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("IF function expects 3 arguments");
        }

        Object condition = args.get(0).evaluate();

        if (!FunctionUtils.isValidValue(condition) || !(condition instanceof Boolean)) {
            return "UNKNOWN";
        }
        else if ((Boolean)condition) {
            return args.get(1).evaluate();
        } else {
            return args.get(2).evaluate();
        }
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
