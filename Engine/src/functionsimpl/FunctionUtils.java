package functionsimpl;

public class FunctionUtils {
    public static boolean isValidValue(Object arg) {
        return !("!UNDEFINED!".equals(arg) || "NaN".equals(arg) || "UNKNOWN".equals(arg));
    }
}
