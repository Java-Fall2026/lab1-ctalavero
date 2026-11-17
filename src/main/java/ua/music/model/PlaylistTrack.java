package ua.music.model;

import ua.common.BaseEntity;
import ua.music.util.MusicUtils;

import java.time.LocalDate;
import java.util.Objects;

public class PlaylistTrack extends BaseEntity {
    private final MusicUser user;
    private final Track track;
    private final LocalDate addedDate;

    public PlaylistTrack(MusicUser user, Track track, LocalDate addedDate) {
        super();
        this.user = MusicUtils.requireNotNull(user, "User");
        this.track = MusicUtils.requireNotNull(track, "Track");
        this.addedDate = MusicUtils.validateAddedDate(addedDate, track.getAlbum().getReleaseDate());
    }

    public MusicUser getUser() {
        return user;
    }

    public Track getTrack() {
        return track;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    // The same user can add the track again on another date.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PlaylistTrack that = (PlaylistTrack) other;
        return user.equals(that.user) && track.equals(that.track) && addedDate.equals(that.addedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, track, addedDate);
    }

    @Override
    public String toString() {
        return "PlaylistTrack{"
                + "user=" + user
                + ", track=" + track
                + ", addedDate=" + addedDate
                + ", createdAt=" + createdAt
                + '}';
    }
}
