package ua.music.model;

import ua.music.util.MusicUtils;

import java.time.LocalDate;
import java.util.Comparator;

public record Album(String upcCode, String title, Artist artist, Genre genre, LocalDate releaseDate) implements Comparable<Album> {
    public Album {
        MusicUtils.requireNotBlank(upcCode, "UPC code");
        MusicUtils.requireNotBlank(title, "Title");
        MusicUtils.requireNotNull(artist, "Artist");
        MusicUtils.requireNotNull(genre, "Genre");
        MusicUtils.validateReleaseDate(releaseDate, artist.debutYear());
    }

    @Override
    public int compareTo(Album other) {
        return Comparator.comparing(Album::upcCode)
                .thenComparing(Album::title)
                .thenComparing(Album::artist)
                .thenComparing(Album::genre)
                .thenComparing(Album::releaseDate)
                .compare(this, other);
    }
}
