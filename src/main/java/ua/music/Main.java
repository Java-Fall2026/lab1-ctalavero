package ua.music;

import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.model.MusicUser;
import ua.music.model.PlaylistTrack;
import ua.music.model.Subscription;
import ua.music.model.Track;
import ua.music.util.MusicUtils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Artist artist = new Artist("ART-01", "The Midnight", "США", 2012);
        LocalDate releaseDate = LocalDate.of(2016, 8, 5);
        Album album = new Album("ALB-01", "Endless Summer", artist, Genre.ELECTRONIC, releaseDate);
        Track track = new Track("TRK-01", album, "Sunset", 225, 1);
        System.out.println("Трек «" + track.title() + "» з альбому «" + track.album().title() + "»");

        System.out.println("Жанри каталогу:");
        for (Genre genre : Genre.values()) {
            System.out.println(genre.name() + " — " + genre.getDisplayName());
        }
        showInvalid(() -> Genre.valueOf("BLUES"));

        MusicUser user = MusicUser.of("ivan@mail.com", "Ivan", Subscription.FREE);
        System.out.println("Початкова підписка: " + user.getSubscription());
        user.setSubscription(Subscription.FAMILY);
        System.out.println("Оновлена підписка: " + user.getSubscription());
        for (Subscription subscription : Subscription.values()) {
            String offline = MusicUtils.supportsOfflineListening(subscription) ? "доступне" : "недоступне";
            System.out.println(subscription + " — офлайн-прослуховування: " + offline
                    + ", ліміт профілів: " + MusicUtils.profileLimit(subscription));
        }

        showInvalid(() -> new Track("TRK-02", album, "Demo", 0, 1));

        Track duplicate = new Track("TRK-01", album, "Sunset", 225, 1);
        System.out.println("Рівні записи: " + track.equals(duplicate));
        System.out.println("Однакові hashCode: " + (track.hashCode() == duplicate.hashCode()));
        System.out.println("Тривалість треку: " + MusicUtils.formatDuration(track));
        System.out.println("Повних років від релізу: " + MusicUtils.trackAgeYears(track));

        PlaylistTrack entry = new PlaylistTrack(user, track, releaseDate.plusDays(1));
        System.out.println("Запис плейлиста: " + entry);
    }

    private static void showInvalid(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            System.out.println("Відхилено: " + e.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
