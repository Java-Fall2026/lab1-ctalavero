package ua.music.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.model.Artist;
import ua.music.util.MusicUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArtistRepository extends GenericRepository<Artist> {
    private static final Logger log = LoggerFactory.getLogger(ArtistRepository.class);

    public ArtistRepository() {
        super("Artists", Artist::artistCode);
    }

    public List<Artist> sortByStageName() {
        Comparator<Artist> byStageName = (left, right) -> left.stageName().compareTo(right.stageName());
        return findAllSorted(byStageName.thenComparing(Artist::artistCode),
                "stage name ascending, then artist code ascending");
    }

    public List<Artist> sortByDebutYearDescending() {
        return findAllSorted(Comparator.comparingInt(Artist::debutYear).reversed()
                        .thenComparing(Artist::stageName).thenComparing(Artist::artistCode),
                "debut year descending, then stage name and artist code ascending");
    }

    public List<Artist> findByStageName(String fragment) {
        String query = MusicUtils.requireNotBlank(fragment, "Stage name query").toLowerCase(Locale.ROOT);
        return search("stage name contains '" + fragment + "'",
                artist -> artist.stageName().toLowerCase(Locale.ROOT).contains(query));
    }

    public List<Artist> findByDebutYearRange(int fromYear, int toYear) {
        if (fromYear > toYear) {
            throw new IllegalArgumentException("Start year must not exceed end year, got: "
                    + fromYear + " > " + toYear);
        }
        return search("debut year between " + fromYear + " and " + toYear,
                artist -> artist.debutYear() >= fromYear && artist.debutYear() <= toYear);
    }

    public List<Artist> findByCountryAndDebutYear(String country, int fromYear) {
        MusicUtils.requireNotBlank(country, "Country");
        return search("country '" + country + "' and debut year >= " + fromYear,
                artist -> artist.country().equalsIgnoreCase(country) && artist.debutYear() >= fromYear);
    }

    public Optional<Artist> findFirstByStageName(String stageName) {
        MusicUtils.requireNotBlank(stageName, "Stage name");
        Optional<Artist> result = stream()
                .filter(artist -> artist.stageName().equalsIgnoreCase(stageName))
                .sorted()
                .findFirst();
        logSearch("first exact stage name '" + stageName + "'", result.stream().count());
        return result;
    }

    public List<String> findCountries(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Country limit must be at least 0, got: " + limit);
        }
        List<String> result = stream()
                .map(Artist::country)
                .distinct()
                .sorted()
                .limit(limit)
                .toList();
        logSearch("distinct countries alphabetically, limit " + limit, result.size());
        return result;
    }

    public Map<String, List<Artist>> groupByCountry() {
        Map<String, List<Artist>> result = stream()
                .collect(Collectors.groupingBy(Artist::country, TreeMap::new, Collectors.toList()));
        log.info("Grouped artists by country: {} groups", result.size());
        return result;
    }

    private List<Artist> search(String criterion, Predicate<Artist> predicate) {
        List<Artist> result = stream().filter(predicate).sorted().toList();
        logSearch(criterion, result.size());
        return result;
    }

    private void logSearch(String criterion, long count) {
        if (count == 0) {
            log.warn("Artist search by {} found 0 entries", criterion);
        } else {
            log.info("Artist search by {} found {} entries", criterion, count);
        }
    }
}
