package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;

import java.util.List;

public class BiggerFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("BIGGER function expects 2 arguments");
        }

        Object arg1 = args.get(0).evaluate();
        Object arg2 = args.get(1).evaluate();

        if (!(arg1 instanceof Number) || !(arg2 instanceof Number)) {
            return "UNKNOWN";
        }

        return ((Number) arg1).doubleValue() >= ((Number) arg2).doubleValue();
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
