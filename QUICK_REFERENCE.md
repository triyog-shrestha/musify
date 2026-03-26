# Musify MySQL Integration - Quick Reference

## 🚀 Quick Start (5 minutes)

### 1. Setup Database
```bash
# Create database and schema
mysql -u root -p < scripts/create-database.sql

# Load sample data
mysql -u root -p < scripts/insert-sample-data.sql
```

### 2. Configure Connection
Edit `src/config/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/musify_db
db.user=root
db.password=
```

### 3. Add MySQL Driver
Download and add `mysql-connector-java-8.0.33.jar` to project classpath

### 4. Run Application
```bash
javac -d bin -cp lib/mysql-connector-java-8.0.33.jar src/**/*.java
java -cp bin:lib/mysql-connector-java-8.0.33.jar Main
```

## 📊 Database Overview

### Schema (12 Tables)
```
User (userId, username, email, password, role, createdAt)
├─ Statistics (statId, userId, totalPlays, totalMinutesListened)
└─ Library (userId, songId, playCount)

Song (songId, trackName, length, mood, spotifyUrl, albumId)
├─ Song_Artist (songId, artistId) ──> Artist (artistId, artistName)
└─ Song_Genre (songId, genreId) ──> Genre (genreId, genreName)

Album (albumId, albumName)

Recommendation (recId, recSongName, recTrackLength, recMood, spotifyUrl)
├─ Recommendation_Artist (recId, artistId) ──> Artist
└─ Recommendation_Genre (recId, genreId) ──> Genre
```

### Sample Data Included
- 3 users (john_doe, jane_smith, admin_user)
- 15 songs from 8 artists
- 6 genres and 6 albums
- 5 recommendations

## 🛠️ Common Tasks

### Login/Register
```java
AuthService authService = new AuthService();

// Register
User user = authService.register("username", "email@example.com", "password");

// Login
User loggedIn = authService.login("email@example.com", "password");

// Check if admin
if (authService.isAdmin(loggedIn)) {
    // Show admin features
}
```

### Song Management
```java
SongService songService = new SongService();

// Get all songs
List<Song> songs = songService.getAllSongs();

// Search songs
List<Song> results = songService.search("Blinding");

// Set play count
songService.setPlayCount(songId, 10);

// Delete song
songService.deleteSong(songId);
```

### User Library
```java
LibraryDAO libraryDAO = new LibraryDAO();

// Add song to library
libraryDAO.addSongToLibrary(userId, songId, 0);

// Update play count
libraryDAO.updatePlayCount(userId, songId, 5);

// Get play count
int plays = libraryDAO.getPlayCount(userId, songId);

// Get all user songs
List<Integer> songIds = libraryDAO.getUserLibrarySongs(userId);

// Check if in library
boolean inLibrary = libraryDAO.isSongInLibrary(userId, songId);
```

### Statistics
```java
StatisticsDAO statsDAO = new StatisticsDAO();

// Initialize stats for new user
statsDAO.createUserStatistics(userId);

// Update stats
statsDAO.updateTotalPlays(userId, 150);
statsDAO.updateTotalMinutesListened(userId, 500.5);

// Or increment
statsDAO.incrementPlays(userId, 1);
statsDAO.incrementMinutesListened(userId, 3.5);

// Get stats
int plays = statsDAO.getTotalPlays(userId);
double minutes = statsDAO.getTotalMinutesListened(userId);
```

### Recommendations
```java
RecommendationService recService = new RecommendationService();

// Get all recommendations (filtered)
List<Recommendation> recs = recService.getAll();

// By mood
List<Recommendation> moodRecs = recService.byTopMood();

// By genre
List<Recommendation> genreRecs = recService.byGenre("Pop");

// By top genre
List<Recommendation> topGenreRecs = recService.byTopGenre();
```

## 📦 DAO Classes Reference

### UserDAO
```java
new UserDAO().createUser(user);
userDAO.getUserByEmail(email);
userDAO.getUserById(id);
userDAO.updateUser(user);
userDAO.deleteUser(id);
userDAO.getAllUsers();
userDAO.emailExists(email);
```

### SongDAO
```java
songDAO.insert(song);
songDAO.getAll();
songDAO.search(query);
songDAO.getSongById(id);
songDAO.delete(id);
songDAO.getArtistsForSong(songId);
songDAO.getGenresForSong(songId);
```

### LibraryDAO
```java
libraryDAO.addSongToLibrary(userId, songId, playCount);
libraryDAO.updatePlayCount(userId, songId, count);
libraryDAO.getPlayCount(userId, songId);
libraryDAO.getUserLibrarySongs(userId);
libraryDAO.removeSongFromLibrary(userId, songId);
libraryDAO.isSongInLibrary(userId, songId);
libraryDAO.getTotalPlaysForUser(userId);
libraryDAO.clearLibrary(userId);
```

