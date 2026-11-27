package ua.music.repository;

import ua.music.model.Artist;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class SearchBenchmark {
    private static final int ITEM_COUNT = 100_000;
    private static final int LOOKUPS = 100;
    private static final int WARMUP_ROUNDS = 5;
    private static final int SAMPLES = 9;
    private static volatile int observedMatches;

    public static void main(String[] args) {
        IdentityExtractor<Artist> extractor = artist -> artist.artistCode();
        GenericRepository<Artist> repository = new GenericRepository<>("Benchmark artists", extractor);
        for (int i = 0; i < ITEM_COUNT; i++) {
            repository.add(new Artist("BENCH-" + i, "Artist " + i, "USA", 2000));
        }
        List<Artist> items = repository.findAll();
        String[] identities = {"BENCH-0", "BENCH-" + (ITEM_COUNT - 1), "BENCH-MISSING"};
        String[] scenarios = {"First", "Last", "Missing"};

        System.out.println("Items: " + ITEM_COUNT + "; lookups per sample: " + LOOKUPS
                + "; warmup rounds: " + WARMUP_ROUNDS + "; samples: " + SAMPLES);
        System.out.println("| Scenario | Map, ns/lookup | List, ns/lookup |");
        System.out.println("| --- | ---: | ---: |");
        for (int i = 0; i < identities.length; i++) {
            String identity = identities[i];
            boolean expectedFound = i < 2;
            Supplier<Optional<Artist>> mapSearch = () -> repository.findByIdentity(identity);
            Supplier<Optional<Artist>> listSearch = () -> linearSearch(items, identity, extractor);

            for (int warmup = 0; warmup < WARMUP_ROUNDS; warmup++) {
                timeBatch(mapSearch, expectedFound);
                timeBatch(listSearch, expectedFound);
            }
            long[] mapTimes = new long[SAMPLES];
            long[] listTimes = new long[SAMPLES];
            for (int sample = 0; sample < SAMPLES; sample++) {
                // Alternate the measurement order to reduce order bias.
                if (sample % 2 == 0) {
                    mapTimes[sample] = timeBatch(mapSearch, expectedFound);
                    listTimes[sample] = timeBatch(listSearch, expectedFound);
                } else {
                    listTimes[sample] = timeBatch(listSearch, expectedFound);
                    mapTimes[sample] = timeBatch(mapSearch, expectedFound);
                }
            }
            Arrays.sort(mapTimes);
            Arrays.sort(listTimes);
            System.out.printf(Locale.ROOT, "| %s | %.1f | %.1f |%n", scenarios[i],
                    mapTimes[SAMPLES / 2] / (double) LOOKUPS,
                    listTimes[SAMPLES / 2] / (double) LOOKUPS);
        }
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private static <E> long timeBatch(Supplier<Optional<E>> search, boolean expectedFound) {
        int matches = 0;
        long start = System.nanoTime();
        for (int lookup = 0; lookup < LOOKUPS; lookup++) {
            if (search.get().isPresent()) {
                matches++;
            }
        }
        long elapsed = System.nanoTime() - start;
        observedMatches = matches;
        if (matches != (expectedFound ? LOOKUPS : 0)) {
            throw new AssertionError("Unexpected search result count: " + matches);
        }
        return elapsed;
    }
}
