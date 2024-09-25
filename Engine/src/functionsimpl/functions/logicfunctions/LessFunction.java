package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;
import functionsimpl.FunctionUtils;

import java.util.List;

public class LessFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("LESS function expects 2 arguments");
        }

        Object arg1 = args.get(0).evaluate();
        Object arg2 = args.get(1).evaluate();

        if (!(FunctionUtils.isValidValue(arg1)) || !(FunctionUtils.isValidValue(arg2)) || !(arg1 instanceof Number) || !(arg2 instanceof Number)) {
            return "UNKNOWN";
        }

        return ((Number) arg1).doubleValue() <= ((Number) arg2).doubleValue();
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
