package service;

import dao.UserDAO;
import dao.RecommendationDAO;
import exception.AuthException;
import model.User;
import util.Importer;

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

    public void deleteUser(int userId) {
        userDAO.deleteUser(userId);
    }

//    public int importRecommendationCSV(String filePath) {
//        List<model.Recommendation> recs = Importer.parseRecommendations(filePath);
//        recDAO.insertBatch(recs);
//        return recs.size();
//    }
}