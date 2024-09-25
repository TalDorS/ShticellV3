package functionsimpl.functions.mathfunctions;

import api.Expression;
import api.Function;
import api.Range;
import cells.Cell;
import expressionimpls.RangeExpression;

import java.util.List;

public class AverageFunction implements Function {
    private final int expectedArguments = 1;

    @Override
    public Object apply(List<Expression> arguments) {
        if (arguments.size() != expectedArguments) {
            throw new IllegalArgumentException("AVERAGE function requires exactly one argument.");
        }

        // Check if the argument is a RangeExpression
        if (!(arguments.get(0) instanceof RangeExpression)) {
            return "NaN";
        }

        // Get the range and spreadsheet supplier from the RangeExpression
        RangeExpression rangeExpression = (RangeExpression) arguments.get(0);

        // Fetch the range and validate its existence
        if (rangeExpression.getRange() == null) {
            return "NaN"; // Return "NaN" if the range does not exist
        }

        double sum = 0;
        int numericCount = 0;

        // Iterate through the cells in the range and sum the numeric values
        for (Object value : (List<Object>) rangeExpression.evaluate()) {
            if (value instanceof Number) {
                sum += ((Number) value).doubleValue();
                numericCount++;
            }
        }

        // Check if there were any numeric cells
        if (numericCount == 0) {
            return "NaN"; // Return "NaN" if no numeric cells were found
        }

        // Return the calculated average
        return sum / numericCount;
    }

    @Override
    public int getNumberOfArguments() {
        return expectedArguments;
    }
}
