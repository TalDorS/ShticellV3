package servlets;

import api.Engine;
import engineimpl.EngineImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebServlet("/updateCellValue")
public class UpdateCellValueServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String userName = request.getParameter("userName");
        String fileName = request.getParameter("fileName");
        String cellId = request.getParameter("cellId");
        String newValue = request.getParameter("newValue");

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Call your engine method to update the cell
            engine.updateCellValue(userName, fileName, cellId, newValue); // Implement this method in your Engine class
            // If successful, return a success message
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            // Handle exceptions such as CellUpdateException, InvalidExpressionException, etc.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        }
    }
}
