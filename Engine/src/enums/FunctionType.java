package enums;

import api.Function;
import functionsimpl.functions.logicfunctions.*;
import functionsimpl.functions.mathfunctions.*;
import functionsimpl.functions.stringfunctions.ConcatFunction;
import functionsimpl.functions.stringfunctions.SubFunction;
import functionsimpl.functions.systemfunctions.RefFunction;

// The FunctionType enum is the enum for the function types for each functionality
public enum FunctionType {
    PLUS(new PlusFunction()),
    MINUS(new MinusFunction()),
    TIMES(new TimesFunction()),
    DIVIDE(new DivideFunction()),
    MOD(new ModuloFunction()),
    POW(new PowFunction()),
    ABS(new AbsFunction()),
    CONCAT(new ConcatFunction()),
    SUB(new SubFunction()),
    REF(new RefFunction()),
    EQUAL(new EqualFunction()),
    NOT(new NotFunction()),
    BIGGER(new BiggerFunction()),
    LESS(new LessFunction()),
    OR(new OrFunction()),
    AND(new AndFunction()),
    IF(new IfFunction()),
    SUM(new SumFunction()),
    AVERAGE(new AverageFunction()),
    PERCENT(new PercentFunction());

    private final Function function;

    FunctionType(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
