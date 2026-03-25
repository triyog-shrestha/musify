// AuthService.java
// Handles login, registration and password changes.
// Never touches files directly — always goes through UserDAO.

package service;

import dao.UserDAO;
import exception.AuthException;
import model.Admin;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // register a new user
    public User register(String username, String email,
                         String password) throws AuthException {
        if (username == null || username.isBlank())
            throw new AuthException("Username cannot be empty.");
        if (email == null || email.isBlank())
            throw new AuthException("Email cannot be empty.");
        if (!email.contains("@"))
            throw new AuthException("Invalid email address.");
        if (password == null || password.isBlank())
            throw new AuthException("Password cannot be empty.");
        if (userDAO.emailExists(email))
            throw new AuthException("An account with this email already exists.");

        User user = new User(username.trim(), email.trim(), hashPassword(password));
        userDAO.createUser(user);
        System.out.println("Account created successfully. Welcome, " + username + "!");
        return user;
    }

    // login an existing user
    public User login(String email, String password) throws AuthException {
        if (email == null || email.isBlank() ||
                password == null || password.isBlank())
            throw new AuthException("Email and password cannot be empty.");

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null)
            throw new AuthException("No account found with that email.");
        if (!user.getPassword().equals(hashPassword(password)))
            throw new AuthException("Incorrect password.");

        System.out.println("Welcome back, " + user.getUsername() + "!");
        return user;
    }

    // change password — verifies old password first
    public void changePassword(int userId, String oldPassword,
                               String newPassword) throws AuthException {
        User user = userDAO.getUserById(userId);
        if (user == null)
            throw new AuthException("User not found.");
        if (!user.getPassword().equals(hashPassword(oldPassword)))
            throw new AuthException("Current password is incorrect.");
        if (newPassword == null || newPassword.isBlank())
            throw new AuthException("New password cannot be empty.");

        user.setPassword(hashPassword(newPassword));
        userDAO.updateUser(user);
        System.out.println("Password updated successfully.");
    }

    // returns true if user is an Admin
    public boolean isAdmin(User user) {
        return user instanceof Admin;
    }

    // hashes a plain text password using SHA-256
    public String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available.", e);
        }
    }
}