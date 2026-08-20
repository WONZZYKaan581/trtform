package com.example.trtform.service;

import com.example.trtform.model.User;
import com.example.trtform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        boolean firstUser = userRepository.count() == 0;
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFull_name(firstName + " " + lastName);
        newUser.setRole(firstUser || "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER");

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

    public boolean isLoggedIn() {
        return loggedInUser != null && !loggedInUser.isBlank();
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
        return isAdmin(loggedInUser);
    }

    public boolean isAdmin(String username) {
        if (username == null || username.isBlank()) return false;
        User user = userRepository.findByUsername(username);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(user -> user.getUsername(), String.CASE_INSENSITIVE_ORDER))
                .map(user -> new UserSummaryDto(
                        user.getId(),
                        user.getUsername(),
                        user.getFull_name(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    public boolean updateUserRole(String username, String role) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }

        user.setRole("ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "USER");
        userRepository.save(user);
        return true;
    }

    public static class UserSummaryDto {
        private final Long id;
        private final String username;
        private final String fullName;
        private final String role;

        public UserSummaryDto(Long id, String username, String fullName, String role) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
    }
}