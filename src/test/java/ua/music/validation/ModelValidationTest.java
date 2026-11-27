package ua.music.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModelValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    static Stream<Arguments> invalidArtists() {
        return Stream.of(
                Arguments.of(new Artist("", "Artist", "USA", 2000), "artistCode", NotBlank.class),
                Arguments.of(new Artist("A", "X".repeat(121), "USA", 2000), "stageName", Size.class),
                Arguments.of(new Artist("A", "Artist", "123", 2000), "country", Pattern.class),
                Arguments.of(new Artist("A", "Artist", "USA", null), "debutYear", NotNull.class),
                Arguments.of(new Artist("A", "Artist", "USA", 1899), "debutYear", Min.class));
    }

    @ParameterizedTest
    @MethodSource("invalidArtists")
    @DisplayName("Reports the expected field for every standard artist constraint")
    void validatesArtistConstraints(Artist artist, String field, Class<? extends Annotation> annotation) {
        assertTrue(validator.validate(artist).stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals(field)
                        && violation.getConstraintDescriptor().getAnnotation().annotationType().equals(annotation)));
        assertTrue(validator.validate(artist).stream().allMatch(
                violation -> violation.getMessage().matches(".*[А-Яа-яІіЇїЄєҐґ].*")));
    }

    static Stream<Arguments> invalidAlbums() {
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        LocalDate date = LocalDate.of(2020, 1, 1);
        return Stream.of(
                Arguments.of(new Album("B", "", artist, Genre.POP, date), "title", NotBlank.class),
                Arguments.of(new Album("B", "X".repeat(151), artist, Genre.POP, date), "title", Size.class),
                Arguments.of(new Album("?", "Album", artist, Genre.POP, date), "upcCode", Pattern.class),
                Arguments.of(new Album("B", "Album", null, Genre.POP, date), "artist", NotNull.class),
                Arguments.of(new Album("B", "Album", artist, Genre.POP, LocalDate.now().plusDays(1)),
                        "releaseDate", PastOrPresent.class));
    }

    @ParameterizedTest
    @MethodSource("invalidAlbums")
    @DisplayName("Reports the expected field for every standard album constraint")
    void validatesAlbumConstraints(Album album, String field, Class<? extends Annotation> annotation) {
        assertTrue(validator.validate(album).stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals(field)
                        && violation.getConstraintDescriptor().getAnnotation().annotationType().equals(annotation)));
    }

    @Test
    @DisplayName("Accepts valid records and a release in the debut year")
    void acceptsValidRecords() {
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        assertTrue(validator.validate(artist).isEmpty());
        assertTrue(validator.validate(new Album("B", "Album", artist, Genre.POP,
                LocalDate.of(2000, 1, 1))).isEmpty());
    }

    @Test
    @DisplayName("Rejects an album release before the artist debut")
    void validatesCrossFieldRule() {
        Artist artist = new Artist("A", "Artist", "USA", 2000);
        Album album = new Album("B", "Album", artist, Genre.POP, LocalDate.of(1999, 12, 31));
        var violations = validator.validate(album);
        assertEquals(1, violations.size());
        var violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertInstanceOf(ValidAlbumRelease.class, violation.getConstraintDescriptor().getAnnotation());
    }

    @Test
    @DisplayName("Validates nested artist fields")
    void validatesNestedArtist() {
        Album album = new Album("B", "Album", new Artist("A", "", "USA", 2000),
                Genre.POP, LocalDate.of(2020, 1, 1));
        assertTrue(validator.validate(album).stream().anyMatch(
                violation -> violation.getPropertyPath().toString().equals("artist.stageName")));
    }

    @Test
    @DisplayName("Uses the validation clock for the current debut year")
    void validatesFutureDebutYear() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-27T12:00:00Z"), ZoneOffset.UTC);
        Validator fixedValidator = factory.usingContext().clockProvider(() -> clock).getValidator();
        assertTrue(fixedValidator.validate(new Artist("A", "Artist", "USA", 2026)).isEmpty());
        assertTrue(fixedValidator.validate(new Artist("A", "Artist", "USA", 2027)).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("debutYear")));
    }

    @Test
    @DisplayName("Allows invalid construction so all violations can be collected")
    void constructorsDoNotRejectInvalidRecords() {
        Artist artist = assertDoesNotThrow(() -> new Artist("", "", "123", null));
        assertEquals(4, validator.validate(artist).size());
        assertDoesNotThrow(() -> new Album("", "", null, null, null));
    }
}
