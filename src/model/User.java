// User.java
// Represents a registered user in the system.
// Holds all user data and does not handle any logic.

package model;

import java.time.LocalDateTime;

public class User {

    private int userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private LocalDateTime createdAt;

    // constructor for new registration
    public User(String username, String email, String password) {
        this.userId    = -1;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.role      = "LISTENER";
        this.createdAt = LocalDateTime.now();
    }

    // constructor for loading from database
    public User(int userId, String username, String email,
                String password, LocalDateTime createdAt) {
        this.userId    = userId;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.role      = "LISTENER";
        this.createdAt = createdAt;
    }

    public int getUserId()           { return userId; }
    public String getUsername()      { return username; }
    public String getEmail()         { return email; }
    public String getPassword()      { return password; }
    public String getRole()          { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(int userId)       { this.userId = userId; }
    public void setUsername(String username){ this.username = username; }
    public void setEmail(String email)      { this.email = email; }
    public void setPassword(String password){ this.password = password; }
    public void setRole(String role)        { this.role = role; }

    @Override
    public String toString() {
        return String.format("User { id=%d, username='%s', email='%s', role='%s', createdAt=%s }",
                userId, username, email, role, createdAt);
    }
}