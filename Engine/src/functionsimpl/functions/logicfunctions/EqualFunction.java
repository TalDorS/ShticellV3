package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;
import functionsimpl.FunctionUtils;

import java.util.List;

public class EqualFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("EQUAL function expects 2 arguments");
        }

        Object arg1 = args.get(0).evaluate();
        Object arg2 = args.get(1).evaluate();

        if (!FunctionUtils.isValidValue(arg1) || !FunctionUtils.isValidValue(arg2)) {
            return "UNKNOWN";
        }
        if (arg1 == null && arg2 == null) {
            return true;
        }
        else if ((arg1 == null || arg2 == null) || !arg1.getClass().equals(arg2.getClass())) {
            return false;
        }

        // Return true if both are equal, false otherwise
        return arg1.equals(arg2);
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
