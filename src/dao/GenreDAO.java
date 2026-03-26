// GenreDAO.java
// Database operations for Genre table

package dao;

import model.Genre;
import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenreDAO extends BaseDAO {

    /**
     * Create a new genre
     * @param genre Genre to create
     */
    public void createGenre(Genre genre) throws SQLException {
        String sql = "INSERT INTO Genre (genreName) VALUES (?)";
        int generatedId = executeInsertWithId(sql, genre.getGenreName());
        genre.setGenreId(generatedId);
    }

    /**
     * Get genre by ID
     * @param genreId Genre ID
     * @return Genre if found, null otherwise
     */
    public Genre getGenreById(int genreId) throws SQLException {
        String sql = "SELECT genreId, genreName FROM Genre WHERE genreId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, genreId);
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
     * Get genre by name
     * @param genreName Genre name
     * @return Genre if found, null otherwise
     */
    public Genre getGenreByName(String genreName) throws SQLException {
        String sql = "SELECT genreId, genreName FROM Genre WHERE genreName = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, genreName);
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
     * Get all genres
     * @return List of all genres
     */
    public List<Genre> getAllGenres() throws SQLException {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT genreId, genreName FROM Genre";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                genres.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return genres;
    }

    /**
     * Update genre
     * @param genre Genre with updated data
     */
    public void updateGenre(Genre genre) throws SQLException {
        String sql = "UPDATE Genre SET genreName = ? WHERE genreId = ?";
        executeUpdate(sql, genre.getGenreName(), genre.getGenreId());
    }

    /**
     * Delete genre
     * @param genreId Genre ID to delete
     */
    public void deleteGenre(int genreId) throws SQLException {
        String sql = "DELETE FROM Genre WHERE genreId = ?";
        executeUpdate(sql, genreId);
    }

    /**
     * Helper to convert ResultSet to Genre object
     */
    private Genre fromResultSet(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("genreId"), rs.getString("genreName"));
    }
}
