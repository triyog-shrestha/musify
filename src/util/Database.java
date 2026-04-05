/**
 * MySQL database connection manager and schema initialization.
 * Handles automatic database creation, table initialization, and connection pooling.
 * 
 * The database uses a normalized schema with separate tables for artists, genres,
 * albums, and songs, connected via junction tables for many-to-many relationships.
 */
package util;

import java.sql.*;

public class Database {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/musify";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "2008";

    // Controls whether to drop all tables on startup (false = preserve data across restarts)
    private static final boolean RESET_ON_STARTUP = false;

    /**
     * Gets a connection to the Musify database.
     * MUSIFY_DB_URL, MUSIFY_DB_USER, MUSIFY_DB_PASSWORD
     * 
     * @return Active database connection
     * @throws SQLException If connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = getEnv("MUSIFY_DB_URL", DEFAULT_URL);
        String user = getEnv("MUSIFY_DB_USER", DEFAULT_USER);
        String password = getEnv("MUSIFY_DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Initializes the database schema.
     * - Loads the MySQL JDBC driver
     * - Creates the database if it doesn't exist
     * - Optionally drops all tables if RESET_ON_STARTUP is true
     * - Creates all required tables with proper relationships
     */
    public static void init() {
        String rootUrl = getEnv("MUSIFY_DB_URL", DEFAULT_URL);
        String user = getEnv("MUSIFY_DB_USER", DEFAULT_USER);
        String password = getEnv("MUSIFY_DB_PASSWORD", DEFAULT_PASSWORD);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver can be auto-loaded by newer JDBC implementations.
        }

