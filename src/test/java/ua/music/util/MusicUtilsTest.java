package ua.music.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.model.Subscription;
import ua.music.model.Track;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MusicUtilsTest {
    private Track track(LocalDate releaseDate, int seconds) {
        Artist artist = new Artist("A", "Artist", "USA", 1900);
        Album album = new Album("B", "Album", artist, Genre.POP, releaseDate);
        return new Track("T", album, "Track", seconds, 1);
    }

    @Test
    @DisplayName("Formats track durations including the minimum and maximum")
    void formatsDurationIncludingBoundaries() {
        LocalDate today = LocalDate.now();
        assertAll(
                () -> assertEquals("0:01", MusicUtils.formatDuration(track(today, 1))),
                () -> assertEquals("3:45", MusicUtils.formatDuration(track(today, 225))),
                () -> assertEquals("60:00", MusicUtils.formatDuration(track(today, 3600))));
    }

    @Test
    @DisplayName("Counts only completed years since the album release")
    void countsCompletedYearsSinceRelease() {
        LocalDate today = LocalDate.now();
        assertAll(
                () -> assertEquals(0, MusicUtils.trackAgeYears(track(today, 225))),
                () -> assertEquals(2, MusicUtils.trackAgeYears(track(today.minusYears(2), 225))),
                () -> assertEquals(1, MusicUtils.trackAgeYears(track(today.minusYears(2).plusDays(1), 225))));
    }

    @Test
    @DisplayName("Applies offline listening and profile limits for each subscription")
    void appliesSubscriptionRules() {
        assertAll(
                () -> assertFalse(MusicUtils.supportsOfflineListening(Subscription.FREE)),
                () -> assertTrue(MusicUtils.supportsOfflineListening(Subscription.PREMIUM)),
                () -> assertTrue(MusicUtils.supportsOfflineListening(Subscription.FAMILY)),
                () -> assertEquals(1, MusicUtils.profileLimit(Subscription.FREE)),
                () -> assertEquals(1, MusicUtils.profileLimit(Subscription.PREMIUM)),
                () -> assertEquals(6, MusicUtils.profileLimit(Subscription.FAMILY)));
    }
}
