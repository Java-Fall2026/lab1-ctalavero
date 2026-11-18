package ua.music.model;

import ua.music.util.MusicUtils;

import java.time.LocalDate;

public record Album(String upcCode, String title, Artist artist, Genre genre, LocalDate releaseDate) {
    public Album {
        MusicUtils.requireNotBlank(upcCode, "UPC code");
        MusicUtils.requireNotBlank(title, "Title");
        MusicUtils.requireNotNull(artist, "Artist");
        MusicUtils.requireNotNull(genre, "Genre");
        MusicUtils.validateReleaseDate(releaseDate, artist.debutYear());
    }
}
