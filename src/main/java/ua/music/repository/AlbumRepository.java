package ua.music.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.model.Album;
import ua.music.model.Genre;
import ua.music.util.MusicUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlbumRepository extends GenericRepository<Album> {
    private static final Logger log = LoggerFactory.getLogger(AlbumRepository.class);

    public AlbumRepository() {
        super("Albums", Album::upcCode);
    }

    public List<Album> sortByTitle() {
        return findAllSorted(Comparator.comparing(Album::title).thenComparing(Album::upcCode),
                "title ascending, then UPC ascending");
    }

    public List<Album> sortByReleaseDateDescending() {
        return findAllSorted(Comparator.comparing(Album::releaseDate).reversed()
                        .thenComparing(Album::title).thenComparing(Album::upcCode),
                "release date descending, then title and UPC ascending");
    }

    public List<Album> findByTitle(String fragment) {
        String query = MusicUtils.requireNotBlank(fragment, "Title query").toLowerCase(Locale.ROOT);
        return search("title contains '" + fragment + "'",
                album -> album.title().toLowerCase(Locale.ROOT).contains(query));
    }

    public List<Album> findByReleaseDateRange(LocalDate fromDate, LocalDate toDate) {
        MusicUtils.requireNotNull(fromDate, "Start date");
        MusicUtils.requireNotNull(toDate, "End date");
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Start date must not exceed end date, got: "
                    + fromDate + " > " + toDate);
        }
        return search("release date between " + fromDate + " and " + toDate,
                album -> !album.releaseDate().isBefore(fromDate) && !album.releaseDate().isAfter(toDate));
    }

    public List<Album> findByGenreAndArtist(Genre genre, String artistCode) {
        MusicUtils.requireNotNull(genre, "Genre");
        MusicUtils.requireNotBlank(artistCode, "Artist code");
        return search("genre " + genre + " and artist code '" + artistCode + "'",
                album -> album.genre() == genre && album.artist().artistCode().equals(artistCode));
    }

    public Optional<Album> findFirstByTitle(String title) {
        MusicUtils.requireNotBlank(title, "Title");
        Optional<Album> result = stream()
                .filter(album -> album.title().equalsIgnoreCase(title))
                .sorted()
                .findFirst();
        logSearch("first exact title '" + title + "'", result.stream().count());
        return result;
    }

    public Map<Genre, Long> countByGenre() {
        Map<Genre, Long> result = stream().collect(Collectors.groupingBy(
                Album::genre, () -> new EnumMap<>(Genre.class), Collectors.counting()));
        log.info("Counted albums by genre: {} groups", result.size());
        return result;
    }

    public long totalAgeYears() {
        LocalDate today = LocalDate.now();
        long result = stream()
                .mapToLong(album -> ChronoUnit.YEARS.between(album.releaseDate(), today))
                .sum();
        log.info("Summed completed album ages: {} years", result);
        return result;
    }

    private List<Album> search(String criterion, Predicate<Album> predicate) {
        List<Album> result = stream().filter(predicate).sorted().toList();
        logSearch(criterion, result.size());
        return result;
    }

    private void logSearch(String criterion, long count) {
        if (count == 0) {
            log.warn("Album search by {} found 0 entries", criterion);
        } else {
            log.info("Album search by {} found {} entries", criterion, count);
        }
    }
}
