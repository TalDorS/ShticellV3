package servlets.postservlets;

import api.Expression;
import api.Function;
import api.Range;
import com.google.gson.*;
import expressionimpls.FunctionExpression;
import expressionimpls.LiteralExpression;
import expressionimpls.RangeExpression;
import expressionimpls.ReferenceExpression;
import functionsimpl.FunctionFactory;
import ranges.RangeImpl;
import spreadsheet.Spreadsheet;
import com.google.gson.TypeAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExpressionAdapter extends TypeAdapter<Expression> {
    private Gson gson = new Gson();

    @Override
    public void write(com.google.gson.stream.JsonWriter out, Expression value) throws IOException {
        JsonObject jsonObject = new JsonObject();
        // Serialize the type of the Expression
        jsonObject.addProperty("type", value.getClass().getSimpleName());

        // Serialize specific fields based on the type of Expression
        if (value instanceof LiteralExpression) {
            Object literalValue = ((LiteralExpression) value).evaluate();
            // Check the type of the literal value and add it appropriately
            if (literalValue instanceof Number) {
                jsonObject.addProperty("value", (Number) literalValue); // Add as Number
            } else if (literalValue instanceof Boolean) {
                jsonObject.addProperty("value", (Boolean) literalValue); // Add as Boolean
            } else {
                jsonObject.addProperty("value", (String) literalValue); // Add as String
            }
        }else if (value instanceof FunctionExpression) {
            FunctionExpression funcExpr = (FunctionExpression) value;
            jsonObject.addProperty("functionName", funcExpr.getFunctionName());
            JsonArray argsArray = new JsonArray();
            for (Expression arg : funcExpr.getArguments()) {
                // Recursively serialize the arguments
                JsonElement argJson = gson.toJsonTree(arg, Expression.class);
                argsArray.add(argJson);
            }
            jsonObject.add("arguments", argsArray);
        } else if (value instanceof ReferenceExpression) {
            jsonObject.addProperty("cellId", ((ReferenceExpression) value).getCellId());
        } else if (value instanceof RangeExpression) {
            RangeExpression rangeExpr = (RangeExpression) value;
            jsonObject.addProperty("rangeName", rangeExpr.getRangeName());

            // Serialize the range details
            Range range = rangeExpr.getRange();
            JsonObject rangeObject = new JsonObject();
            rangeObject.addProperty("startCell", range.getStartCell());
            rangeObject.addProperty("endCell", range.getEndCell());
            rangeObject.addProperty("name", range.getName());
            jsonObject.add("range", rangeObject);
        }
        gson.toJson(jsonObject, out);
    }

    @Override
    public Expression read(com.google.gson.stream.JsonReader in) throws IOException {
        JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
        return readFromJsonObject(jsonObject);  // Separate the logic for reading a JsonObject
    }

    // Helper method to handle recursive parsing
    private Expression readFromJsonObject(JsonObject jsonObject) throws IOException {
        String type = jsonObject.get("type").getAsString();

        switch (type) {
            case "LiteralExpression":
                Object literalValue = jsonObject.get("value").getAsString();
                return new LiteralExpression(literalValue);

            case "FunctionExpression":
                String functionName = jsonObject.get("functionName").getAsString();
                List<Expression> arguments = new ArrayList<>();
                for (JsonElement argElement : jsonObject.get("arguments").getAsJsonArray()) {
                    arguments.add(readFromJsonObject(argElement.getAsJsonObject()));  // Recursively call to read arguments
                }
                Function function = FunctionFactory.getFunction(functionName);
                return new FunctionExpression(functionName, arguments, function);

            case "ReferenceExpression":
                String cellId = jsonObject.get("cellId").getAsString();
                Supplier<Spreadsheet> spreadsheetSupplier = () -> null;  // Placeholder
                return new ReferenceExpression(cellId, spreadsheetSupplier);

            case "RangeExpression":
                String rangeName = jsonObject.get("rangeName").getAsString();
                // Extract the range object
                JsonObject rangeObject = jsonObject.getAsJsonObject("range");
                String rangeStartCell = rangeObject.get("startCell").getAsString();
                String rangeEndCell = rangeObject.get("endCell").getAsString();
                String rangeObjName = rangeObject.get("name").getAsString();

                // Construct the RangeImpl object
                RangeImpl range = new RangeImpl(rangeObjName, rangeStartCell, rangeEndCell);

                Supplier<Spreadsheet> rangeSupplier = () -> null;  // Placeholder
                return new RangeExpression(rangeName,range, rangeSupplier);

            default:
                throw new JsonParseException("Unknown expression type: " + type);
        }
    }
}