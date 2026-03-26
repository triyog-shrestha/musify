// Album.java
// Represents an album in the music system

package model;

public class Album {
    private int albumId;
    private String albumName;

    // Constructor for new album
    public Album(String albumName) {
        this.albumId = -1;
        this.albumName = albumName;
    }

    // Constructor for loading from database
    public Album(int albumId, String albumName) {
        this.albumId = albumId;
        this.albumName = albumName;
    }

    public int getAlbumId() { return albumId; }
    public String getAlbumName() { return albumName; }

    public void setAlbumId(int albumId) { this.albumId = albumId; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }

    @Override
    public String toString() {
        return String.format("Album { id=%d, name='%s' }", albumId, albumName);
    }
}
