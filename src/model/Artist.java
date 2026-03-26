// Artist.java
// Represents an artist in the music system

package model;

public class Artist {
    private int artistId;
    private String artistName;

    // Constructor for new artist
    public Artist(String artistName) {
        this.artistId = -1;
        this.artistName = artistName;
    }

    // Constructor for loading from database
    public Artist(int artistId, String artistName) {
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public int getArtistId() { return artistId; }
    public String getArtistName() { return artistName; }

    public void setArtistId(int artistId) { this.artistId = artistId; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    @Override
    public String toString() {
        return String.format("Artist { id=%d, name='%s' }", artistId, artistName);
    }
}
