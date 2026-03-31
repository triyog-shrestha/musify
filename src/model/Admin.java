/**
 * Represents an administrator account in the Musify system.
 * Admins have elevated privileges to manage users, view system-wide
 * statistics, and curate the recommendation pool.
 * 
 * Inherits from User but automatically sets role to "ADMIN".
 */
package model;

import java.time.LocalDateTime;

public class Admin extends User {

    /**
     * Creates a new admin instance for registration.
     * Automatically sets the role to "ADMIN".
     * 
     * @param username Admin display name
     * @param email    Admin email (must be unique)
     * @param password SHA-256 hashed password
     */
    public Admin(String username, String email, String password) {
        super(username, email, password);
        setRole("ADMIN");
    }

    /**
     * Creates an admin instance from database row data.
     * Used by UserDAO when loading admin accounts.
     * 
     * @param userId    Unique identifier from database
     * @param username  Admin display name
     * @param email     Admin email
     * @param password  SHA-256 hashed password
     * @param createdAt Account creation timestamp
     */
    public Admin(int userId, String username, String email,
                 String password, LocalDateTime createdAt) {
        super(userId, username, email, password, createdAt);
        setRole("ADMIN");
    }

    @Override
    public String toString() {
        return String.format("Admin { id=%d, username='%s', email='%s', role='%s', createdAt=%s }",
                getUserId(), getUsername(), getEmail(), getRole(), getCreatedAt());
    }
}