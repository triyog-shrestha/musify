/**
 * Authentication and authorization service.
 * Handles user registration, login, password management, and profile updates.
 * 
 * Security Features:
 * - Passwords are hashed with SHA-256 before storage
 * - Legacy plain text passwords are automatically upgraded on login
 * - Email and username uniqueness is enforced
 * - Password minimum length: 8 characters
 */
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

    /**
     * Registers a new user account with validation.
     * 
     * Validations:
     * - Username, email, and password cannot be empty
     * - Email must contain @ symbol
     * - Password must be at least 8 characters
     * - Username and email must be unique
     * 
     * @param username Desired username
     * @param email    Email address (must be unique)
     * @param password Plain text password (will be hashed)
     * @return Newly created User object with assigned userId
     * @throws AuthException If validation fails or user already exists
     */
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
        if (password.length() < 8)
            throw new AuthException("Password must be at least 8 characters.");

        try {
            if (userDAO.usernameExists(username.trim()))
                throw new AuthException("Username is already taken.");
            if (userDAO.emailExists(email))
                throw new AuthException("An account with this email already exists.");

            User user = new User(username.trim(), email.trim(), hashPassword(password));
            userDAO.createUser(user);
            System.out.println("Account created successfully. Welcome, " + username + "!");
            return user;
        } catch (RuntimeException e) {
            throw dbError(e);
        }
    }

    /**
     * Authenticates a user with email and password.
     * 
     * @param email    User's email address
     * @param password Plain text password
     * @return User or Admin object if authentication succeeds
     * @throws AuthException If credentials are invalid or account doesn't exist
     */
    public User login(String email, String password) throws AuthException {
        if (email == null || email.isBlank() ||
                password == null || password.isBlank())
            throw new AuthException("Email and password cannot be empty.");

        try {
            User user = userDAO.getUserByEmail(email.trim());
            if (user == null)
                throw new AuthException("No account found with that email.");

            if (!passwordMatches(user, password))
                throw new AuthException("Incorrect password.");

            System.out.println("Welcome back, " + user.getUsername() + "!");
            return user;
        } catch (RuntimeException e) {
            throw dbError(e);
        }
    }

    /**
     * Changes a user's password after verifying the current password.
     * 
     * @param userId      User whose password to change
     * @param oldPassword Current password for verification
     * @param newPassword New password (min 8 characters)
     * @throws AuthException If old password is wrong or new password is invalid
     */
    public void changePassword(int userId, String oldPassword,
                               String newPassword) throws AuthException {
        User user;
        try {
            user = userDAO.getUserById(userId);
        } catch (RuntimeException e) {
            throw dbError(e);
        }

        if (user == null)
            throw new AuthException("User not found.");
        if (!passwordMatches(user, oldPassword))
            throw new AuthException("Current password is incorrect.");
        if (newPassword == null || newPassword.isBlank())
            throw new AuthException("New password cannot be empty.");
        if (newPassword.length() < 8)
            throw new AuthException("New password must be at least 8 characters.");

        user.setPassword(hashPassword(newPassword));
        try {
            userDAO.updateUser(user);
        } catch (RuntimeException e) {
            throw dbError(e);
        }
        System.out.println("Password updated successfully.");
    }

    /**
     * Updates a user's profile information (username and email).
     * Validates that new username/email are not taken by other users.
     * 
     * @param user     User object to update
     * @param username New username
     * @param email    New email
     * @throws AuthException If username/email is already taken
     */
    public void updateProfile(User user, String username, String email) throws AuthException {
        if (username == null || username.isBlank())
            throw new AuthException("Username cannot be empty.");
        if (email == null || email.isBlank())
            throw new AuthException("Email cannot be empty.");
        if (!email.contains("@"))
            throw new AuthException("Invalid email address.");

        String cleanUsername = username.trim();
        String cleanEmail = email.trim();

        try {
            if (userDAO.usernameExistsForOther(user.getUserId(), cleanUsername))
                throw new AuthException("Username is already taken.");
            if (userDAO.emailExistsForOther(user.getUserId(), cleanEmail))
                throw new AuthException("An account with this email already exists.");

            user.setUsername(cleanUsername);
            user.setEmail(cleanEmail);
            userDAO.updateUser(user);
        } catch (RuntimeException e) {
            throw dbError(e);
        }
    }

    /**
     * Verifies if a plain text password matches the stored hashed password.
     * Automatically upgrades legacy plain text passwords to hashed format.
     */
    private boolean passwordMatches(User user, String plainPassword) {
        String stored = user.getPassword();
        String hashedInput = hashPassword(plainPassword);

        if (stored != null && stored.equals(hashedInput)) return true;

        // Compatibility path for legacy plain text passwords.
        if (stored != null && stored.equals(plainPassword)) {
            try {
                user.setPassword(hashedInput);
                userDAO.updateUser(user);
            } catch (RuntimeException ignored) {
                // Login is still valid even if background upgrade fails.
            }
            return true;
        }
        return false;
    }

    /**
     * Wraps database exceptions in a user-friendly AuthException.
     */
    private AuthException dbError(RuntimeException e) {
        return new AuthException("Database is unavailable. Check MySQL connection and credentials.");
    }

    /**
     * Checks if a user has admin privileges.
     * 
     * @param user User to check
     * @return true if user is an Admin instance
     */
    public boolean isAdmin(User user) {
        return user instanceof Admin;
    }

    /**
     * Hashes a plain text password using SHA-256.
     * 
     * @param plain Plain text password
     * @return Hexadecimal hash string
     * @throws RuntimeException If SHA-256 algorithm is not available
     */
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