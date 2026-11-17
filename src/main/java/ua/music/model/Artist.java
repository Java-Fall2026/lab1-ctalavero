package ua.music.model;

import ua.music.util.MusicUtils;

public record Artist(String artistCode, String stageName, String country, int debutYear) {
    public Artist {
        MusicUtils.requireNotBlank(artistCode, "Artist code");
        MusicUtils.requireNotBlank(stageName, "Stage name");
        MusicUtils.requireNotBlank(country, "Country");
        MusicUtils.validateDebutYear(debutYear);
    }
}
