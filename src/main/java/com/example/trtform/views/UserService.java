package com.example.trtform.views;

import com.example.trtform.model.User;
import com.example.trtform.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static String loggedInUser = null;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String username, String password, String firstName, String lastName) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFull_name(firstName + " " + lastName);
        newUser.setRole("admin".equals(username) ? "ADMIN" : "USER");

        userRepository.save(newUser);
        return true;
    }

    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = username;
            return true;
        }
        return false;
    }

    public void logout() {
        loggedInUser = null;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public String getLoggedInUserFullName() {
        if (loggedInUser == null) return "Misafir";
        User user = userRepository.findByUsername(loggedInUser);
        return user != null ? user.getFull_name() : loggedInUser;
    }

    public boolean isAdmin() {
        if (loggedInUser == null) return false;
        User user = userRepository.findByUsername(loggedInUser);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}