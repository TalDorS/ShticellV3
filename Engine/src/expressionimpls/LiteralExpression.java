package expressionimpls;
import api.Expression;

import java.io.Serializable;

// Represent simple values like numbers, strings, and booleans.
public class LiteralExpression implements Expression, Serializable {
    private final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate() {
        return value;
    }
}
