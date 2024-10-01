package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import user.UserManager;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

import static utils.Constants.USERNAME;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) { //user is not logged in yet

            String usernameFromParameter = request.getParameter(USERNAME);

            System.out.println("Login request received with username: " + usernameFromParameter);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session and no username in parameter - not standard situation. it's a conflict
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                //normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";

                        // stands for unauthorized as there is already such user with this name
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getOutputStream().print(errorMessage);
                    }
                    else {
                        try {
                            //add the new user to the users list
                            userManager.addUser(usernameFromParameter);
                            //set the username in a session so it will be available on each request
                            //the true parameter means that if a session object does not exists yet
                            //create a new one
                        }
                        catch(Exception e) {
                            // stands for internal server error
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getOutputStream().print("Name already exists in the system.\nPlease enter a different name.");
                        }
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);

                        //redirect the request to the chat room - in order to actually change the URL
                        System.out.println("On login, request URI is: " + request.getRequestURI());
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

//@WebServlet(name = "LoginServlet", urlPatterns = "/login")
//public class LoginServlet extends HttpServlet {
//    private UsersManager usersManager;
//
//    @Override
//    public void init() {
//        // Initialize UsersManager inside init() where ServletConfig is available
//        usersManager = ServletUtils.getUserManager(getServletContext());
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        String username = req.getParameter("username");
//        System.out.println("POST request received with username: " + req.getParameter("username"));
//
//        synchronized (usersManager) {
//            // If username is already logged in, send an error response
//            if (usersManager.isUserExist(username)) {
//                resp.setStatus(HttpServletResponse.SC_CONFLICT);
//                resp.getWriter().write(new Gson().toJson("User is already logged in."));
//            } else {
//                // Add user to the active users list
//                try {
//                    usersManager.addUser(username);
//
//                    // Create session and store user information
//                    HttpSession session = req.getSession(true);
//                    session.setAttribute("username", username);
//
//                    // Set a session cookie
//                    Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
//                    sessionCookie.setHttpOnly(true);
//                    sessionCookie.setMaxAge(30 * 60); // TODO - check if needed TAL
//                    resp.addCookie(sessionCookie);
//
//                    // Success response
//                    resp.setContentType("application/json");
//                    //resp.getWriter().write(new Gson().toJson("Login Successful"));
//                    resp.getWriter().write("Login Successful");
//                } catch (Exception e) {
//                    // Handle user addition failure
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(new Gson().toJson("Failed to log in user."));
//                }
//            }
//        }
//    }

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        resp.getWriter().write("GET request received"); // Test output
//    }

