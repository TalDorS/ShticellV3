//package servlets.getservlets;
//
//
//import api.Engine;
//import api.Expression;
//import exceptions.engineexceptions.CircularReferenceException;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import utils.ServletUtils;
//
//import java.io.IOException;
//
//@WebServlet("/checkCircularReferences")
//public class CheckCircularReferenceServlet extends HttpServlet {
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json;charset=UTF-8");
//
//        // Retrieve parameters from the request
//        String userName = request.getParameter("userName");
//        String spreadsheetName = request.getParameter("spreadsheetName");
//        String cellId = request.getParameter("cellId");
//        String newExpressionStr = request.getParameter("newExpression");
//        System.out.println( "userName: " + userName + " spreadsheetName: " + spreadsheetName + " cellId: " + cellId + " newExpression: " + newExpressionStr);
//
//        // Ensure all parameters are present
//        if (userName == null || spreadsheetName == null || cellId == null || newExpressionStr == null) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getWriter().write("Invalid input parameters.");
//            return;
//        }
//
//        Engine engine = ServletUtils.getEngine(getServletContext());
//
//        try {
//            // Convert the new expression string to an Expression object
//            Expression newExpression = engine.parseExpression(userName, spreadsheetName,newExpressionStr); // Assuming you have a method to parse expressions
//            System.out.println("newExpression: " + newExpression);
//            // Call the engine method to check for circular references
//            engine.checkForCircularReferences(userName, spreadsheetName, cellId, newExpression);
//
//            // If no circular references are found, respond with success
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().write("No circular references found.");
//        } catch (CircularReferenceException e) {
//            response.setStatus(HttpServletResponse.SC_CONFLICT);
//            response.getWriter().write("Circular reference detected: " + e.getMessage());
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("Error checking for circular references: " + e.getMessage());
//            System.err.println("Error checking for circular references: " + e.getMessage());
//        }
//    }
//
//}
