package servlets.postservlets;

import api.Engine;
import api.Expression;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.engineexceptions.InvalidExpressionException;
import exceptions.engineexceptions.SpreadsheetNotFoundException;
import exceptions.engineexceptions.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/parseExpression")
public class ParseExpressionServlet extends HttpServlet {

    private final Gson gson;

    public ParseExpressionServlet() {
        // Register the ExpressionAdapter with Gson
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Expression.class, new ExpressionAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String expressionInput = request.getParameter("expression");

        // Ensure all parameters are present
        if (userName == null || spreadsheetName == null || expressionInput == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input parameters.");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Call the engine method to parse the expression
            Expression parsedExpression = engine.parseExpression(userName, spreadsheetName, expressionInput);

            // Serialize the parsed expression to JSON
            String jsonResponse = gson.toJson(parsedExpression);
            // Create a response JSON object
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonResponse);
        } catch (InvalidExpressionException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid expression: " + e.getMessage());
        } catch (UserNotFoundException | SpreadsheetNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User or spreadsheet not found: " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error parsing expression: " + e.getMessage());
        }
    }
}

