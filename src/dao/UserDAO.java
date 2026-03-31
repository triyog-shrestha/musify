/**
 * Data Access Object for user account operations.
 * Handles CRUD operations for User and Admin accounts in the database.
 * Supports user authentication, registration, and profile management.
 */
package dao;

import model.Admin;
import model.User;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Initializes the database schema.
     * Can be called to ensure tables exist.
     */
    public void init() {
        Database.init();
    }

    /**
     * Saves a new user to the database and assigns the generated userId.
     * Automatically detects if the user is an Admin and sets the role accordingly.
     * 
     * @param user User or Admin object to create
     * @throws RuntimeException If database operation fails
     */
    public void createUser(User user) {
        String sql = "INSERT INTO `User`(username,email,password,role,createdAt) VALUES(?,?,?,?,?)";
        String role = (user instanceof Admin) ? "ADMIN" : "LISTENER";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, role);
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setUserId(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user.", e);
        }
    }

    /**
     * Finds a user by email address (case-insensitive).
     * Returns an Admin object if the user has ADMIN role, otherwise returns User.
     * 
     * @param email Email address to search for
     * @return User or Admin object, or null if not found
     * @throws RuntimeException If database operation fails
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT userId,username,email,password,role,createdAt FROM `User` WHERE LOWER(email)=LOWER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch user by email.", e);
        }
        return null;
    }

    /**
     * Finds a user by their unique ID.
     * Returns an Admin object if the user has ADMIN role, otherwise returns User.
     * 
     * @param userId Unique user identifier
     * @return User or Admin object, or null if not found
     * @throws RuntimeException If database operation fails
     */
    public User getUserById(int userId) {
        String sql = "SELECT userId,username,email,password,role,createdAt FROM `User` WHERE userId=? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch user by id.", e);
        }
        return null;
    }

    /**
     * Checks if an email address is already registered (case-insensitive).
     * 
     * @param email Email address to check
     * @return true if email exists, false otherwise
     * @throws RuntimeException If database operation fails
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM `User` WHERE LOWER(email)=LOWER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate email uniqueness.", e);
        }
    }

    /**
     * Checks if a username is already taken (case-insensitive).
     * 
     * @param username Username to check
     * @return true if username exists, false otherwise
     * @throws RuntimeException If database operation fails
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM `User` WHERE LOWER(username)=LOWER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate username uniqueness.", e);
        }
    }

    /**
     * Checks if an email is taken by a different user (used for profile updates).
     * 
     * @param userId User ID to exclude from check
     * @param email  Email address to check
     * @return true if email exists for another user, false otherwise
     * @throws RuntimeException If database operation fails
     */
    public boolean emailExistsForOther(int userId, String email) {
        String sql = "SELECT 1 FROM `User` WHERE LOWER(email)=LOWER(?) AND userId<>? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate email uniqueness.", e);
        }
    }

    /**
     * Checks if a username is taken by a different user (used for profile updates).
     * 
     * @param userId   User ID to exclude from check
     * @param username Username to check
     * @return true if username exists for another user, false otherwise
     * @throws RuntimeException If database operation fails
     */
    public boolean usernameExistsForOther(int userId, String username) {
        String sql = "SELECT 1 FROM `User` WHERE LOWER(username)=LOWER(?) AND userId<>? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate username uniqueness.", e);
        }
    }

    /**
     * Updates an existing user's information in the database.
     * 
     * @param user User object with updated information
     * @throws RuntimeException If database operation fails
     */
    public void updateUser(User user) {
        String sql = "UPDATE `User` SET username=?, email=?, password=?, role=?, createdAt=? WHERE userId=?";
        String role = (user instanceof Admin) ? "ADMIN" : "LISTENER";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, role);
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            ps.setInt(6, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update user.", e);
        }
    }

    /**
     * Converts a database result row to a User or Admin object.
     * Automatically creates an Admin instance if role is "ADMIN".
     * 
     * @param rs ResultSet positioned at a user row
     * @return User or Admin object
     * @throws SQLException If column access fails
     */
    private User fromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("userId");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");
        LocalDateTime date = rs.getTimestamp("createdAt").toLocalDateTime();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, username, email, password, date);
        }
        return new User(id, username, email, password, date);
    }

    /**
     * Retrieves all users from the database, ordered by userId.
     * 
     * @return List of all User and Admin objects
     * @throws RuntimeException If database operation fails
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userId,username,email,password,role,createdAt FROM `User` ORDER BY userId";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(fromRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch all users.", e);
        }
        return users;
    }

    /**
     * Deletes a user from the database by their userId.
     * Cascade delete will remove associated library entries and statistics.
     * 
     * @param userId ID of the user to delete
     * @throws RuntimeException If database operation fails
     */
    public void deleteUser(int userId) {
        String sql = "DELETE FROM `User` WHERE userId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete user.", e);
        }
    }
}