-- Insert Sample Data for Musify Database

USE musify_db;

-- Clear existing data (optional - remove if you want to preserve data)
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE User;
TRUNCATE TABLE Artist;
TRUNCATE TABLE Album;
TRUNCATE TABLE Genre;
TRUNCATE TABLE Song;
TRUNCATE TABLE Song_Artist;
TRUNCATE TABLE Song_Genre;
TRUNCATE TABLE Library;
TRUNCATE TABLE Statistics;
TRUNCATE TABLE Recommendation;
TRUNCATE TABLE Recommendation_Artist;
TRUNCATE TABLE Recommendation_Genre;
TRUNCATE TABLE RecommendationLibrary;
SET FOREIGN_KEY_CHECKS=1;

-- Insert Users
INSERT INTO User (username, email, password, role, createdAt) VALUES
('john_doe', 'john@example.com', '8d969eef6ecad3c29a3a873fba2d9f7f3acb51f0308fc546fe16db90ef8ef14d', 'USER', '2024-01-15 10:30:00'),
('jane_smith', 'jane@example.com', '8d969eef6ecad3c29a3a873fba2d9f7f3acb51f0308fc546fe16db90ef8ef14d', 'USER', '2024-02-20 14:45:00'),
('admin_user', 'admin@example.com', '8d969eef6ecad3c29a3a873fba2d9f7f3acb51f0308fc546fe16db90ef8ef14d', 'ADMIN', '2024-01-01 09:00:00');

-- Insert Artists
INSERT INTO Artist (artistName) VALUES
('Drake'),
('The Weeknd'),
('Kendrick Lamar'),
('Ariana Grande'),
('Post Malone'),
('Billie Eilish'),
('Ed Sheeran'),
('Taylor Swift');

-- Insert Genres
INSERT INTO Genre (genreName) VALUES
('Hip-Hop'),
('R&B'),
('Pop'),
('Indie'),
('Electronic'),
('Rock');

-- Insert Albums
INSERT INTO Album (albumName) VALUES
('Certified Lover Boy'),
('After Hours'),
('good kid, m.A.A.d city'),
('Positions'),
('Hollywood\'s Bleeding'),
('When We All Fall Asleep, Where Do We Go?');

-- Insert Songs
INSERT INTO Song (trackName, length, mood, spotifyUrl, albumId) VALUES
('One Dance', '04:04', 'happy', 'https://open.spotify.com/track/1', 1),
('Controlla', '03:27', 'chill', 'https://open.spotify.com/track/2', 1),
('Blinding Lights', '03:20', 'energetic', 'https://open.spotify.com/track/3', 2),
('The Hills', '04:03', 'dark', 'https://open.spotify.com/track/4', 2),
('Swimming Pools (Drank)', '04:37', 'dark', 'https://open.spotify.com/track/5', 3),
('Backseat Freestyle', '04:10', 'energetic', 'https://open.spotify.com/track/6', 3),
('thank u, next', '03:32', 'happy', 'https://open.spotify.com/track/7', 4),
('6 rings', '03:44', 'romantic', 'https://open.spotify.com/track/8', 4),
('Circles', '03:34', 'chill', 'https://open.spotify.com/track/9', 5),
('Goodbyes', '03:55', 'melancholic', 'https://open.spotify.com/track/10', 5),
('when the party\'s over', '03:12', 'chill', 'https://open.spotify.com/track/11', 6),
('bad guy', '03:14', 'dark', 'https://open.spotify.com/track/12', 6),
('Shape of You', '03:53', 'happy', 'https://open.spotify.com/track/13', NULL),
('Perfect', '04:23', 'romantic', 'https://open.spotify.com/track/14', NULL),
('Anti-Hero', '03:20', 'reflective', 'https://open.spotify.com/track/15', NULL);

