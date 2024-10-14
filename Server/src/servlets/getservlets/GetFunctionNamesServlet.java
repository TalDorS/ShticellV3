package servlets.getservlets;

import com.google.gson.Gson;
import enums.FunctionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/get-function-names")
public class GetFunctionNamesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> functionNames = new ArrayList<>();

        // Get the function names from the FunctionType enum
        for (FunctionType functionType : FunctionType.values()) {
            functionNames.add(functionType.name());
        }

        // Convert the list of function names to JSON using Gson
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(functionNames);

        // Set the response type and encoding, then send the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
