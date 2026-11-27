package ua.music.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DomainRepositoryTest {
    private final Artist alpha = new Artist("A", "Alpha", "USA", 2000);
    private final Artist beta = new Artist("B", "Beta", "UK", 2010);
    private final Artist gamma = new Artist("C", "Gamma", "USA", 2010);
    private final Album summer = new Album("AL1", "Summer", alpha, Genre.POP, LocalDate.of(2020, 1, 1));
    private final Album night = new Album("AL2", "Night", beta, Genre.JAZZ, LocalDate.of(2011, 5, 1));
    private final Album sunrise = new Album("AL3", "Sunrise", alpha, Genre.POP, LocalDate.of(2020, 1, 1));

    private ArtistRepository artists() {
        ArtistRepository result = new ArtistRepository();
        result.addAll(List.of(beta, gamma, alpha));
        return result;
    }

    private AlbumRepository albums() {
        AlbumRepository result = new AlbumRepository();
        result.addAll(List.of(sunrise, night, summer));
        return result;
    }

    @Test
    @DisplayName("Sorts artists and albums by their natural order")
    void sortsByNaturalOrder() {
        List<Artist> artistList = new ArrayList<>(List.of(gamma, beta, alpha));
        List<Album> albumList = new ArrayList<>(List.of(sunrise, night, summer));
        Collections.sort(artistList);
        Collections.sort(albumList);
        assertEquals(List.of(alpha, beta, gamma), artistList);
        assertEquals(List.of(summer, night, sunrise), albumList);
    }

    @Test
    @DisplayName("Compares every artist component when the identity is equal")
    void artistComparisonAgreesWithEquals() {
        assertEquals(0, alpha.compareTo(new Artist("A", "Alpha", "USA", 2000)));
        List<Artist> variants = List.of(
                new Artist("A", "Other", "USA", 2000),
                new Artist("A", "Alpha", "UK", 2000),
                new Artist("A", "Alpha", "USA", 2001));
        variants.forEach(other -> {
            assertNotEquals(alpha, other);
            assertNotEquals(0, alpha.compareTo(other));
            assertEquals(-Integer.signum(other.compareTo(alpha)), Integer.signum(alpha.compareTo(other)));
        });
    }

    @Test
    @DisplayName("Compares every album component including nested artist data")
    void albumComparisonAgreesWithEquals() {
        assertEquals(0, summer.compareTo(new Album("AL1", "Summer", alpha, Genre.POP, LocalDate.of(2020, 1, 1))));
        List<Album> variants = List.of(
                new Album("AL1", "Other", alpha, Genre.POP, summer.releaseDate()),
                new Album("AL1", "Summer", new Artist("A", "Other", "USA", 2000), Genre.POP, summer.releaseDate()),
                new Album("AL1", "Summer", alpha, Genre.JAZZ, summer.releaseDate()),
                new Album("AL1", "Summer", alpha, Genre.POP, summer.releaseDate().plusDays(1)));
        variants.forEach(other -> {
            assertNotEquals(summer, other);
            assertNotEquals(0, summer.compareTo(other));
            assertEquals(-Integer.signum(other.compareTo(summer)), Integer.signum(summer.compareTo(other)));
        });
    }

    @Test
    @DisplayName("Sorts artists by descending debut year and ascending stage name")
    void sortsArtistsWithMultipleKeys() {
        assertEquals(List.of(beta, gamma, alpha), artists().sortByDebutYearDescending());
        assertEquals(List.of(alpha, beta, gamma), artists().sortByStageName());
    }

    @Test
    @DisplayName("Sorts albums by descending release date and ascending title")
    void sortsAlbumsWithMultipleKeys() {
        assertEquals(List.of(summer, sunrise, night), albums().sortByReleaseDateDescending());
        assertEquals(List.of(night, summer, sunrise), albums().sortByTitle());
    }

    @Test
    @DisplayName("Returns reversed identity orders without changing insertion order")
    void sortsByIdentityInBothDirections() {
        ArtistRepository repository = artists();
        assertEquals(List.of(alpha, beta, gamma), repository.findAllSortedByIdentity(SortOrder.ASC));
        assertEquals(List.of(gamma, beta, alpha), repository.findAllSortedByIdentity(SortOrder.DESC));
        assertEquals(List.of(beta, gamma, alpha), repository.findAll());
    }

    private record PriceQuote(String code, double price) {
    }

    @Test
    @DisplayName("Sorts prices with a fractional difference smaller than one")
    void preservesFractionalPriceDifferences() {
        GenericRepository<PriceQuote> prices = new GenericRepository<>("Test prices", PriceQuote::code);
        PriceQuote cheaper = new PriceQuote("A", 10.10);
        PriceQuote dearer = new PriceQuote("B", 10.90);
        prices.addAll(List.of(dearer, cheaper));
        assertEquals(List.of(cheaper, dearer), prices.findAllSorted(Comparator.comparingDouble(PriceQuote::price)));
    }

    @Test
    @DisplayName("Keeps one album when the TreeSet comparator compares only genre")
    void treeSetUsesComparatorEquality() {
        TreeSet<Album> set = new TreeSet<>(Comparator.comparing(Album::genre));
        assertNotEquals(summer, sunrise);
        assertTrue(set.add(summer));
        assertFalse(set.add(sunrise));
        assertEquals(1, set.size());
        assertSame(summer, set.first());
    }

    @Test
    @DisplayName("Finds artists by a case-insensitive part of the stage name")
    void findsArtistsByText() {
        assertEquals(List.of(alpha), artists().findByStageName("LPH"));
        assertTrue(artists().findByStageName("missing").isEmpty());
    }

    @Test
    @DisplayName("Includes both artist year boundaries and excludes outside values")
    void findsArtistsByYearRange() {
        assertEquals(List.of(beta, gamma), artists().findByDebutYearRange(2010, 2010));
        assertEquals(List.of(alpha), artists().findByDebutYearRange(1999, 2000));
        assertTrue(artists().findByDebutYearRange(2001, 2009).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> artists().findByDebutYearRange(2010, 2000));
    }

    @Test
    @DisplayName("Requires both country and minimum debut year to match")
    void findsArtistsByBothConditions() {
        assertEquals(List.of(gamma), artists().findByCountryAndDebutYear("usa", 2010));
    }

    @Test
    @DisplayName("Finds the first exact artist name or returns an empty Optional")
    void findsSingleArtist() {
        ArtistRepository repository = artists();
        repository.add(new Artist("D", "Alpha", "UK", 2001));
        assertEquals(alpha, repository.findFirstByStageName("alpha").orElseThrow());
        assertTrue(repository.findFirstByStageName("missing").isEmpty());
    }

    @Test
    @DisplayName("Returns distinct sorted countries and respects the limit")
    void findsLimitedCountries() {
        assertEquals(List.of("UK", "USA"), artists().findCountries(10));
        assertEquals(List.of("UK"), artists().findCountries(1));
        assertTrue(artists().findCountries(0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> artists().findCountries(-1));
    }

    @Test
    @DisplayName("Finds albums by a case-insensitive part of the title")
    void findsAlbumsByText() {
        assertEquals(List.of(summer, sunrise), albums().findByTitle("SU"));
        assertTrue(albums().findByTitle("missing").isEmpty());
    }

    @Test
    @DisplayName("Includes both album date boundaries and excludes outside values")
    void findsAlbumsByDateRange() {
        assertEquals(List.of(summer, sunrise), albums().findByReleaseDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1)));
        assertEquals(List.of(night), albums().findByReleaseDateRange(
                LocalDate.of(2011, 5, 1), LocalDate.of(2019, 12, 31)));
        assertThrows(IllegalArgumentException.class, () -> albums().findByReleaseDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1)));
    }

    @Test
    @DisplayName("Requires both album genre and artist identity to match")
    void findsAlbumsByBothConditions() {
        AlbumRepository repository = albums();
        repository.add(new Album("AL4", "Other pop", beta, Genre.POP, night.releaseDate()));
        repository.add(new Album("AL5", "Other jazz", alpha, Genre.JAZZ, night.releaseDate()));
        assertEquals(List.of(summer, sunrise), repository.findByGenreAndArtist(Genre.POP, "A"));
    }

    @Test
    @DisplayName("Finds an exact album title or returns an empty Optional")
    void findsSingleAlbum() {
        assertEquals(summer, albums().findFirstByTitle("summer").orElseThrow());
        assertTrue(albums().findFirstByTitle("missing").isEmpty());
    }

    @Test
    @DisplayName("Groups all artists by country")
    void groupsArtists() {
        Map<String, List<Artist>> groups = artists().groupByCountry();
        assertEquals(2, groups.size());
        assertEquals(List.of(gamma, alpha), groups.get("USA"));
        assertEquals(List.of(beta), groups.get("UK"));
    }

    @Test
    @DisplayName("Counts albums within each genre")
    void countsAlbumsByGenre() {
        assertEquals(Map.of(Genre.POP, 2L, Genre.JAZZ, 1L), albums().countByGenre());
    }

    @Test
    @DisplayName("Sums completed album ages and returns zero for an empty repository")
    void aggregatesAlbumAges() {
        AlbumRepository repository = new AlbumRepository();
        Artist artist = new Artist("OLD", "Artist", "USA", 1900);
        LocalDate today = LocalDate.now();
        repository.addAll(List.of(
                new Album("X", "Two years", artist, Genre.POP, today.minusYears(2)),
                new Album("Y", "Almost three years", artist, Genre.POP, today.minusYears(3).plusDays(1))));
        assertEquals(4, repository.totalAgeYears());
        assertEquals(0, new AlbumRepository().totalAgeYears());
        assertTrue(new AlbumRepository().countByGenre().isEmpty());
        assertTrue(new ArtistRepository().groupByCountry().isEmpty());
    }

    @Test
    @DisplayName("Rejects reuse of a consumed stream")
    void rejectsStreamReuse() {
        Stream<Album> stream = albums().stream();
        assertEquals(3, stream.count());
        assertThrows(IllegalStateException.class, stream::count);
    }

    @Test
    @DisplayName("Creates a fresh stream for every repository query")
    void createsFreshStreams() {
        AlbumRepository repository = albums();
        assertEquals(3, repository.stream().count());
        assertEquals(3, repository.stream().count());
        assertEquals(3, repository.findAll().size());
    }

    @Test
    @DisplayName("Keeps sorted result lists immutable")
    void protectsSortedResults() {
        List<Artist> result = artists().findAllSorted(Comparator.naturalOrder());
        assertThrows(UnsupportedOperationException.class, result::clear);
    }
}
