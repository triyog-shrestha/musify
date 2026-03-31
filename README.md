# Musify

Musify is a JavaFX desktop application for importing Spotify data, managing your music library, and viewing personalized listening analytics. The application features a Spotify-inspired UI with separate user and admin portals.

## Features

### Core Functionality
- **Programmatic JavaFX UI** - No FXML, all UI built with Java code
- **MySQL-backed storage** - Normalized database schema with junction tables
- **Dual-role system** - Separate user and admin accounts with distinct capabilities
- **Secure authentication** - SHA-256 password hashing with validation

### User Features
- **Registration/Login** with validation:
  - Unique username and email
  - Email must contain `@`
  - Password length >= 8 characters
  - Automatic password hash upgrade from legacy plaintext

- **CSV Import Pipeline** with automatic format detection:
  - Supports raw Exportify CSV (24 columns with audio features)
  - Supports cleaned CSV (11 columns)
  - Duration conversion (milliseconds to `mm:ss`)
  - Artist splitting on `;` with unique counting
  - Genre normalization and splitting for accurate stats
  - Duplicate detection by Spotify URL

- **Mood Engine** - Automated mood classification from audio features:
  - Five moods: `RELAXED`, `HAPPY`, `MELANCHOLIC`, `ENERGETIC`, `FOCUSED`
  - Calculated from: danceability, energy, acousticness, valence, tempo, mode
  - Numeric scoring (0.2 to 1.0) for weighted statistics

- **Library Management**:
  - Real-time search across all fields
  - Edit song metadata (title, artist, album, genre)
  - Update play count by selecting rows
  - Delete with confirmation popup
  - Clickable Spotify links to open in browser

- **Statistics Dashboard**:
  - Top songs, artists, and genres (ranked by play count)
  - Top mood and average mood score
  - Total plays and minutes listened
  - Multi-genre counting (songs with multiple genres contribute to each)

- **Personalized Recommendations**:
  - Three algorithms: top genre match, top mood match, personalized blend
  - TreeView organized by genre or mood
  - Filters out songs already in library
  - Scoring based on genre matches and mood similarity weighted by play counts

### Admin Features
- **User Management**:
  - View all registered users
  - Delete user accounts

- **Recommendation Pool Management**:
  - Import songs via CSV to shared recommendation database
  - Delete recommendations
  - All users can access the same recommendation pool

## Project Structure

```
src/
├── dao/               # Data Access Objects for database operations
│   ├── UserDAO.java
│   ├── SongDAO.java
│   ├── RecommendationDAO.java
│   └── AdminStatsDAO.java
├── exception/         # Custom exceptions
│   └── AuthException.java
├── model/             # Data models
│   ├── User.java
│   ├── Admin.java
│   ├── Song.java
│   ├── Recommendation.java
│   └── Mood.java
├── service/           # Business logic layer
│   ├── AuthService.java
│   ├── SongService.java
│   ├── RecommendationService.java
│   ├── AdminService.java
│   └── StatsService.java
├── ui/                # JavaFX screens and components
│   ├── Theme.java
│   ├── Sidebar.java
│   ├── AppContext.java
│   ├── RoleSelectionScreen.java
│   ├── LoginScreen.java
│   ├── RegisterScreen.java
│   ├── User*.java (user-specific screens)
│   └── Admin*.java (admin-specific screens)
├── util/              # Utilities
│   ├── Database.java
│   ├── GenreUtil.java
│   ├── Importer.java
│   └── MoodCalculator.java
└── Main.java          # Application entry point
```

## Database Schema

The application uses a normalized MySQL schema with the following tables:

### Core Tables
- `User` - User and admin accounts with hashed passwords
- `Song` - Song metadata with foreign keys to normalized entities
- `Library` - Junction table linking users to songs with play counts
- `Recommendation` - Shared pool of recommended songs

### Normalized Entity Tables
- `Artist` - Unique artist names
- `Genre` - Unique genre names
- `Album` - Unique album names

### Junction Tables
- `SongArtist` - Many-to-many relationship between songs and artists
- `SongGenre` - Many-to-many relationship between songs and genres
- `RecommendationArtist` - Artists for recommendation pool songs
- `RecommendationGenre` - Genres for recommendation pool songs

## Setup Instructions

### Prerequisites
- Java 11 or higher
- JavaFX SDK
- MySQL 8.0 or higher

### MySQL Setup

1. Create a MySQL database named `musify`:
```sql
CREATE DATABASE musify;
```

2. The application will automatically create all required tables on first run.

3. Configure database connection (optional):

Set environment variables if your MySQL setup differs from defaults:
```bash
export MUSIFY_DB_URL="jdbc:mysql://localhost:3306/musify?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MUSIFY_DB_USER="your_username"
export MUSIFY_DB_PASSWORD="your_password"
```

Default connection uses:
- Host: `localhost:3306`
- Database: `musify`
- User: `root`
- Password: (empty)

## Usage

### First Time Setup
1. Launch the application
2. Select either "User" or "Admin" role
3. Register a new account
4. Login with your credentials

### For Users
1. **Import Songs**: Go to Library → Import CSV → Select your Spotify export file
2. **Browse Library**: View, search, edit, and delete songs
3. **View Stats**: Check your top songs, artists, genres, and listening statistics
4. **Get Recommendations**: Browse personalized recommendations based on your library
5. **Update Profile**: Change username, email, or password

### For Admins
1. **Manage Users**: View and delete user accounts
2. **Manage Recommendations**: Import CSV files to the recommendation pool
3. **View Stats**: Monitor platform statistics (when implemented)

### CSV Import Formats

**Raw Exportify CSV** (24 columns):
```
spotify_id,name,artists,duration_ms,album,spotify_url,danceability,energy,key,loudness,mode,speechiness,acousticness,instrumentalness,liveness,valence,tempo,time_signature,explicit,popularity,artist_genres,release_date,added_at,preview_url
```

**Cleaned CSV** (11 columns):
```
title,artist,album,duration,playCount,mood,genre,releaseDate,spotifyUrl,explicit,popularity
```

Duration in cleaned CSV should be in `mm:ss` format.

## Technologies

- **Java 11+** - Core programming language
- **JavaFX** - Desktop UI framework
- **MySQL 8.0** - Relational database
- **Maven** - Build and dependency management
- **JDBC** - Database connectivity

## Architecture

The application follows a layered architecture pattern:

1. **UI Layer** (`src/ui`) - JavaFX screens and components
2. **Service Layer** (`src/service`) - Business logic and validation
3. **DAO Layer** (`src/dao`) - Database operations and queries
4. **Model Layer** (`src/model`) - Data structures and domain objects
5. **Utility Layer** (`src/util`) - Helper classes and shared functionality

Data flows: **UI → Service → DAO → Database**

## Key Technical Features

- **Normalized schema** reduces data redundancy and ensures consistency
- **Transaction handling** for atomic multi-table operations
- **Duplicate detection** prevents adding the same song twice (by Spotify URL)
- **Multi-valued field handling** with pipe `|` separator for artists/genres
- **Automatic mood calculation** from Spotify audio features
- **Weighted recommendation scoring** based on listening history
- **Role-based navigation** with separate admin and user screen flows
- **Spotify brand theming** with official color palette

## Notes

- All classes include comprehensive JavaDoc comments describing functionality
- Password security: SHA-256 hashing with automatic migration from plaintext
- Songs can have multiple artists and genres (many-to-many relationships)
- Statistics calculations use weighted averages and multi-genre counting
- Recommendation algorithms prioritize genre matches and mood similarity
