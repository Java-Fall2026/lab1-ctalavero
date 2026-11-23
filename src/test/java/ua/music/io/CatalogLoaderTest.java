package ua.music.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.music.model.Artist;
import ua.music.model.MusicUser;
import ua.music.model.Subscription;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogLoaderTest {
    private final CatalogLoader loader = new CatalogLoader();

    @TempDir
    Path directory;

    @Test
    void parsesValidArtist() throws InvalidDataException {
        Artist artist = loader.parseArtist("ART-01,The Midnight,USA,2012", 2);
        assertEquals(new Artist("ART-01", "The Midnight", "USA", 2012), artist);
    }

    @Test
    void parsesAndNormalizesUser() throws InvalidDataException {
        MusicUser user = loader.parseUser("  Ivan@MAIL.com , Ivan , premium ", 3);
        assertAll(
                () -> assertEquals("ivan@mail.com", user.getAccountEmail()),
                () -> assertEquals("Ivan", user.getUsername()),
                () -> assertEquals(Subscription.PREMIUM, user.getSubscription()));
    }

    @Test
    void wrapsInvalidNumberWithLineAndCause() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> loader.parseArtist("A,Artist,USA,not-a-year", 7));
        assertTrue(error.getMessage().contains("Line 7:"));
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    @Test
    void wrapsConstructorValidationWithLineAndCause() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> loader.parseArtist("A,Artist,USA,1899", 8));
        assertTrue(error.getMessage().contains("Line 8:"));
        assertTrue(error.getMessage().contains("1899"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @Test
    void rejectsWrongColumnCount() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> loader.parseArtist("A,Artist,USA", 4));
        assertTrue(error.getMessage().contains("Line 4:"));
        assertTrue(error.getMessage().contains("Expected 4 columns, got: 3"));
    }

    @Test
    void rejectsInvalidSubscription() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> loader.parseUser("a@mail.com,User,VIP", 5));
        assertTrue(error.getMessage().contains("Line 5:"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @Test
    void preservesEmptyTrailingColumnForValidation() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> loader.parseUser("a@mail.com,User,", 6));
        assertTrue(error.getMessage().contains("Line 6:"));
        assertFalse(error.getMessage().contains("Expected 3 columns"));
    }

    @Test
    void continuesAfterDifferentInvalidRowsAndReadsUtf8() throws Exception {
        Path csv = directory.resolve("artists.csv");
        Files.writeString(csv, """
                artistCode,stageName,country,debutYear
                A,First,USA,2000
                B,Bad number,USA,abc
                C,Bad range,USA,1899
                D,Missing
                E,Океан Ельзи,Ukraine,1994
                """, StandardCharsets.UTF_8);
        List<Artist> artists = loader.loadArtists(csv);
        assertEquals(List.of(
                new Artist("A", "First", "USA", 2000),
                new Artist("E", "Океан Ельзи", "Ukraine", 1994)), artists);
    }

    @Test
    void continuesLoadingUsersAfterInvalidRow() throws Exception {
        Path csv = directory.resolve("users.csv");
        Files.writeString(csv, """
                accountEmail,username,subscription
                a@mail.com,A,FREE
                bad@mail.com,Bad,VIP
                b@mail.com,B,FAMILY
                """);
        List<MusicUser> users = loader.loadUsers(csv);
        assertEquals(2, users.size());
        assertEquals(Subscription.FAMILY, users.get(1).getSubscription());
    }

    @Test
    void missingFileReturnsEmptyList() {
        assertTrue(loader.loadArtists(directory.resolve("missing.csv")).isEmpty());
    }

    @Test
    void invalidHeaderReturnsEmptyList() throws Exception {
        Path csv = directory.resolve("wrong-header.csv");
        Files.writeString(csv, "wrong,header\nA,Artist,USA,2000\n");
        assertTrue(loader.loadArtists(csv).isEmpty());
    }

    @Test
    void emptyFileReturnsEmptyList() throws Exception {
        Path csv = Files.createFile(directory.resolve("empty.csv"));
        assertTrue(loader.loadArtists(csv).isEmpty());
    }

    @Test
    void invalidSetterPreservesSubscription() {
        MusicUser user = MusicUser.of("a@mail.com", "User", Subscription.PREMIUM);
        assertThrows(IllegalArgumentException.class, () -> user.setSubscription(null));
        assertEquals(Subscription.PREMIUM, user.getSubscription());
    }

    @Test
    void exceptionSupportsMessageAndOptionalCause() {
        InvalidDataException error = new InvalidDataException("Line 9: invalid row");
        assertEquals("Line 9: invalid row", error.getMessage());
        assertNull(error.getCause());
        assertFalse(RuntimeException.class.isAssignableFrom(InvalidDataException.class));
    }
}
