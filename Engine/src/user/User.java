package user;

import versions.VersionsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {
    private String name; // Client's name

    public User(String name) {
        this.name = name;
    }

    public String getUserName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

