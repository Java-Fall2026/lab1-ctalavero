package ua.music.model;

import ua.common.BaseEntity;
import ua.music.util.MusicUtils;

import java.time.LocalDate;

public class Album extends BaseEntity {
    private final String upcCode;
    private final String title;
    private final Artist artist;
    private final String genre;
    private final LocalDate releaseDate;

    public Album(String upcCode, String title, Artist artist, String genre, LocalDate releaseDate) {
        super();
        this.upcCode = MusicUtils.requireNotBlank(upcCode, "UPC code");
        this.title = MusicUtils.requireNotBlank(title, "Title");
        this.artist = MusicUtils.requireNotNull(artist, "Artist");
        this.genre = MusicUtils.normalizeGenre(genre);
        this.releaseDate = MusicUtils.validateReleaseDate(releaseDate, artist.getDebutYear());
    }

    public String getUpcCode() {
        return upcCode;
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Album that = (Album) other;
        return upcCode.equals(that.upcCode);
    }

    @Override
    public int hashCode() {
        return upcCode.hashCode();
    }

    @Override
    public String toString() {
        return "Album{"
                + "upcCode=" + upcCode
                + ", title=" + title
                + ", artist=" + artist
                + ", genre=" + genre
                + ", releaseDate=" + releaseDate
                + ", createdAt=" + createdAt
                + '}';
    }
}
