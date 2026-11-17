package ua.music.model;

import ua.music.util.MusicUtils;

public record Track(String isrcCode, Album album, String title, int durationSeconds, int trackNumber) {
    public Track {
        MusicUtils.requireNotBlank(isrcCode, "ISRC code");
        MusicUtils.requireNotNull(album, "Album");
        MusicUtils.requireNotBlank(title, "Title");
        MusicUtils.validateDurationSeconds(durationSeconds);
        MusicUtils.validateTrackNumber(trackNumber);
    }
}
