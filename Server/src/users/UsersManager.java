package users;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UsersManager {
    private Set<String> usersSet = ConcurrentHashMap.newKeySet(); // thread-safe

    // Method to add a new user by name
    public synchronized void addUser(String username) throws Exception {
        if (usersSet.contains(username)) {
            throw new Exception("User with the name '" + username + "' already exists.");
        }

        usersSet.add(username);
    }

    // Method to remove a user by name
    public synchronized boolean removeUser(String username) {
        return usersSet.remove(username);
    }

    // Method to check if a user exists by name
    public boolean isUserExist(String username) {
        return usersSet.contains(username);
    }

    // Method to retrieve all users (copy)
    public synchronized Set<String> getUsers() {
        return new HashSet<>(usersSet);
    }
}
