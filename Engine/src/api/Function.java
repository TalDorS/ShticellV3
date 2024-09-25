package api;

import java.util.List;

// The Function interface is the interface for the function objects
public interface Function {
    Object apply(List<Expression> arguments);
    int getNumberOfArguments();
}
