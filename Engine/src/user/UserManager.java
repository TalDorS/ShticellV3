package user;

import java.util.*;

/*
Adding and retrieving users is synchronized and in that manner - these actions are thread safe
Note that asking if a user exists (isUserExists) does not participate in the synchronization and it is the responsibility
of the user of this class to handle the synchronization of isUserExists with other methods here on it's own
 */
public class UserManager {

    private final Map<String, User> usersMap;

    public UserManager() {
        usersMap = new HashMap<>();
    }

    public synchronized void addUser(String username) throws Exception {
        usersMap.put(username, new User(username));
    }

    public synchronized void removeUser(String username) {
        usersMap.remove(username);
    }

    public synchronized User getUser(String username) {
        return usersMap.get(username);
    }

    public boolean isUserExists(String username) {
        return usersMap.containsKey(username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(usersMap, that.usersMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(usersMap);
    }
}

