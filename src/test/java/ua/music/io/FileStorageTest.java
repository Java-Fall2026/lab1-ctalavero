package ua.music.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {
    @TempDir
    Path directory;

    private final Artist artist = new Artist("A", "Океан Ельзи", "Ukraine", 1994);

    @ParameterizedTest
    @ValueSource(strings = {"json", "yaml", "yml"})
    @DisplayName("Round trips artist records through every supported format")
    void roundTripsArtists(String extension) throws IOException {
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        Path file = directory.resolve("artists." + extension);
        List<Artist> expected = List.of(artist);
        storage.save(file, expected);
        assertEquals(expected, storage.load(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"json", "yaml"})
    @DisplayName("Round trips nested albums and writes dates as ISO strings")
    void roundTripsAlbumsWithIsoDates(String extension) throws IOException {
        Album album = new Album("B", "Album", artist, Genre.ROCK, LocalDate.of(2005, 9, 22));
        FileStorage<Album> storage = new FileStorage<>(Album.class);
        Path file = directory.resolve("albums." + extension);
        storage.save(file, List.of(album));
        assertEquals(List.of(album), storage.load(file));
        ObjectMapper mapper = extension.equals("json") ? new ObjectMapper() : new ObjectMapper(new YAMLFactory());
        var date = mapper.readTree(file.toFile()).get(0).get("releaseDate");
        assertTrue(date.isTextual());
        assertEquals("2005-09-22", date.asText());
    }

    @Test
    @DisplayName("Rejects an unsupported extension")
    void rejectsUnknownExtension() throws IOException {
        Path file = directory.resolve("artists.txt");
        Files.writeString(file, "[]");
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        assertThrows(IllegalArgumentException.class, () -> storage.load(file));
        assertThrows(IllegalArgumentException.class, () -> storage.save(file, List.of(artist)));
    }

    @Test
    @DisplayName("Rejects a root value that is not an array")
    void rejectsNonArrayRoot() throws IOException {
        Path file = directory.resolve("artists.json");
        Files.writeString(file, "{}");
        assertThrows(IOException.class, () -> new FileStorage<>(Artist.class).load(file));
    }

    @Test
    @DisplayName("Rejects malformed syntax and extra top level data")
    void rejectsInvalidSyntax() throws IOException {
        Path file = directory.resolve("artists.json");
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        Files.writeString(file, "[{");
        assertThrows(IOException.class, () -> storage.load(file));
        Files.writeString(file, "[] []");
        assertThrows(IOException.class, () -> storage.load(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"json", "yaml"})
    @DisplayName("Round trips an empty collection")
    void roundTripsEmptyCollection(String extension) throws IOException {
        Path file = directory.resolve("empty." + extension);
        FileStorage<Artist> storage = new FileStorage<>(Artist.class);
        storage.save(file, List.of());
        assertTrue(storage.load(file).isEmpty());
    }

    public static class SelfReference {
        public SelfReference getSelf() {
            return this;
        }
    }

    @Test
    @DisplayName("Preserves the target file when serialization fails")
    void preservesExistingFileOnFailure() throws IOException {
        Path file = directory.resolve("existing.json");
        Files.writeString(file, "original content");
        FileStorage<SelfReference> storage = new FileStorage<>(SelfReference.class);
        assertThrows(IOException.class, () -> storage.save(file, List.of(new SelfReference())));
        assertEquals("original content", Files.readString(file));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(file), files.toList());
        }
    }
}