### StatisticsDAO
```java
statsDAO.createUserStatistics(userId);
statsDAO.getUserStatistics(userId);  // Returns [plays, minutes]
statsDAO.updateTotalPlays(userId, plays);
statsDAO.updateTotalMinutesListened(userId, minutes);
statsDAO.incrementPlays(userId, amount);
statsDAO.incrementMinutesListened(userId, amount);
statsDAO.getTotalPlays(userId);
statsDAO.getTotalMinutesListened(userId);
statsDAO.resetStatistics(userId);
```

### ArtistDAO, GenreDAO, AlbumDAO
```java
artistDAO.createArtist(artist);
artistDAO.getArtistById(id);
artistDAO.getArtistByName(name);
artistDAO.getAllArtists();
artistDAO.updateArtist(artist);
artistDAO.deleteArtist(id);

// Same pattern for GenreDAO and AlbumDAO
```

### RecommendationDAO
```java
recDAO.getAll();
recDAO.filterByMood(mood);
recDAO.filterByGenre(genre);
recDAO.getRecommendationById(id);
recDAO.createRecommendation(rec);
recDAO.deleteRecommendation(id);
recDAO.updateRecommendation(rec);
```

## 🔧 BaseDAO Methods

All DAOs extend BaseDAO which provides:
```java
protected int executeUpdate(String sql, Object... params);
protected int executeInsertWithId(String sql, Object... params);
protected ResultSet executeQuery(String sql, Object... params);
protected void bindParameters(PreparedStatement stmt, Object... params);
```

## ⚠️ Error Handling

All DAO methods throw `SQLException`. Always wrap in try-catch:

```java
try {
    userDAO.createUser(user);
} catch (SQLException e) {
    System.err.println("Database error: " + e.getMessage());
}
```

Or declare throws in method signature:
```java
public void registerUser(String email) throws SQLException {
    userDAO.getUserByEmail(email);
}
```

## 🔍 Debugging

### Test Connection
```java
if (DatabaseConnection.testConnection()) {
    System.out.println("Connected!");
} else {
    System.out.println("Connection failed");
}
```

### Connection String
Format: `jdbc:mysql://host:port/database`
- Default: `jdbc:mysql://localhost:3306/musify_db`
- Remote: `jdbc:mysql://192.168.1.100:3306/musify_db`

### Check Configuration
```java
// If connection fails, check:
// 1. MySQL service running: systemctl status mysql
// 2. db.properties in src/config/
// 3. MySQL JDBC driver in classpath
// 4. Database created: mysql -e "SHOW DATABASES;"
```

## 📝 SQL Queries (Reference)

### Create Sample User
```sql
INSERT INTO User (username, email, password, role) 
VALUES ('testuser', 'test@example.com', 'hash123', 'USER');
```

### Get User's Statistics
```sql
SELECT totalPlays, totalMinutesListened 
FROM Statistics WHERE userId = 1;
```

### Get Songs by Genre
```sql
SELECT s.* FROM Song s
JOIN Song_Genre sg ON s.songId = sg.songId
JOIN Genre g ON sg.genreId = g.genreId
WHERE g.genreName = 'Pop';
```

### Get User's Top Songs
```sql
SELECT s.trackName, l.playCount FROM Song s
JOIN Library l ON s.songId = l.songId
WHERE l.userId = 1
ORDER BY l.playCount DESC
LIMIT 10;
```

## 📚 File Locations

| What | Where |
|------|-------|
| Database Config | `src/config/db.properties` |
| Schema | `scripts/create-database.sql` |
| Sample Data | `scripts/insert-sample-data.sql` |
| Connection Utility | `src/db/DatabaseConnection.java` |
| Base DAO | `src/dao/BaseDAO.java` |
| DAOs | `src/dao/*.java` |
| Models | `src/model/*.java` |
| Services | `src/service/*.java` |
| UI | `src/ui/*.java` |

## ✅ Checklist for New Setup

- [ ] MySQL 5.7+ installed
- [ ] Database created: `mysql -u root -p < scripts/create-database.sql`
- [ ] Sample data loaded: `mysql -u root -p < scripts/insert-sample-data.sql`
- [ ] MySQL JDBC driver added to classpath
- [ ] `src/config/db.properties` configured
- [ ] Application compiles without errors
- [ ] `Main.java` starts and tests database connection
- [ ] Login works with sample credentials
- [ ] Songs load from database
- [ ] Statistics save and retrieve correctly

## 🆘 Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| No suitable driver | Add mysql-connector-java JAR to classpath |
| Unknown database | Run `create-database.sql` first |
| Access denied | Check username/password in `db.properties` |
| Connection refused | MySQL not running, start with `systemctl start mysql` |
| Column doesn't exist | Schema mismatch, re-run `create-database.sql` |

---

**Version**: 1.0  
**Last Updated**: 2024  
**Status**: Production Ready ✅
