# Musify - JavaFX GUI with MySQL Database

## Project Overview
Musify is a Personal Music Analysis System built with JavaFX GUI and MySQL database. Users can manage their music library, track statistics, get recommendations, and more.

## System Requirements
- **Java**: JDK 11 or higher
- **MySQL**: 5.7 or higher
- **JavaFX SDK**: 21 or higher

## Installation & Setup

### Step 1: Database Setup
1. **Create the database and schema:**
   ```bash
   mysql -u root -p < scripts/create-database.sql
   ```

2. **Insert sample data:**
   ```bash
   mysql -u root -p < scripts/insert-sample-data.sql
   ```

3. **Verify database created:**
   ```bash
   mysql -u root -p -e "USE musify_db; SHOW TABLES;"
   ```

### Step 2: Configure Database Connection
1. Edit `src/config/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/musify_db
   db.user=root
   db.password=
   ```
   Update with your MySQL credentials if needed.

### Step 3: Add MySQL JDBC Driver
1. Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/
2. Add the JAR to your project's classpath/lib directory
3. Or if using Maven, add to pom.xml:
   ```xml
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <version>8.0.33</version>
   </dependency>
   ```

### Step 4: Compile & Run
```bash
javac -d bin src/**/*.java
java -cp bin:lib/mysql-connector-java-8.0.33.jar Main
```

## Database Schema

### Core Tables
- **User**: User accounts (userId, username, email, password, role, createdAt)
- **Song**: Music tracks (songId, trackName, length, mood, spotifyUrl, albumId)
- **Artist**: Artist information (artistId, artistName)
- **Album**: Album information (albumId, albumName)
- **Genre**: Music genres (genreId, genreName)

### Junction Tables
- **Song_Artist**: Many-to-many relationship between songs and artists
- **Song_Genre**: Many-to-many relationship between songs and genres

### User-Specific Tables
- **Library**: User's music library with play counts (userId, songId, playCount)
- **Statistics**: User statistics (statId, userId, totalPlays, totalMinutesListened)

### Recommendation Tables
- **Recommendation**: Recommended songs
- **Recommendation_Artist**: Artists associated with recommendations
- **Recommendation_Genre**: Genres associated with recommendations

## Sample Data

The database includes:
- **3 Users**: john_doe, jane_smith, admin_user (admin role)
- **15 Songs**: Popular tracks from Drake, The Weeknd, Kendrick Lamar, Ariana Grande, Post Malone, Billie Eilish, Ed Sheeran, Taylor Swift
- **8 Artists**: Drake, The Weeknd, Kendrick Lamar, Ariana Grande, Post Malone, Billie Eilish, Ed Sheeran, Taylor Swift
- **4 Genres**: Hip-Hop, R&B, Pop, Indie, Electronic, Rock
- **5 Recommendations**: Mood-matched recommendations for users

### Default Test Credentials
```
Email: john@example.com
Password: (hashed)

Admin Email: admin@example.com
Password: (hashed)
```

## Architecture

### Layer Structure
1. **UI Layer** (`ui/`): JavaFX screens and components
2. **Service Layer** (`service/`): Business logic (AuthService, SongService, etc.)
3. **DAO Layer** (`dao/`): Database access objects
4. **Database Layer** (`db/`): Connection management and utilities
5. **Model Layer** (`model/`): Domain objects

### Key Classes
- `DatabaseConnection.java`: Manages MySQL connections
- `BaseDAO.java`: Abstract base class for all DAOs with common JDBC operations
- `UserDAO.java`: User CRUD operations
- `SongDAO.java`: Song CRUD operations with artist/genre support
- `ArtistDAO.java`, `GenreDAO.java`, `AlbumDAO.java`: Supporting DAOs

## Features

### User Features
- **Login/Registration**: User account management with password hashing
- **Library Management**: Import songs, view library
- **Play Statistics**: Track total plays and listening time
- **Recommendations**: Get music recommendations based on mood
- **Profile Management**: Update user information

### Admin Features
- **User Management**: View and manage user accounts
- **Recommendation Library**: Manage system recommendations
- **App Statistics**: View overall app usage statistics

## Configuration

### Database Configuration (src/config/db.properties)
```properties
# MySQL Connection Details
db.url=jdbc:mysql://localhost:3306/musify_db
db.user=root
db.password=
```

### Supported Databases
- **Local MySQL**: Default setup
- **Remote MySQL**: Update db.url with host:port
- **MariaDB**: Compatible, use appropriate JDBC driver

## Troubleshooting

### Connection Error: "No suitable driver found"
- **Solution**: Ensure mysql-connector-java JAR is in classpath

### Error: "Unknown database 'musify_db'"
- **Solution**: Run `scripts/create-database.sql` first

### Error: "Access denied for user"
- **Solution**: Check db.properties credentials match your MySQL setup

### Error: "Communications link failure"
- **Solution**: Verify MySQL service is running: `systemctl status mysql`

## Migration from CSV to MySQL

The project has been migrated from CSV file storage to MySQL:
- Original `UserDAO` now extends `BaseDAO` and uses JDBC
- Original `SongDAO` now extends `BaseDAO` and uses JDBC
- CSV files are no longer used for data storage
- All service layers work transparently with new database backend

## Development Notes

### Adding New Features
1. Create model class in `src/model/`
2. Create DAO in `src/dao/` extending `BaseDAO`
3. Create service in `src/service/` using the DAO
4. Create UI screen in `src/ui/`

### Best Practices
- Always use prepared statements to prevent SQL injection
- Close resources in finally blocks (handled by DatabaseConnection)
- Use BaseDAO.executeUpdate() for INSERT/UPDATE/DELETE
- Use BaseDAO.executeQuery() for SELECT operations
- Add appropriate logging and error handling

## License
This project is part of the Musify Music Management System

## Support
For issues or questions:
1. Check database configuration in `src/config/db.properties`
2. Verify MySQL service is running
3. Review error messages in console output
4. Check database schema matches `scripts/create-database.sql`
