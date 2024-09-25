package functionsimpl.functions.logicfunctions;

import api.Expression;
import api.Function;
import functionsimpl.FunctionUtils;

import java.util.List;

public class NotFunction implements Function {
    private final int expectedArguments = 1;

    @Override
    public Object apply(List<Expression> args) {
        if (args.size() != expectedArguments) {
            throw new IllegalArgumentException("NOT function expects 1 argument");
        }

        Object exp1 = args.get(0).evaluate();

        if (!(FunctionUtils.isValidValue(exp1)) || !(exp1 instanceof Boolean)) {
            return "UNKNOWN";
        }

        return !(Boolean) exp1;
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
