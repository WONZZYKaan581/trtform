package com.example.trtform.views;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    // Kullanıcı adı -> Şifre tutan geçici hafıza
    private static final Map<String, String> users = new HashMap<>();
    private static String loggedInUser = null;
    private static String userRole = "KATILIMCI"; // Varsayılan rol

    static {
        // Test için hazır bir admin hesabı ekleyelim
        users.put("admin", "1234");
    }

    public static boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // Bu kullanıcı zaten var
        }
        users.put(username, password);
        return true;
    }

    public static boolean login(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            loggedInUser = username;
            // "admin" ile başlayanlar veya direkt admin ise rolü ADMIN yapalım
            if (username.equals("admin")) {
                userRole = "ADMIN";
            } else {
                userRole = "KATILIMCI";
            }
            return true;
        }
        return false;
    }

    public static void logout() {
        loggedInUser = null;
        userRole = "KATILIMCI";
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(userRole);
    }
}