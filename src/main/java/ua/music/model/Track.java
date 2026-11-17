package ua.music.model;

import ua.common.BaseEntity;
import ua.music.util.MusicUtils;

public class Track extends BaseEntity {
    private final String isrcCode;
    private final Album album;
    private final String title;
    private final int durationSeconds;
    private final int trackNumber;

    private Track(String isrcCode, Album album, String title, int durationSeconds, int trackNumber) {
        super();
        this.isrcCode = MusicUtils.requireNotBlank(isrcCode, "ISRC code");
        this.album = MusicUtils.requireNotNull(album, "Album");
        this.title = MusicUtils.requireNotBlank(title, "Title");
        this.durationSeconds = MusicUtils.validateDurationSeconds(durationSeconds);
        this.trackNumber = MusicUtils.validateTrackNumber(trackNumber);
    }

    public static Track of(String isrcCode, Album album, String title, int durationSeconds, int trackNumber) {
        return new Track(isrcCode, album, title, durationSeconds, trackNumber);
    }

    public String getIsrcCode() {
        return isrcCode;
    }

    public Album getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public String formatDuration() {
        return MusicUtils.formatDuration(this);
    }

    public long trackAgeYears() {
        return MusicUtils.trackAgeYears(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Track that = (Track) other;
        return isrcCode.equals(that.isrcCode);
    }

    @Override
    public int hashCode() {
        return isrcCode.hashCode();
    }

    @Override
    public String toString() {
        return "Track{"
                + "isrcCode=" + isrcCode
                + ", album=" + album
                + ", title=" + title
                + ", durationSeconds=" + durationSeconds
                + ", trackNumber=" + trackNumber
                + ", createdAt=" + createdAt
                + '}';
    }
}
