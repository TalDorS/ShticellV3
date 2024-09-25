package functionsimpl;

import api.Function;
import enums.FunctionType;

// Factory class to create functions
public class FunctionFactory {

    public static Function getFunction(String functionName) {
        try {
            // Convert the function name to uppercase and get the corresponding enum value
            return FunctionType.valueOf(functionName.toUpperCase()).getFunction();
        } catch (IllegalArgumentException e) {
            // If the function name doesn't match any enum, return null or throw an exception
            return null;
        }
    }
}
