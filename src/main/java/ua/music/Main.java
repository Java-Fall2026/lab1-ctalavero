package ua.music;

import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.MusicUser;
import ua.music.model.PlaylistTrack;
import ua.music.model.Track;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Artist artist = new Artist("ART-01", "The Midnight", "США", 2012);
        LocalDate releaseDate = LocalDate.of(2016, 8, 5);
        Album album = new Album("ALB-01", "Endless Summer", artist, "  electronic ", releaseDate);
        Track track = Track.of("TRK-01", album, "Sunset", 225, 1);
        MusicUser user = MusicUser.of("  Ivan@MAIL.com ", "Ivan", " free ");
        PlaylistTrack entry = new PlaylistTrack(user, track, releaseDate.plusDays(1));

        System.out.println("Створено конструктором: " + artist);
        System.out.println("Створено фабрикою: " + user);
        System.out.println("Жанр «  electronic » → «" + album.getGenre() + "»");
        System.out.println("Пошта «  Ivan@MAIL.com » → «" + user.getAccountEmail() + "»");

        showInvalid(() -> new Artist(" ", "The Midnight", "США", 2012));
        showInvalid(() -> new Album("ALB-02", "Demo", artist, "POP", releaseDate.plusYears(200)));
        showInvalid(() -> Track.of("TRK-02", album, "Demo", 0, 1));

        user.setSubscription(" premium ");
        showInvalid(() -> user.setSubscription("VIP"));
        System.out.println("Підписка після відхиленої зміни: " + user.getSubscription());

        MusicUser sameUser = MusicUser.of("ivan@mail.com", "Ivan", "PREMIUM");
        MusicUser differentUser = MusicUser.of("olena@mail.com", "Olena", "FREE");
        System.out.println("Та сама адреса пошти, ==: " + (user == sameUser));
        System.out.println("Та сама адреса пошти, equals: " + user.equals(sameUser));
        System.out.println("Однакові hashCode: " + (user.hashCode() == sameUser.hashCode()));
        System.out.println("Інша адреса пошти, equals: " + user.equals(differentUser));

        System.out.println("Тривалість треку: " + track.formatDuration());
        System.out.println("Повних років від релізу: " + track.trackAgeYears());

        System.out.println("Сутності каталогу:");
        System.out.println(artist);
        System.out.println(user);
        System.out.println(album);
        System.out.println(track);
        System.out.println(entry);
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
