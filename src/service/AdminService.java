/**
 * Administrative operations service.
 * Provides user management and recommendation pool management for admins.
 */
package service;

import dao.UserDAO;
import dao.RecommendationDAO;
import exception.AuthException;
import model.Recommendation;
import model.User;

import java.util.List;

public class AdminService {

    private final UserDAO              userDAO     = new UserDAO();
    private final AuthService          authService = new AuthService();
    private final RecommendationDAO    recDAO      = new RecommendationDAO();

    /**
     * Retrieves all registered users from the database.
     * 
     * @return List of all User and Admin accounts
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Creates a new user account through admin interface.
     * 
     * @param username Username for new account
     * @param email    Email for new account
     * @param password Plain text password (will be hashed)
     * @return Newly created User object
     * @throws AuthException If validation fails or user already exists
     */
    public User createUser(String username, String email,
                           String password) throws AuthException {
        return authService.register(username, email, password);
    }

    /**
     * Updates an existing user's information.
     * 
     * @param user User object with updated information
     */
    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    /**
     * Deletes a user account and all associated data.
     * 
     * @param userId ID of the user to delete
     */
    public void deleteUser(int userId) {
        userDAO.deleteUser(userId);
    }

    /**
     * Retrieves all recommendations in the global pool.
     * 
     * @return List of all recommendations
     */
    public List<Recommendation> getAllRecommendations() {
        return recDAO.getAll();
    }

    /**
     * Removes a recommendation from the global pool.
     * 
     * @param recId ID of the recommendation to delete
     */
    public void deleteRecommendation(int recId) {
        recDAO.delete(recId);
    }
}