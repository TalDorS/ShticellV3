package servlets.postservlets;

import api.Engine;
import enums.PermissionStatus;
import enums.PermissionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "HandlePermissionRequestServlet", urlPatterns = "/handle-permission-request")
public class HandlePermissionRequestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String applicantName = req.getParameter("applicantName");
        String handlerName = req.getParameter("handlerName");
        String spreadsheetName = req.getParameter("spreadsheetName");
        String permissionStatusStr = req.getParameter("permissionStatus");
        String permissionTypeStr = req.getParameter("permissionType");

        // Convert the parameters to enum types
        PermissionStatus permissionStatus = PermissionStatus.valueOf(permissionStatusStr);
        PermissionType permissionType = PermissionType.valueOf(permissionTypeStr);

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        synchronized (engine) {
            try {
                engine.handlePermissionRequest(applicantName, handlerName, spreadsheetName, permissionStatus, permissionType);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Success");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Error: " + e.getMessage());
            }
        }
    }
}
