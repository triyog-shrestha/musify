-- Musify normalized schema (as requested)

-- 1. Independent/Primary Entities
CREATE TABLE IF NOT EXISTS `User` (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    createdAt DATE
);

CREATE TABLE IF NOT EXISTS Artist (
    artistId INT AUTO_INCREMENT PRIMARY KEY,
    artistName VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Genre (
    genreId INT AUTO_INCREMENT PRIMARY KEY,
    genreName VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Album (
    albumId INT AUTO_INCREMENT PRIMARY KEY,
    albumName VARCHAR(255) NOT NULL
);

-- 2. Dependent Entities
CREATE TABLE IF NOT EXISTS Statistics (
    statId INT AUTO_INCREMENT PRIMARY KEY,
    totalPlays INT DEFAULT 0,
    totalMinutesListened DOUBLE(10, 2),
    UseruserId INT,
    FOREIGN KEY (UseruserId) REFERENCES `User`(userId)
);

CREATE TABLE IF NOT EXISTS Song (
    songId INT AUTO_INCREMENT PRIMARY KEY,
    songName VARCHAR(255) NOT NULL,
    tracklength TIME,
    mood VARCHAR(255),
    spotifyUrl VARCHAR(255),
    AlbumalbumId INT,
    FOREIGN KEY (AlbumalbumId) REFERENCES Album(albumId)
);

CREATE TABLE IF NOT EXISTS Recommendation (
    recSongId INT AUTO_INCREMENT PRIMARY KEY,
    recSongName VARCHAR(255),
    recTrackLength TIME,
    recMood VARCHAR(255),
    spotifyUrl VARCHAR(255),
    AlbumalbumId INT,
    FOREIGN KEY (AlbumalbumId) REFERENCES Album(albumId)
);

-- 3. Junction Tables (Many-to-Many Relationships)
CREATE TABLE IF NOT EXISTS Library (
    playCount INT DEFAULT 0,
    SongsongId INT,
    UseruserId INT,
    PRIMARY KEY (SongsongId, UseruserId),
    FOREIGN KEY (SongsongId) REFERENCES Song(songId),
    FOREIGN KEY (UseruserId) REFERENCES `User`(userId)
);

CREATE TABLE IF NOT EXISTS Song_Artist (
    SongsongId INT,
    ArtistartistId INT,
    PRIMARY KEY (SongsongId, ArtistartistId),
    FOREIGN KEY (SongsongId) REFERENCES Song(songId),
    FOREIGN KEY (ArtistartistId) REFERENCES Artist(artistId)
);

CREATE TABLE IF NOT EXISTS Song_Genre (
    GenregenreId INT,
    SongsongId INT,
    PRIMARY KEY (GenregenreId, SongsongId),
    FOREIGN KEY (GenregenreId) REFERENCES Genre(genreId),
    FOREIGN KEY (SongsongId) REFERENCES Song(songId)
);

CREATE TABLE IF NOT EXISTS Recommendation_Artist (
    RecommendationrecSongId INT,
    ArtistartistId INT,
    PRIMARY KEY (RecommendationrecSongId, ArtistartistId),
    FOREIGN KEY (RecommendationrecSongId) REFERENCES Recommendation(recSongId),
    FOREIGN KEY (ArtistartistId) REFERENCES Artist(artistId)
);

CREATE TABLE IF NOT EXISTS Recommendation_Genre (
    RecommendationrecSongId INT,
    GenregenreId INT,
    PRIMARY KEY (RecommendationrecSongId, GenregenreId),
    FOREIGN KEY (RecommendationrecSongId) REFERENCES Recommendation(recSongId),
    FOREIGN KEY (GenregenreId) REFERENCES Genre(genreId)
);
