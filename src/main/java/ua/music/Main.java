package ua.music;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.config.AppConfig;
import ua.music.io.DataLoader;
import ua.music.io.FileStorage;
import ua.music.io.LoadResult;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.repository.AlbumRepository;
import ua.music.repository.ArtistRepository;
import ua.music.repository.GenericRepository;
import ua.music.repository.IdentityExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            DataLoader loader = new DataLoader(factory.getValidator());
            ArtistRepository artists = new ArtistRepository();
            AlbumRepository albums = new AlbumRepository();
            FileStorage<Artist> artistStorage = new FileStorage<>(Artist.class);
            FileStorage<Album> albumStorage = new FileStorage<>(Album.class);

            showResult("Виконавці з JSON", loader.load(config.path("artists.input"), artistStorage, artists));
            showResult("Альбоми з YAML", loader.load(config.path("albums.input"), albumStorage, albums));
            showResult("Синтаксично пошкоджений JSON",
                    loader.load(config.path("broken.input"), artistStorage, artists));
            showResult("Відсутній файл",
                    loader.load(config.path("missing.input"), artistStorage, artists));

            artists.removeByIdentity("ART-01");
            albums.removeByIdentity("ALB-01");
            saveAndReload(config.path("artists.output"), artistStorage, artists, loader, Artist::artistCode);
            saveAndReload(config.path("albums.output"), albumStorage, albums, loader, Album::upcCode);
        }
        System.out.println("Демонстрацію завершено.");
    }

    private static void showResult(String label, LoadResult result) {
        System.out.println(label + ": прочитано " + result.read() + ", додано " + result.added()
                + ", відкинуто " + result.rejected().size());
        result.rejected().forEach(record -> System.out.println(
                "Відхилено запис " + record.recordNumber() + ": " + String.join("; ", record.errors())));
    }

    private static <T> void saveAndReload(Path file, FileStorage<T> storage, GenericRepository<T> repository,
                                         DataLoader loader,
                                         IdentityExtractor<T> extractor) {
        List<T> expected = repository.findAll();
        try {
            storage.save(file, expected);
            GenericRepository<T> restored = new GenericRepository<>("Restored catalog", extractor);
            LoadResult result = loader.load(file, storage, restored);
            showResult("Повторне завантаження " + file, result);
            System.out.println("Збережений і відновлений вміст збігаються: "
                    + expected.equals(restored.findAll()));
        } catch (IOException e) {
            log.error("Cannot save file {}: {}", file, e.getMessage());
            System.out.println("Збереження відхилено: " + e.getMessage());
        }
    }
}
