package com.example.trtform.views;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static String loggedInUser = null;
    private static final Map<String, UserProfile> users = new HashMap<>();

    static {
        // Test için varsayılan kullanıcı
        users.put("admin", new UserProfile("admin", "123", "Admin", "User"));
    }

    public static boolean register(String username, String password, String firstName, String lastName) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new UserProfile(username, password, firstName, lastName));
        return true;
    }

    public static boolean login(String username, String password) {
        UserProfile user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = username;
            return true;
        }
        return false;
    }

    public static void logout() {
        loggedInUser = null;
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    public static String getLoggedInUserFullName() {
        if (loggedInUser == null) return "Misafir";
        UserProfile user = users.get(loggedInUser);
        return user != null ? user.getFirstName() + " " + user.getLastName() : loggedInUser;
    }

    public static class UserProfile {
        private String username;
        private String password;
        private String firstName;
        private String lastName;

        public UserProfile(String username, String password, String firstName, String lastName) {
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }

    public static boolean isAdmin() {
    if (loggedInUser == null) return false;
    // Örneğin "admin" kullanıcı adını admin kabul edelim veya kullanıcı profiline rol ekleyebiliriz
    return "admin".equals(loggedInUser);
}
}