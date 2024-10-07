package servlets;

import api.Engine;

import com.google.gson.Gson;
import engineimpl.EngineImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.Constants;
import utils.ServletUtils;
import utils.ServletUtils.*;

import utils.SessionUtils;

import java.io.IOException;

import static utils.Constants.USERNAME;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String usernameFromSession = SessionUtils.getUsername(request);
        Engine engine = (EngineImpl) ServletUtils.getEngine(getServletContext());

        if (usernameFromSession == null) { //user is not logged in yet

            String usernameFromParameter = request.getParameter(USERNAME);

            //System.out.println("Login request received with username: " + usernameFromParameter);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session and no username in parameter - not standard situation. it's a conflict
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                //normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                synchronized (this) {
                    if (engine.isUserExist(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter + " already exists.\n Please enter a different username.";

                        // stands for unauthorized as there is already such user with this name
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getOutputStream().print(errorMessage);
                    }
                    else {
                        try {
                            //add the new user to the users list
                            engine.addUser(usernameFromParameter);
                        }
                        catch(Exception e) {
                            // stands for internal server error
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getOutputStream().print("Name already exists in the system.\nPlease enter a different name.");
                        }
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);

                        //redirect the request to the chat room - in order to actually change the URL
                        System.out.println("On login, request URI is: " + request.getRequestURI()); //todo remove
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            }
        } else {
            //user is already logged in
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

}


