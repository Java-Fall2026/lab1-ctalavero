package ua.music;

import ua.music.io.CatalogLoader;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.model.MusicUser;
import ua.music.model.Subscription;
import ua.music.repository.DuplicateIdentityException;
import ua.music.repository.GenericRepository;

import java.nio.file.Path;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        GenericRepository<Artist> artists = new GenericRepository<>("Artists", artist -> artist.artistCode());
        GenericRepository<MusicUser> users = new GenericRepository<>("Users", user -> user.getAccountEmail());
        GenericRepository<Album> albums = new GenericRepository<>("Albums", album -> album.upcCode());

        for (Artist artist : new CatalogLoader().loadArtists(Path.of("data/artists.csv"))) {
            try {
                artists.add(artist);
            } catch (DuplicateIdentityException e) {
                System.out.println("Дублікат із CSV відхилено: " + e.getMessage());
            }
        }

        MusicUser user = MusicUser.of("ivan@mail.com", "Ivan", Subscription.FREE);
        users.add(user);
        Artist artist = artists.findByIdentity("ART-01").orElseThrow();
        Album album = new Album("ALB-01", "Endless Summer", artist, Genre.ELECTRONIC,
                LocalDate.of(2016, 8, 5));
        albums.add(album);

        System.out.println("Виконавець: " + artists.findByIdentity("ART-01"));
        System.out.println("Користувач: " + users.findByIdentity("ivan@mail.com"));
        System.out.println("Альбом: " + albums.findByIdentity("ALB-01"));
        System.out.println("Відсутній виконавець: " + artists.findByIdentity("ART-MISSING"));
        System.out.println("Відсутній користувач: " + users.findByIdentity("missing@mail.com"));
        System.out.println("Відсутній альбом: " + albums.findByIdentity("ALB-MISSING"));

        try {
            users.add(user);
        } catch (DuplicateIdentityException e) {
            System.out.println("Повторний add відхилено: " + e.getMessage());
        }
        System.out.println("Повторний addIfAbsent: " + users.addIfAbsent(user));
        System.out.println("Кількість користувачів: " + users.size());

        user.setSubscription(Subscription.PREMIUM);
        System.out.println("Після зміни підписки користувач знайдений за тією самою поштою: "
                + users.findByIdentity("ivan@mail.com"));
        System.out.println("Видалення альбому: " + albums.removeByIdentity("ALB-01"));
        System.out.println("Повторне видалення альбому: " + albums.removeByIdentity("ALB-01"));

        try {
            artists.add(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Некоректне додавання відхилено: " + e.getMessage());
        }
        System.out.println("Усього виконавців із CSV: " + artists.size());
    }
}
