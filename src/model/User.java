/**
 * Represents a registered user (listener) account in the Musify system.
 * Users can import their music libraries, track listening statistics,
 * and receive personalized recommendations based on their listening patterns.
 * 
 * Password Security: Passwords are stored as SHA-256 hashes, not plain text.
 * The AuthService handles all password hashing and verification.
 */
package model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String email;
    private String password;         // SHA-256 hashed
    private String role;             // "LISTENER" or "ADMIN"
    private LocalDateTime createdAt;

    /**
     * Creates a new user instance for registration.
     * Used when creating a brand new user account.
     * The userId will be set to -1 until the user is saved to the database.
     * 
     * @param username Display name for the user
     * @param email    Email address (must be unique)
     * @param password SHA-256 hashed password
     */
    public User(String username, String email, String password) {
        this.userId = -1;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "LISTENER";
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a user instance from database row data.
     * Used by UserDAO when loading users from the database.
     * 
     * @param userId    Unique identifier from database
     * @param username  Display name
     * @param email     Email address
     * @param password  SHA-256 hashed password
     * @param createdAt Account creation timestamp
     */
    public User(int userId, String username, String email,
                String password, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "LISTENER";
        this.createdAt = createdAt;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return String.format("User { id=%d, username='%s', email='%s', role='%s', createdAt=%s }",
                userId, username, email, role, createdAt);
    }
}