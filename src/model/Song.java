
package model;

public class Song {

    private int    songId;
    private String trackName;
    private String albumName;
    private String artists;   // pipe-separated e.g. "Drake|Future"
    private String length;    // mm:ss format
    private String genres;    // pipe-separated e.g. "rap|r&b"
    private String mood;
    private String link;
    private int    playCount;
    private int    totalArtists;

    // Constructor for a NEW song from import
    public Song(String trackName, String albumName, String artists,
                String length, String genres, String mood, String link) {
        this(trackName, albumName, artists, 0, length, genres, mood, link);
    }

    public Song(String trackName, String albumName, String artists,
                int totalArtists, String length, String genres, String mood, String link) {
        this.songId     = -1;
        this.trackName  = trackName;
        this.albumName  = albumName;
        this.artists    = artists;
        this.totalArtists = totalArtists;
        this.length     = length;
        this.genres     = genres;
        this.mood       = mood;
        this.link       = link;
        this.playCount  = 0;
    }

    // Constructor for loading an EXISTING song from database
    public Song(int songId, String trackName, String albumName, String artists,
                String length, String genres, String mood, String link, int playCount) {
        this(songId, trackName, albumName, artists, 0, length, genres, mood, link, playCount);
    }

    public Song(int songId, String trackName, String albumName, String artists,
                int totalArtists, String length, String genres, String mood, String link, int playCount) {
        this.songId    = songId;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artists   = artists;
        this.totalArtists = totalArtists;
        this.length    = length;
        this.genres    = genres;
        this.mood      = mood;
        this.link      = link;
        this.playCount = playCount;
    }

    // Getters
    public int    getSongId()    { return songId; }
    public String getTrackName() { return trackName; }
    public String getAlbumName() { return albumName; }
    public String getArtists()   { return artists; }
    public String getLength()    { return length; }
    public String getGenres()    { return genres; }
    public String getMood()      { return mood; }
    public String getLink()      { return link; }
    public int    getPlayCount() { return playCount; }
    public int    getTotalArtists() { return totalArtists; }

    // Setters
    public void setSongId(int songId)       { this.songId = songId; }
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

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}