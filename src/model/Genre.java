// Genre.java
// Represents a music genre in the music system

package model;

public class Genre {
    private int genreId;
    private String genreName;

    // Constructor for new genre
    public Genre(String genreName) {
        this.genreId = -1;
        this.genreName = genreName;
    }

    // Constructor for loading from database
    public Genre(int genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }

    public int getGenreId() { return genreId; }
    public String getGenreName() { return genreName; }

    public void setGenreId(int genreId) { this.genreId = genreId; }
    public void setGenreName(String genreName) { this.genreName = genreName; }

    @Override
    public String toString() {
        return String.format("Genre { id=%d, name='%s' }", genreId, genreName);
    }
}
