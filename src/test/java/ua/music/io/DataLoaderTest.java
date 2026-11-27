package ua.music.io;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.repository.AlbumRepository;
import ua.music.repository.ArtistRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataLoaderTest {
    private static ValidatorFactory factory;

    @TempDir
    Path directory;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    @DisplayName("Keeps valid records and reports every rejection and violation")
    void loadsValidRecordsAndAllViolations() throws IOException {
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        Artist first = new Artist("A", "First", "USA", 2000);
        Artist last = new Artist("C", "Last", "UK", 2001);
        Path file = directory.resolve("artists.json");
        storage.save(file, Arrays.asList(first, new Artist("B", "", "123", null),
                new Artist("A", "Duplicate", "USA", 2000), null,
                new Artist("", "Missing code", "USA", 2000), last));
        ArtistRepository repository = new ArtistRepository();

        LoadResult result = new DataLoader(factory.getValidator()).load(file, storage, repository);

        assertEquals(6, result.read());
        assertEquals(2, result.added());
        assertEquals(4, result.rejected().size());
        assertEquals(List.of(first, last), repository.findAll());
        RejectedRecord multipleErrors = result.rejected().getFirst();
        assertEquals(2, multipleErrors.recordNumber());
        assertEquals(3, multipleErrors.errors().size());
        assertTrue(multipleErrors.errors().stream().anyMatch(error -> error.startsWith("stageName:")));
        assertTrue(multipleErrors.errors().stream().anyMatch(error -> error.startsWith("country:")));
        assertTrue(multipleErrors.errors().stream().anyMatch(error -> error.startsWith("debutYear:")));
        assertEquals(3, result.rejected().get(1).recordNumber());
        assertTrue(result.rejected().get(1).errors().getFirst().startsWith("<identity>:"));
    }

    @Test
    @DisplayName("Rejects duplicates against both existing and newly loaded identities")
    void rejectsRepeatedLoad() throws IOException {
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        Path file = directory.resolve("artists.yaml");
        storage.save(file, List.of(artist, artist));
        DataLoader loader = new DataLoader(factory.getValidator());
        ArtistRepository repository = new ArtistRepository();
        LoadResult first = assertDoesNotThrow(() -> loader.load(file, storage, repository));
        LoadResult second = assertDoesNotThrow(() -> loader.load(file, storage, repository));
        assertEquals(1, first.added());
        assertEquals(1, first.rejected().size());
        assertEquals(0, second.added());
        assertEquals(2, second.rejected().size());
        assertEquals(1, repository.size());
    }

    @Test
    @DisplayName("Continues after an object mapping error")
    void continuesAfterMappingError() throws IOException {
        Path file = directory.resolve("artists.json");
        Files.writeString(file, """
                [
                  {"artistCode":"A","stageName":"Bad year","country":"USA","debutYear":"oops"},
                  {"artistCode":"B","stageName":"Valid","country":"UK","debutYear":2000}
                ]
                """);
        ArtistRepository repository = new ArtistRepository();
        LoadResult result = new DataLoader(factory.getValidator()).load(file,
                new FileStorage<>(Artist.class), repository);
        assertEquals(2, result.read());
        assertEquals(1, result.added());
        assertEquals(1, result.rejected().size());
        assertEquals(1, result.rejected().getFirst().recordNumber());
        assertTrue(result.rejected().getFirst().errors().getFirst().startsWith("debutYear:"));
        assertFalse(result.rejected().getFirst().errors().getFirst().contains("\n"));
        assertEquals("B", repository.findAll().getFirst().artistCode());
    }

    @Test
    @DisplayName("Loads valid YAML albums and rejects a cross field violation")
    void validatesYamlAlbums() throws IOException {
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        Album valid = new Album("B", "Valid", artist, Genre.POP, LocalDate.of(2020, 1, 1));
        Album invalid = new Album("C", "Early", artist, Genre.POP, LocalDate.of(1999, 1, 1));
        Path file = directory.resolve("albums.yml");
        FileStorage<Album> storage = new FileStorage<>(Album.class);
        storage.save(file, List.of(invalid, valid));
        AlbumRepository repository = new AlbumRepository();
        LoadResult result = new DataLoader(factory.getValidator()).load(file, storage, repository);
        assertEquals(2, result.read());
        assertEquals(1, result.added());
        assertEquals(1, result.rejected().size());
        assertTrue(result.rejected().getFirst().errors().getFirst().startsWith("releaseDate:"));
        assertEquals(List.of(valid), repository.findAll());
    }

    @Test
    @DisplayName("Reports broken and missing files and continues with another file")
    void recoversFromFileFailures() throws IOException {
        DataLoader loader = new DataLoader(factory.getValidator());
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        ArtistRepository repository = new ArtistRepository();
        Path broken = directory.resolve("broken.json");
        Files.writeString(broken, "[{");
        LoadResult brokenResult = assertDoesNotThrow(() -> loader.load(broken, storage, repository));
        LoadResult missingResult = assertDoesNotThrow(
                () -> loader.load(directory.resolve("missing.json"), storage, repository));
        assertEquals(0, brokenResult.read());
        assertFalse(brokenResult.rejected().getFirst().errors().getFirst().contains("\n"));
        assertEquals(1, brokenResult.rejected().size());
        assertEquals(0, brokenResult.rejected().getFirst().recordNumber());
        assertEquals(1, missingResult.rejected().size());

        Path valid = directory.resolve("valid.json");
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        storage.save(valid, List.of(artist));
        assertEquals(1, loader.load(valid, storage, repository).added());
    }

    @Test
    @DisplayName("Protects the rejection list and error details from mutation")
    void protectsLoadResults() {
        RejectedRecord record = new RejectedRecord(1, List.of("field: error"));
        LoadResult result = new LoadResult(1, 0, List.of(record));
        assertThrows(UnsupportedOperationException.class, () -> result.rejected().clear());
        assertThrows(UnsupportedOperationException.class, () -> record.errors().clear());
    }
}
