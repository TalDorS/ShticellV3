package servlets.postservlets;

import api.Engine;

import exceptions.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "LoadFileServlet", urlPatterns = "/loadSpreadsheet")
public class LoadFileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // Get user session and the engine

        String usernameFromSession = SessionUtils.getUsername(request);
        Engine engine = ServletUtils.getEngine(getServletContext());

        if (usernameFromSession == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("You must be logged in to load a spreadsheet.");
            return;
        }

        String filePathFromParameter = request.getParameter("filePath");
        if (filePathFromParameter == null || filePathFromParameter.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("File path is missing or empty.");
            return;
        }

        // Normalize the file path
        filePathFromParameter = filePathFromParameter.trim();

        synchronized (this) {
            try {
                // Use engine to load the spreadsheet
                String spreadsheetName = engine.loadSpreadsheet(usernameFromSession, filePathFromParameter);

                // If loadSpreadsheet is successful, respond with the file name
                String successResponse = String.format(spreadsheetName);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(successResponse);

            } catch (SpreadsheetLoadingException e) {
                // Handle case where file already exists
                String errorResponse = String.format(e.getMessage());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write(errorResponse);
            } catch (CellUpdateException | InvalidExpressionException |
                     CircularReferenceException | RangeProcessException e) {
                String errorResponse = String.format(e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(errorResponse);
            } catch (Exception e) {
                String errorResponse = String.format(e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(errorResponse);
            }
        }
    }
}
