// Admin.java
// Represents an admin account. Extends User.
// The class type itself is what gives admin privileges.

package model;

import java.time.LocalDateTime;

public class Admin extends User {

    public Admin(String username, String email, String password) {
        super(username, email, password);
    }

    public Admin(int userId, String username, String email,
                 String password, LocalDateTime createdAt) {
        super(userId, username, email, password, createdAt);
    }

    @Override
    public String toString() {
        return String.format("Admin { id=%d, username='%s', email='%s', createdAt=%s }",
                getUserId(), getUsername(), getEmail(), getCreatedAt());
    }
}