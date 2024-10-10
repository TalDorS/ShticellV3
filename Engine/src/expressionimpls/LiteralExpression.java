package expressionimpls;
import api.Expression;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralExpression that = (LiteralExpression) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
