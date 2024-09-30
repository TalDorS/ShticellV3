package utils;

import jakarta.servlet.ServletContext;
import users.UsersManager;

public class ServletUtils {
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "usersManager";
    private static final Object userManagerLock = new Object();

    public static UsersManager getUserManager(ServletContext servletContext) {

        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UsersManager());
            }
        }
        return (UsersManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }
}