        ensureDatabaseExists(rootUrl, user, password);
        if (RESET_ON_STARTUP) {
            dropAllTables();
        }
        createTables();
    }

    /**
     * Ensures the Musify database exists in MySQL.
     * Creates it if missing, using the database name from the JDBC URL.
     */
    private static void ensureDatabaseExists(String jdbcUrl, String user, String password) {
        ParsedUrl p = parseUrl(jdbcUrl);
        if (p.dbName.isEmpty()) return;

        String adminUrl = p.base + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String sql = "CREATE DATABASE IF NOT EXISTS " + p.dbName;
        try (Connection conn = DriverManager.getConnection(adminUrl, user, password);
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Could not ensure database exists: " + e.getMessage());
        }
    }

    /**
     * Drops all tables in the correct order to avoid foreign key constraint violations.
     * Junction tables are dropped first, then content tables, then primary entity tables.
     */
    private static void dropAllTables() {
        String[] tables = {
                // Junction tables first (foreign keys)
                "Song_Artist",
                "Song_Genre",
                "Recommendation_Artist",
                "Recommendation_Genre",
                "Library",
                // Main content tables
                "Song",
                "Recommendation",
                "Statistics",
                // Primary entities
                "Artist",
                "Genre",
                "Album",
                "`User`"
        };

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : tables) {
                st.execute("DROP TABLE IF EXISTS " + table);
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            System.out.println("Database reset error: " + e.getMessage());
        }
    }

    /**
     * Creates all database tables if they don't exist.
     * Tables are created in dependency order:
     * 1. Primary entities (User, Artist, Genre, Album)
     * 2. Content tables (Song, Recommendation)  
     * 3. User data tables (Library, Statistics)
     * 4. Junction tables (Song_Artist, Song_Genre, Recommendation_Artist, Recommendation_Genre)
     */
    private static void createTables() {
        // 1. PRIMARY ENTITIES
        String userSql = """
                CREATE TABLE IF NOT EXISTS `User` (
                    userId INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    role ENUM('ADMIN', 'LISTENER') NOT NULL DEFAULT 'LISTENER',
                    createdAt DATETIME NOT NULL
                )
                """;

        String artistSql = """
                CREATE TABLE IF NOT EXISTS Artist (
                    artistId INT AUTO_INCREMENT PRIMARY KEY,
                    artistName VARCHAR(255) NOT NULL UNIQUE
                )
                """;

        String genreSql = """
                CREATE TABLE IF NOT EXISTS Genre (
                    genreId INT AUTO_INCREMENT PRIMARY KEY,
                    genreName VARCHAR(255) NOT NULL UNIQUE
                )
                """;

        String albumSql = """
                CREATE TABLE IF NOT EXISTS Album (
                    albumId INT AUTO_INCREMENT PRIMARY KEY,
                    albumName VARCHAR(255) NOT NULL UNIQUE
                )
                """;

        // 2. MUSIC & CONTENT
        String songSql = """
                CREATE TABLE IF NOT EXISTS Song (
                    songId INT AUTO_INCREMENT PRIMARY KEY,
                    songName VARCHAR(255) NOT NULL,
                    tracklength TIME NOT NULL,
                    mood VARCHAR(40) DEFAULT 'RELAXED',
                    spotifyUrl VARCHAR(512),
                    albumId INT,
                    FOREIGN KEY (albumId) REFERENCES Album(albumId) ON DELETE SET NULL,
                    INDEX idx_songName (songName)
                )
                """;

        String recommendationSql = """
                CREATE TABLE IF NOT EXISTS Recommendation (
                    recId INT AUTO_INCREMENT PRIMARY KEY,
                    songName VARCHAR(255) NOT NULL,
                    tracklength TIME NOT NULL,
                    mood VARCHAR(40) DEFAULT 'RELAXED',
                    spotifyUrl VARCHAR(512) UNIQUE,
                    albumId INT,
                    FOREIGN KEY (albumId) REFERENCES Album(albumId) ON DELETE SET NULL,
                    INDEX idx_mood (mood)
                )
                """;

        // 3. USER DATA & ANALYTICS
        String librarySql = """
                CREATE TABLE IF NOT EXISTS Library (
                    libraryId INT AUTO_INCREMENT PRIMARY KEY,
                    userId INT NOT NULL,
                    songId INT NOT NULL,
                    playCount INT DEFAULT 0,
                    FOREIGN KEY (userId) REFERENCES `User`(userId) ON DELETE CASCADE,
                    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
                    UNIQUE KEY unique_user_song (userId, songId)
                )
                """;

        String statisticsSql = """
                CREATE TABLE IF NOT EXISTS Statistics (
                    statId INT AUTO_INCREMENT PRIMARY KEY,
                    totalPlays INT DEFAULT 0,
                    totalMinutesListened DOUBLE(10, 2),
                    userId INT,
                    FOREIGN KEY (userId) REFERENCES `User`(userId) ON DELETE CASCADE
                )
                """;

        // 4. RELATIONSHIP (JUNCTION) TABLES
        String songArtistSql = """
                CREATE TABLE IF NOT EXISTS Song_Artist (
                    songId INT NOT NULL,
                    artistId INT NOT NULL,
                    PRIMARY KEY (songId, artistId),
                    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
                    FOREIGN KEY (artistId) REFERENCES Artist(artistId) ON DELETE CASCADE
                )
                """;

        String songGenreSql = """
                CREATE TABLE IF NOT EXISTS Song_Genre (
                    songId INT NOT NULL,
                    genreId INT NOT NULL,
                    PRIMARY KEY (songId, genreId),
                    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
                    FOREIGN KEY (genreId) REFERENCES Genre(genreId) ON DELETE CASCADE
                )
                """;

        String recArtistSql = """
                CREATE TABLE IF NOT EXISTS Recommendation_Artist (
                    recId INT NOT NULL,
                    artistId INT NOT NULL,
                    PRIMARY KEY (recId, artistId),
                    FOREIGN KEY (recId) REFERENCES Recommendation(recId) ON DELETE CASCADE,
                    FOREIGN KEY (artistId) REFERENCES Artist(artistId) ON DELETE CASCADE
                )
                """;

        String recGenreSql = """
                CREATE TABLE IF NOT EXISTS Recommendation_Genre (
                    recId INT NOT NULL,
                    genreId INT NOT NULL,
                    PRIMARY KEY (recId, genreId),
                    FOREIGN KEY (recId) REFERENCES Recommendation(recId) ON DELETE CASCADE,
                    FOREIGN KEY (genreId) REFERENCES Genre(genreId) ON DELETE CASCADE
                )
                """;

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // Primary entities first
            st.execute(userSql);
            st.execute(artistSql);
            st.execute(genreSql);
            st.execute(albumSql);
            // Music & content
            st.execute(songSql);
            st.execute(recommendationSql);
            // User data & analytics
            st.execute(librarySql);
            st.execute(statisticsSql);
            // Junction tables
            st.execute(songArtistSql);
            st.execute(songGenreSql);
            st.execute(recArtistSql);
            st.execute(recGenreSql);
        } catch (SQLException e) {
            System.out.println("Database init error: " + e.getMessage());
        }
    }

    /**
     * Gets an environment variable or returns a fallback value if not set.
     */
    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    /**
     * Parses a JDBC URL to extract the base URL and database name.
     * Used to connect without a database to create the database itself.
     */
    private static ParsedUrl parseUrl(String jdbcUrl) {
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0 || slash + 1 >= jdbcUrl.length()) {
            return new ParsedUrl(jdbcUrl, "");
        }

        String afterSlash = jdbcUrl.substring(slash + 1);
        int query = afterSlash.indexOf('?');
        String dbName = (query >= 0 ? afterSlash.substring(0, query) : afterSlash).trim();
        String base = jdbcUrl.substring(0, slash);
        return new ParsedUrl(base, dbName);
    }

    private record ParsedUrl(String base, String dbName) {}
}