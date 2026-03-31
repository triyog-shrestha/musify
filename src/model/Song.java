/**
 * Represents a song in a user's music library.
 * Contains song metadata, play count, and links to Spotify.
 * 
 * Multi-valued fields (artists, genres) use pipe "|" as separator.
 * Example: "Artist1|Artist2|Artist3" or "pop|rock|electronic"
 * 
 * Mood is calculated automatically from Spotify audio features during import.
 */
package model;

public class Song {
    private int songId;
    private String trackName;
    private String albumName;
    private String artists;          // Pipe-separated: "Artist1|Artist2"
    private String length;           // Format: "mm:ss"
    private String genres;           // Pipe-separated: "pop|rock"
    private String mood;
    private String link;             // Spotify URL
    private int playCount;
    private int totalArtists;

    /**
     * Creates a new song from imported CSV data (basic version).
     * Used during CSV import when totalArtists is not yet calculated.
     * 
     * @param trackName Name of the track
     * @param albumName Name of the album
     * @param artists   Pipe-separated artist names
     * @param length    Track duration in mm:ss format
     * @param genres    Pipe-separated genre names
     * @param mood      Calculated mood (ENERGETIC, HAPPY, FOCUSED, RELAXED, MELANCHOLIC)
     * @param link      Spotify URL
     */
    public Song(String trackName, String albumName, String artists,
                String length, String genres, String mood, String link) {
        this(trackName, albumName, artists, 0, length, genres, mood, link);
    }

    /**
     * Creates a new song with a known artist count.
     * 
     * @param trackName    Name of the track
     * @param albumName    Name of the album
     * @param artists      Pipe-separated artist names
     * @param totalArtists Number of unique artists
     * @param length       Track duration in mm:ss format
     * @param genres       Pipe-separated genre names
     * @param mood         Calculated mood
     * @param link         Spotify URL
     */
    public Song(String trackName, String albumName, String artists,
                int totalArtists, String length, String genres, String mood, String link) {
        this.songId = -1;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists = artists;
        this.totalArtists = totalArtists;
        this.length = length;
        this.genres = genres;
        this.mood = mood;
        this.link = link;
        this.playCount = 0;
    }

    /**
     * Creates a song instance from database row (basic version).
     * 
     * @param songId    Unique song identifier
     * @param trackName Name of the track
     * @param albumName Name of the album
     * @param artists   Pipe-separated artist names
     * @param length    Track duration in mm:ss format
     * @param genres    Pipe-separated genre names
     * @param mood      Song mood
     * @param link      Spotify URL
     * @param playCount Number of times played
     */
    public Song(int songId, String trackName, String albumName, String artists,
                String length, String genres, String mood, String link, int playCount) {
        this(songId, trackName, albumName, artists, 0, length, genres, mood, link, playCount);
    }

    /**
     * Creates a complete song instance with all fields.
     * This is the most comprehensive constructor, used internally by other constructors.
     * 
     * @param songId       Unique song identifier
     * @param trackName    Name of the track
     * @param albumName    Name of the album
     * @param artists      Pipe-separated artist names
     * @param totalArtists Number of unique artists
     * @param length       Track duration in mm:ss format
     * @param genres       Pipe-separated genre names
     * @param mood         Song mood
     * @param link         Spotify URL
     * @param playCount    Number of times played
     */
    public Song(int songId, String trackName, String albumName, String artists,
                int totalArtists, String length, String genres, String mood, String link, int playCount) {
        this.songId = songId;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists = artists;
        this.totalArtists = totalArtists;
        this.length = length;
        this.genres = genres;
        this.mood = mood;
        this.link = link;
        this.playCount = playCount;
    }

    // Getters
    public int getSongId() { return songId; }
    public String getTrackName() { return trackName; }
    public String getAlbumName() { return albumName; }
    public String getArtists() { return artists; }
    public String getLength() { return length; }
    public String getGenres() { return genres; }
    public String getMood() { return mood; }
    public String getLink() { return link; }
    public int getPlayCount() { return playCount; }
    public int getTotalArtists() { return totalArtists; }

    // Setters
    public void setSongId(int songId) { this.songId = songId; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    public void setTotalArtists(int totalArtists) { this.totalArtists = totalArtists; }

    @Override
    public String toString() {
        return String.format("[%d] %-35s | %-20s | %-6s | Plays: %d | Mood: %s",
                songId,
                truncate(trackName, 34),
                truncate(artists.replace("|", ", "), 19),
                length, playCount, mood);
    }

    /**
     * Truncates a string and adds ellipsis if it exceeds the maximum length.
     * Used for formatting song/artist names in console displays.
     * 
     * @param s   The string to truncate
     * @param max Maximum length before truncation
     * @return Truncated string with ellipsis, or original if short enough
     */
    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}