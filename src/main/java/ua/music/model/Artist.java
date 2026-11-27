package ua.music.model;

import ua.music.util.MusicUtils;

import java.util.Comparator;

public record Artist(String artistCode, String stageName, String country, int debutYear) implements Comparable<Artist> {
    public Artist {
        MusicUtils.requireNotBlank(artistCode, "Artist code");
        MusicUtils.requireNotBlank(stageName, "Stage name");
        MusicUtils.requireNotBlank(country, "Country");
        MusicUtils.validateDebutYear(debutYear);
    }

    @Override
    public int compareTo(Artist other) {
        return Comparator.comparing(Artist::artistCode)
                .thenComparing(Artist::stageName)
                .thenComparing(Artist::country)
                .thenComparingInt(Artist::debutYear)
                .compare(this, other);
    }
}
