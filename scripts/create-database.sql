-- Create Musify Database Schema
-- This script creates all tables for the Personal Music Analysis System

CREATE DATABASE IF NOT EXISTS musify_db;
USE musify_db;

-- User Table
CREATE TABLE IF NOT EXISTS User (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Artist Table
CREATE TABLE IF NOT EXISTS Artist (
    artistId INT PRIMARY KEY AUTO_INCREMENT,
    artistName VARCHAR(255) NOT NULL UNIQUE
);

-- Album Table
CREATE TABLE IF NOT EXISTS Album (
    albumId INT PRIMARY KEY AUTO_INCREMENT,
    albumName VARCHAR(255) NOT NULL
);

-- Genre Table
CREATE TABLE IF NOT EXISTS Genre (
    genreId INT PRIMARY KEY AUTO_INCREMENT,
    genreName VARCHAR(255) NOT NULL UNIQUE
);

-- Song Table
CREATE TABLE IF NOT EXISTS Song (
    songId INT PRIMARY KEY AUTO_INCREMENT,
    trackName VARCHAR(255) NOT NULL,
    length VARCHAR(10),
    mood VARCHAR(100),
    spotifyUrl VARCHAR(500),
    albumId INT,
    FOREIGN KEY (albumId) REFERENCES Album(albumId) ON DELETE SET NULL
);

-- Song_Artist Junction Table
CREATE TABLE IF NOT EXISTS Song_Artist (
    songId INT NOT NULL,
    artistId INT NOT NULL,
    PRIMARY KEY (songId, artistId),
    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
    FOREIGN KEY (artistId) REFERENCES Artist(artistId) ON DELETE CASCADE
);

-- Song_Genre Junction Table
CREATE TABLE IF NOT EXISTS Song_Genre (
    songId INT NOT NULL,
    genreId INT NOT NULL,
    PRIMARY KEY (songId, genreId),
    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
    FOREIGN KEY (genreId) REFERENCES Genre(genreId) ON DELETE CASCADE
);

-- Library Table (User's playlist with play counts)
CREATE TABLE IF NOT EXISTS Library (
    playCount INT DEFAULT 0,
    userId INT NOT NULL,
    songId INT NOT NULL,
    PRIMARY KEY (userId, songId),
    FOREIGN KEY (userId) REFERENCES User(userId) ON DELETE CASCADE,
    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE
);

-- Statistics Table
CREATE TABLE IF NOT EXISTS Statistics (
    statId INT PRIMARY KEY AUTO_INCREMENT,
    totalPlays INT DEFAULT 0,
    totalMinutesListened DOUBLE DEFAULT 0,
    userId INT NOT NULL UNIQUE,
    FOREIGN KEY (userId) REFERENCES User(userId) ON DELETE CASCADE
);

-- Recommendation Table
CREATE TABLE IF NOT EXISTS Recommendation (
    recId INT PRIMARY KEY AUTO_INCREMENT,
    recSongName VARCHAR(255) NOT NULL,
    recTrackLength VARCHAR(10),
    recMood VARCHAR(100),
    spotifyUrl VARCHAR(500),
    albumId INT,
    FOREIGN KEY (albumId) REFERENCES Album(albumId) ON DELETE SET NULL
);

-- Recommendation_Artist Junction Table
CREATE TABLE IF NOT EXISTS Recommendation_Artist (
    recId INT NOT NULL,
    artistId INT NOT NULL,
    PRIMARY KEY (recId, artistId),
    FOREIGN KEY (recId) REFERENCES Recommendation(recId) ON DELETE CASCADE,
    FOREIGN KEY (artistId) REFERENCES Artist(artistId) ON DELETE CASCADE
);

-- Recommendation_Genre Junction Table
CREATE TABLE IF NOT EXISTS Recommendation_Genre (
    recId INT NOT NULL,
    genreId INT NOT NULL,
    PRIMARY KEY (recId, genreId),
    FOREIGN KEY (recId) REFERENCES Recommendation(recId) ON DELETE CASCADE,
    FOREIGN KEY (genreId) REFERENCES Genre(genreId) ON DELETE CASCADE
);

-- Recommendation_Artist (for artists associated with recommendations) Table
CREATE TABLE IF NOT EXISTS RecommendationLibrary (
    recommendationSongId INT PRIMARY KEY AUTO_INCREMENT,
    recommendationSongName VARCHAR(255) NOT NULL,
    recommendationArtistId INT,
    FOREIGN KEY (recommendationArtistId) REFERENCES Artist(artistId) ON DELETE SET NULL
);

CREATE INDEX idx_user_email ON User(email);
CREATE INDEX idx_song_trackname ON Song(trackName);
CREATE INDEX idx_artist_name ON Artist(artistName);
CREATE INDEX idx_album_name ON Album(albumName);
CREATE INDEX idx_genre_name ON Genre(genreName);
CREATE INDEX idx_library_userid ON Library(userId);
CREATE INDEX idx_library_songid ON Library(songId);
