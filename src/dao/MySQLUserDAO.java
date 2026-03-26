// MySQLUserDAO.java
// Database operations for User table using JDBC
// Replaces CSV-based operations from the original UserDAO

package dao;

import db.DatabaseConnection;
import model.Admin;
import model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLUserDAO extends BaseDAO {

    /**
     * Create a new user in the database
     * @param user User object to create
     */
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO User (username, email, password, role, createdAt) VALUES (?, ?, ?, ?, ?)";
        String role = (user instanceof Admin) ? "ADMIN" : "USER";
        int generatedId = executeInsertWithId(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                role,
                user.getCreatedAt()
        );
        user.setUserId(generatedId);
    }

    /**
     * Get a user by email address
     * @param email Email to search for
     * @return User if found, null otherwise
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT userId, username, email, password, role, createdAt FROM User WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Get a user by ID
     * @param userId User ID to search for
     * @return User if found, null otherwise
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT userId, username, email, password, role, createdAt FROM User WHERE userId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) throws SQLException {
        return getUserByEmail(email) != null;
    }

    /**
     * Update an existing user
     * @param user User object with updated data
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE User SET username = ?, email = ?, password = ? WHERE userId = ?";
        executeUpdate(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getUserId()
        );
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userId, username, email, password, role, createdAt FROM User";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return users;
    }

    /**
     * Delete a user by ID
     * @param userId User ID to delete
     */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM User WHERE userId = ?";
        executeUpdate(sql, userId);
    }

    /**
     * Helper method to convert ResultSet row to User/Admin object
     * @param rs ResultSet positioned at the row to convert
     * @return User or Admin object
     */
    private User fromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("createdAt"));

        if ("ADMIN".equals(role)) {
            return new Admin(userId, username, email, password, createdAt);
        }
        return new User(userId, username, email, password, createdAt);
    }
}
