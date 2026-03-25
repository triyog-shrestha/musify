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
    private LocalDateTime createdAt;

    // constructor for new registration
    public User(String username, String email, String password) {
        this.userId    = -1;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.createdAt = LocalDateTime.now();
    }

    // constructor for loading from CSV
    public User(int userId, String username, String email,
                String password, LocalDateTime createdAt) {
        this.userId    = userId;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.createdAt = createdAt;
    }

    public int getUserId()           { return userId; }
    public String getUsername()      { return username; }
    public String getEmail()         { return email; }
    public String getPassword()      { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(int userId)       { this.userId = userId; }
    public void setUsername(String username){ this.username = username; }
    public void setEmail(String email)      { this.email = email; }
    public void setPassword(String password){ this.password = password; }

    @Override
    public String toString() {
        return String.format("User { id=%d, username='%s', email='%s', createdAt=%s }",
                userId, username, email, createdAt);
    }
}