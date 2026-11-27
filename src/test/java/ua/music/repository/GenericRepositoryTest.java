package ua.music.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.model.MusicUser;
import ua.music.model.Subscription;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericRepositoryTest {
    private final GenericRepository<Artist> artists =
            new GenericRepository<>("Artists", artist -> artist.artistCode());
    private final Artist artist = new Artist("A", "Artist", "USA", 2000);

    @Test
    @DisplayName("Finds an artist after adding it")
    void addsAndFindsArtist() {
        artists.add(artist);
        assertEquals(artist, artists.findByIdentity("A").orElseThrow());
        assertEquals(1, artists.size());
    }

    @Test
    @DisplayName("Returns an empty Optional for an absent identity")
    void returnsEmptyForMissingIdentity() {
        assertTrue(artists.findByIdentity("MISSING").isEmpty());
    }

    @Test
    @DisplayName("Rejects a duplicate identity and preserves the original item")
    void rejectsDuplicateIdentity() {
        artists.add(artist);
        Artist replacement = new Artist("A", "Another artist", "France", 2010);

        DuplicateIdentityException error = assertThrows(DuplicateIdentityException.class,
                () -> artists.add(replacement));

        assertAll(
                () -> assertTrue(error.getMessage().contains("A")),
                () -> assertTrue(error.getMessage().contains("Artists")),
                () -> assertEquals(1, artists.size()),
                () -> assertSame(artist, artists.findByIdentity("A").orElseThrow()));
    }

    @Test
    @DisplayName("Skips the same item without an exception or a size change")
    void skipsExistingItem() {
        assertTrue(artists.addIfAbsent(artist));
        assertFalse(artists.addIfAbsent(artist));
        assertEquals(1, artists.size());
    }

    @Test
    @DisplayName("Skips a different object with an existing identity")
    void skipsExistingIdentity() {
        artists.add(artist);
        assertFalse(artists.addIfAbsent(new Artist("A", "Other", "France", 2001)));
        assertSame(artist, artists.findByIdentity("A").orElseThrow());
    }

    @Test
    @DisplayName("Removes an existing item and reports an absent item")
    void removesByIdentity() {
        artists.add(artist);
        assertTrue(artists.removeByIdentity("A"));
        assertFalse(artists.removeByIdentity("A"));
        assertEquals(0, artists.size());
        assertTrue(artists.findByIdentity("A").isEmpty());
    }

    @Test
    @DisplayName("Protects repository membership from changes to findAll results")
    void protectsReturnedSnapshot() {
        artists.add(artist);
        List<Artist> snapshot = artists.findAll();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        artists.add(new Artist("B", "Second", "USA", 2002));
        assertEquals(List.of(artist), snapshot);
        assertEquals(2, artists.size());
    }

    @Test
    @DisplayName("Uses the same repository class for artists, users, and albums")
    void supportsThreeDomainTypes() {
        GenericRepository<MusicUser> users = new GenericRepository<>("Users", user -> user.getAccountEmail());
        GenericRepository<Album> albums = new GenericRepository<>("Albums", album -> album.upcCode());
        MusicUser user = MusicUser.of("user@mail.com", "User", Subscription.FREE);
        Album album = new Album("B", "Album", artist, Genre.POP, LocalDate.of(2001, 1, 1));

        artists.add(artist);
        users.add(user);
        albums.add(album);

        assertEquals(artist, artists.findByIdentity("A").orElseThrow());
        assertSame(user, users.findByIdentity("user@mail.com").orElseThrow());
        assertEquals(album, albums.findByIdentity("B").orElseThrow());
    }

    @Test
    @DisplayName("Finds a mutable user after the subscription changes")
    void keepsStableIdentityAfterMutation() {
        GenericRepository<MusicUser> users = new GenericRepository<>("Users", user -> user.getAccountEmail());
        MusicUser user = MusicUser.of("user@mail.com", "User", Subscription.FREE);
        users.add(user);
        user.setSubscription(Subscription.FAMILY);

        assertSame(user, users.findByIdentity("user@mail.com").orElseThrow());
        assertEquals(Subscription.FAMILY, users.findByIdentity("user@mail.com").orElseThrow().getSubscription());
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("Rejects null items and empty identities without changing the repository")
    void rejectsInvalidItems() {
        assertThrows(IllegalArgumentException.class, () -> artists.add(null));
        GenericRepository<String> values = new GenericRepository<>("Values", value -> value);
        assertThrows(IllegalArgumentException.class, () -> values.add(" "));
        assertThrows(IllegalArgumentException.class, () -> values.add(""));
        GenericRepository<Artist> invalidKeys = new GenericRepository<>("Invalid keys", value -> null);
        assertThrows(IllegalArgumentException.class, () -> invalidKeys.add(artist));
        assertEquals(0, artists.size());
        assertEquals(0, values.size());
        assertEquals(0, invalidKeys.size());
    }

    @Test
    @DisplayName("Accepts a subtype collection and a supertype identity extractor")
    void supportsWildcardArguments() {
        IdentityExtractor<CharSequence> extractor = text -> text.toString();
        GenericRepository<CharSequence> values = new GenericRepository<>("Text", extractor);
        List<String> strings = List.of("A", "B");
        values.addAll(strings);
        GenericRepository<String> stringsRepository = new GenericRepository<>("Strings", extractor);
        stringsRepository.addAll(strings);
        assertEquals(2, values.size());
        assertEquals("B", values.findByIdentity("B").orElseThrow());
        assertEquals(2, stringsRepository.size());
    }

    @Test
    @DisplayName("Preserves insertion order when adding a collection")
    void addsCollectionInOrder() {
        Artist second = new Artist("B", "Second", "USA", 2001);
        artists.addAll(List.of(artist, second));
        assertEquals(List.of(artist, second), artists.findAll());
    }
}
