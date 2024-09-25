package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;

import java.util.List;

public class AndFunction implements Function {
    private final int expectedArguments = 2;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("AND function expects 2 arguments");
        }

        Object exp1 = args.get(0).evaluate();
        Object exp2 = args.get(1).evaluate();

        if (!(exp1 instanceof Boolean) || !(exp2 instanceof Boolean)) {
            return "UNKNOWN";
        }

        return (Boolean) exp1 && (Boolean) exp2;
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
