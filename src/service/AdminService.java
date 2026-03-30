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

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User createUser(String username, String email,
                           String password) throws AuthException {
        return authService.register(username, email, password);
    }

    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    public void deleteUser(int userId) {
        userDAO.deleteUser(userId);
    }

    public List<Recommendation> getAllRecommendations() {
        return recDAO.getAll();
    }

    public void deleteRecommendation(int recId) {
        recDAO.delete(recId);
    }
}