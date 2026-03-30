// UserDAO.java
// All read and write operations for users.

package dao;

import model.Admin;
import model.User;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void init() {
        Database.init();
    }

    // save a new user and assign the generated ID back
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

    // find a user by email — used during login
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

    // find a user by ID
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

    // returns true if email is already registered
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

    // update an existing user row
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

    // convert result row to User or Admin object
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