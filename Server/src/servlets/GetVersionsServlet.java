package servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.EngineDTO;
import dto.VersionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/getVersions")
public class GetVersionsServlet extends HttpServlet {

    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
            Map<Integer, VersionDTO> versionMap = engineDTO.getVersions();

            // Convert the map values (VersionDTO) to a list
            List<VersionDTO> versionsList = versionMap.values().stream().collect(Collectors.toList());

            System.out.println("GetVersionsServlet: " + versionsList);
            // Serialize the VersionsDTO to JSON and send it in the response
            String jsonResponse = gson.toJson(versionsList);
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            // Handle exceptions (e.g., user or file not found)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write(e.getMessage());
        }
    }
}
