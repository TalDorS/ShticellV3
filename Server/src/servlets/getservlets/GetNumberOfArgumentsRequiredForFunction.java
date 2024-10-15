package servlets.getservlets;

import enums.FunctionType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/get-number-of-arguments")
public class GetNumberOfArgumentsRequiredForFunction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String functionName = request.getParameter("functionName");

        // Example to get the number of arguments based on the function name
        int numberOfArguments = 0;

        for (FunctionType functionType : FunctionType.values()) {
            if (functionType.name().equals(functionName)) {
                numberOfArguments = functionType.getFunction().getNumberOfArguments();
                break;
            }
        }

        // Send the number of arguments as a response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.valueOf(numberOfArguments));
    }
}
