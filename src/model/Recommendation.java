// Recommendation.java
// Holds data for one song in the recommendation library.

package model;

public class Recommendation {

    private int    recId;
    private String trackName;
    private String albumName;
    private String artists;
    private String length;
    private String genres;
    private String mood;
    private String link;

    // Constructor for NEW recommendations before assigning an ID
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

    // Constructor for loading from database
    public Recommendation(int recId, String trackName, String albumName,
                          String artists, String length, String genres,
                          String mood, String link) {
        this.recId     = recId;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists   = artists;
        this.length    = length;
        this.genres    = genres;
        this.mood      = mood;
        this.link      = link;
    }

    // Getters
    public int    getRecId()     { return recId; }
    public String getTrackName() { return trackName; }
    public String getAlbumName() { return albumName; }
    public String getArtists()   { return artists; }
    public String getLength()    { return length; }
    public String getGenres()    { return genres; }
    public String getMood()      { return mood; }
    public String getLink()      { return link; }

    @Override
    public String toString() {
        return String.format("[%d] %-35s | %-20s | %-6s | Mood: %-10s | %s",
                recId,
                truncate(trackName, 34),
                truncate(artists.replace("|", ", "), 19),
                length, mood, genres);
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}