package servlets.postservlets;

import api.Engine;

import exceptions.*;
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
        String spreadsheetName = request.getParameter("spreadsheetName");
        String cellId = request.getParameter("cellId");
        String newValue = request.getParameter("newValue");
        String dynamic = request.getParameter("isDynamicAnalysis");
        Boolean isDynamicAnalysis = Boolean.parseBoolean(dynamic);

        Engine engine = ServletUtils.getEngine(getServletContext());

        synchronized (engine) {
            try {
                // Call your engine method to update the cell
                engine.updateCellValue(userName, spreadsheetName, cellId, newValue, isDynamicAnalysis);
                // If successful, return a success message
                response.setStatus(HttpServletResponse.SC_OK);

            } catch(UserNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("User not found.");
            } catch(SpreadsheetNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Spreadsheet not found.");
            }
            catch(CircularReferenceException e) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write(e.getMessage());
            } catch(InvalidExpressionException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(e.getMessage());
            } catch(CellUpdateException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write( e.getMessage());
            }
            catch (Exception e) {
                // Handle exceptions such as CellUpdateException, InvalidExpressionException, etc.
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(e.getMessage());
            }
        }

    }
}
