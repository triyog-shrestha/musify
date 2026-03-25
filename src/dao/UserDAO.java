// UserDAO.java
// All read and write operations for users.csv.
// This is the only class that directly accesses user data.

package dao;

import model.Admin;
import model.User;
import util.Store;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String HEADER =
            "userId,username,email,password,role,createdAt";
    private static final String FILE = Store.DATA_DIR + "users.csv";

    // create the users.csv file if it does not exist
    public void init() {
        Store.createFileIfMissing(FILE, HEADER);
    }

    // save a new user and assign the generated ID back
    public void createUser(User user) {
        int id = Store.nextId(FILE);
        user.setUserId(id);
        Store.append(FILE, toRow(user));
    }

    // find a user by email — used during login
    public User getUserByEmail(String email) {
        for (String[] row : Store.readAll(FILE)) {
            if (row.length >= 6 && row[2].equalsIgnoreCase(email)) {
                return fromRow(row);
            }
        }
        return null;
    }

    // find a user by ID
    public User getUserById(int userId) {
        for (String[] row : Store.readAll(FILE)) {
            if (row.length >= 6 && Store.parseInt(row[0]) == userId) {
                return fromRow(row);
            }
        }
        return null;
    }

    // returns true if email is already registered
    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    // update an existing user row
    public void updateUser(User user) {
        List<String[]> all = Store.readAll(FILE);
        List<String> updated = new ArrayList<>();
        for (String[] row : all) {
            if (Store.parseInt(row[0]) == user.getUserId()) {
                updated.add(toRow(user));
            } else {
                updated.add(String.join(",", row));
            }
        }
        Store.overwrite(FILE, updated);
    }

    // convert User or Admin object to CSV row
    private String toRow(User user) {
        String role = (user instanceof Admin) ? "ADMIN" : "USER";
        return user.getUserId() + "," +
                Store.safe(user.getUsername()) + "," +
                Store.safe(user.getEmail()) + "," +
                Store.safe(user.getPassword()) + "," +
                role + "," +
                user.getCreatedAt().toString();
    }

    // convert CSV row to User or Admin object
    private User fromRow(String[] row) {
        int id             = Store.parseInt(row[0]);
        String username    = row[1];
        String email       = row[2];
        String password    = row[3];
        String role        = row[4];
        LocalDateTime date = LocalDateTime.parse(row[5]);

        if (role.equals("ADMIN")) {
            return new Admin(id, username, email, password, date);
        }
        return new User(id, username, email, password, date);
    }

    // in UserDAO.java

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (String[] row : Store.readAll(FILE)) {
            if (row.length >= 6) users.add(fromRow(row));
        }
        return users;
    }

    public void deleteUser(int userId) {
        List<String[]> all = Store.readAll(FILE);
        List<String> kept = new ArrayList<>();
        for (String[] row : all) {
            if (Store.parseInt(row[0]) != userId) {
                kept.add(String.join(",", row));
            }
        }
        Store.overwrite(FILE, kept);
    }
}