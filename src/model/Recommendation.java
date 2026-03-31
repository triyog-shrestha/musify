/**
 * Represents a song in the admin-curated recommendation pool.
 * Recommendations are suggested to users based on their listening patterns.
 * Similar structure to Song but without play counts.
 * 
 * Multi-valued fields use pipe "|" as separator (artists, genres).
 */
package model;

public class Recommendation {
    private int recId;
    private String trackName;
    private String albumName;
    private String artists;          // Pipe-separated: "Artist1|Artist2"
    private String length;           // Format: "mm:ss"
    private String genres;           // Pipe-separated: "pop|rock"
    private String mood;
    private String link;             // Spotify URL

    /**
     * Creates a new recommendation before database insertion.
     * The recId will be -1 until saved to the database.
     * 
     * @param trackName Name of the track
     * @param albumName Name of the album
     * @param artists   Pipe-separated artist names
     * @param length    Track duration in mm:ss format
     * @param genres    Pipe-separated genre names
     * @param mood      Song mood
     * @param link      Spotify URL (must be unique in recommendations table)
     */
    public Recommendation(String trackName, String albumName,
                          String artists, String length, String genres,
                          String mood, String link) {
        this.recId = -1;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists = artists;
        this.length = length;
        this.genres = genres;
        this.mood = mood;
        this.link = link;
    }

    /**
     * Creates a recommendation instance from database row data.
     * Used by RecommendationDAO when loading recommendations.
     * 
     * @param recId     Unique recommendation identifier
     * @param trackName Name of the track
     * @param albumName Name of the album
     * @param artists   Pipe-separated artist names
     * @param length    Track duration in mm:ss format
     * @param genres    Pipe-separated genre names
     * @param mood      Song mood
     * @param link      Spotify URL
     */
    public Recommendation(int recId, String trackName, String albumName,
                          String artists, String length, String genres,
                          String mood, String link) {
        this.recId = recId;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists = artists;
        this.length = length;
        this.genres = genres;
        this.mood = mood;
        this.link = link;
    }

    // Getters
    public int getRecId() { return recId; }
    public String getTrackName() { return trackName; }
    public String getAlbumName() { return albumName; }
    public String getArtists() { return artists; }
    public String getLength() { return length; }
    public String getGenres() { return genres; }
    public String getMood() { return mood; }
    public String getLink() { return link; }

    @Override
    public String toString() {
        return String.format("[%d] %-35s | %-20s | %-6s | Mood: %-10s | %s",
                recId,
                truncate(trackName, 34),
                truncate(artists.replace("|", ", "), 19),
                length, mood, genres);
    }

    /**
     * Truncates a string and adds ellipsis if it exceeds the maximum length.
     * Used for formatting display strings in console output.
     * 
     * @param s   The string to truncate
     * @param max Maximum length before truncation
     * @return Truncated string with ellipsis, or original if short enough
     */
    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}