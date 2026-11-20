package ua.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.io.CatalogLoader;
import ua.music.model.Artist;
import ua.music.model.MusicUser;
import ua.music.model.Subscription;

import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        CatalogLoader loader = new CatalogLoader();
        List<Artist> artists = loader.loadArtists(Path.of("data/artists.csv"));
        List<MusicUser> users = loader.loadUsers(Path.of("data/users.csv"));
        System.out.println("Завантажено виконавців: " + artists.size());
        artists.forEach(System.out::println);
        System.out.println("Завантажено користувачів: " + users.size());
        users.forEach(System.out::println);

        List<Artist> missing = loader.loadArtists(Path.of("data/missing-artists.csv"));
        System.out.println("Після спроби читання відсутнього файла програма працює, записів: " + missing.size());

        MusicUser user = MusicUser.of("demo@mail.com", "Demo", Subscription.PREMIUM);
        try {
            user.setSubscription(null);
        } catch (IllegalArgumentException e) {
            log.warn("Rejected subscription change: {}", e.getMessage(), e);
            System.out.println("Некоректну зміну підписки відхилено: " + e.getMessage());
        }
        System.out.println("Підписка після помилки: " + user.getSubscription());
        log.info("Catalog demonstration completed");
    }
}
