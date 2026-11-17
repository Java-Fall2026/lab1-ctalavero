package ua.music.util;

import ua.music.model.Track;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public final class MusicUtils {
    static final int MIN_DEBUT_YEAR = 1900;
    static final int MAX_DURATION_SECONDS = 3600;
    static final int MAX_TRACK_NUMBER = 500;
    static final List<String> SUBSCRIPTIONS = List.of("FREE", "PREMIUM", "FAMILY");
    static final List<String> GENRES = List.of("ROCK", "POP", "JAZZ", "CLASSICAL", "ELECTRONIC", "HIPHOP");

    private MusicUtils() {
    }

    public static <T> T requireNotNull(T value, String fieldName) {
        return ValidationHelper.requireNotNull(value, fieldName);
    }

    public static String requireNotBlank(String value, String fieldName) {
        return ValidationHelper.requireNotBlank(value, fieldName);
    }

    public static int validateDebutYear(int debutYear) {
        return ValidationHelper.requireInRange(debutYear, MIN_DEBUT_YEAR, Year.now().getValue(), "Debut year");
    }

    public static String normalizeEmail(String email) {
        return FormatHelper.normalizeLowercase(ValidationHelper.requireNotBlank(email, "Account email"));
    }

    public static String normalizeSubscription(String subscription) {
        return ValidationHelper.requireChoice(subscription, SUBSCRIPTIONS, "Subscription");
    }

    public static String normalizeGenre(String genre) {
        return ValidationHelper.requireChoice(genre, GENRES, "Genre");
    }

    public static LocalDate validateReleaseDate(LocalDate releaseDate, int debutYear) {
        ValidationHelper.requireNotNull(releaseDate, "Release date");
        LocalDate latest = LocalDate.now();
        LocalDate earliest = LocalDate.of(debutYear, 1, 1);
        if (releaseDate.isBefore(earliest) || releaseDate.isAfter(latest)) {
            throw new IllegalArgumentException(
                    "Release date must be between " + earliest + " and " + latest + ", got: " + releaseDate);
        }
        return releaseDate;
    }

    public static int validateDurationSeconds(int durationSeconds) {
        return ValidationHelper.requireInRange(durationSeconds, 1, MAX_DURATION_SECONDS, "Duration in seconds");
    }

    public static int validateTrackNumber(int trackNumber) {
        return ValidationHelper.requireInRange(trackNumber, 1, MAX_TRACK_NUMBER, "Track number");
    }

    public static LocalDate validateAddedDate(LocalDate addedDate, LocalDate releaseDate) {
        ValidationHelper.requireNotNull(addedDate, "Added date");
        if (addedDate.isBefore(releaseDate)) {
            throw new IllegalArgumentException("Added date must be on or after " + releaseDate + ", got: " + addedDate);
        }
        return addedDate;
    }

    public static String formatDuration(Track track) {
        int seconds = track.getDurationSeconds();
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    public static long trackAgeYears(Track track) {
        return ChronoUnit.YEARS.between(track.getAlbum().getReleaseDate(), LocalDate.now());
    }
}