-- Link Songs to Artists (Song_Artist)
INSERT INTO Song_Artist (songId, artistId) VALUES
(1, 1), (1, 2),  -- One Dance: Drake, The Weeknd
(2, 1),          -- Controlla: Drake
(3, 2),          -- Blinding Lights: The Weeknd
(4, 2),          -- The Hills: The Weeknd
(5, 3),          -- Swimming Pools: Kendrick Lamar
(6, 3),          -- Backseat Freestyle: Kendrick Lamar
(7, 4),          -- thank u, next: Ariana Grande
(8, 4),          -- 6 rings: Ariana Grande
(9, 5),          -- Circles: Post Malone
(10, 5),         -- Goodbyes: Post Malone
(11, 6),         -- when the party's over: Billie Eilish
(12, 6),         -- bad guy: Billie Eilish
(13, 7),         -- Shape of You: Ed Sheeran
(14, 7),         -- Perfect: Ed Sheeran
(15, 8);         -- Anti-Hero: Taylor Swift

-- Link Songs to Genres (Song_Genre)
INSERT INTO Song_Genre (songId, genreId) VALUES
(1, 1), (1, 2),  -- One Dance: Hip-Hop, R&B
(2, 1), (2, 2),  -- Controlla: Hip-Hop, R&B
(3, 5), (3, 3),  -- Blinding Lights: Electronic, Pop
(4, 5), (4, 2),  -- The Hills: Electronic, R&B
(5, 1),          -- Swimming Pools: Hip-Hop
(6, 1),          -- Backseat Freestyle: Hip-Hop
(7, 3),          -- thank u, next: Pop
(8, 3),          -- 6 rings: Pop
(9, 5), (9, 3),  -- Circles: Electronic, Pop
(10, 3), (10, 2),-- Goodbyes: Pop, R&B
(11, 3), (11, 4),-- when the party's over: Pop, Indie
(12, 3),         -- bad guy: Pop
(13, 3),         -- Shape of You: Pop
(14, 3),         -- Perfect: Pop
(15, 3);         -- Anti-Hero: Pop

-- Insert Library Data (User play counts)
INSERT INTO Library (userId, songId, playCount) VALUES
(1, 1, 45),
(1, 2, 32),
(1, 3, 78),
(1, 5, 22),
(1, 7, 15),
(2, 4, 35),
(2, 6, 28),
(2, 9, 52),
(2, 11, 40),
(2, 14, 18);

-- Insert Statistics
INSERT INTO Statistics (userId, totalPlays, totalMinutesListened) VALUES
(1, 192, 720.5),
(2, 173, 645.25);

-- Insert Recommendations
INSERT INTO Recommendation (recSongName, recTrackLength, recMood, spotifyUrl, albumId) VALUES
('Redo', '03:22', 'happy', 'https://open.spotify.com/track/rec1', NULL),
('DNA.', '04:08', 'dark', 'https://open.spotify.com/track/rec2', NULL),
('Die For You', '03:42', 'romantic', 'https://open.spotify.com/track/rec3', NULL),
('All Too Well', '05:29', 'melancholic', 'https://open.spotify.com/track/rec4', NULL),
('Electric Feel', '03:48', 'energetic', 'https://open.spotify.com/track/rec5', NULL);

-- Link Recommendations to Artists
INSERT INTO Recommendation_Artist (recId, artistId) VALUES
(1, 1),          -- Redo: Drake
(2, 3),          -- DNA.: Kendrick Lamar
(3, 2),          -- Die For You: The Weeknd
(4, 8),          -- All Too Well: Taylor Swift
(5, 2);          -- Electric Feel: The Weeknd

-- Link Recommendations to Genres
INSERT INTO Recommendation_Genre (recId, genreId) VALUES
(1, 1), (1, 2),  -- Redo: Hip-Hop, R&B
(2, 1),          -- DNA.: Hip-Hop
(3, 5), (3, 2),  -- Die For You: Electronic, R&B
(4, 3),          -- All Too Well: Pop
(5, 5), (5, 3);  -- Electric Feel: Electronic, Pop

-- Insert Recommendation Library Data
INSERT INTO RecommendationLibrary (recommendationSongName, recommendationArtistId) VALUES
('Redo', 1),
('DNA.', 3),
('Die For You', 2),
('All Too Well', 8),
('Electric Feel', 2);
