package ua.music.model;

import ua.music.util.MusicUtils;

import java.time.LocalDate;

public record PlaylistTrack(MusicUser user, Track track, LocalDate addedDate) {
    public PlaylistTrack {
        MusicUtils.requireNotNull(user, "User");
        MusicUtils.requireNotNull(track, "Track");
        MusicUtils.validateAddedDate(addedDate, track.album().releaseDate());
    }
}
