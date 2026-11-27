package ua.music.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.util.MusicUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class GenericRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<? super T> extractor;
    private final Map<String, T> items = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<? super T> extractor) {
        this.name = MusicUtils.requireNotBlank(name, "Repository name");
        this.extractor = MusicUtils.requireNotNull(extractor, "Identity extractor");
    }

    public void add(T item) {
        if (!addIfAbsent(item)) {
            throw new DuplicateIdentityException(extractor.identityOf(item), name);
        }
    }

    public boolean addIfAbsent(T item) {
        if (item == null) {
            log.error("Cannot add null to repository '{}'", name);
            throw new IllegalArgumentException("Item must not be null, got: null");
        }
        String identity = requireIdentity(extractor.identityOf(item));
        if (items.putIfAbsent(identity, item) != null) {
            log.warn("Duplicate identity '{}' rejected by repository '{}'", identity, name);
            return false;
        }
        log.info("Added '{}' to repository '{}'", identity, name);
        return true;
    }

    public void addAll(Collection<? extends T> values) {
        MusicUtils.requireNotNull(values, "Items");
        for (T item : values) {
            add(item);
        }
    }

    public boolean removeByIdentity(String identity) {
        requireIdentity(identity);
        if (items.remove(identity) == null) {
            log.warn("Identity '{}' not found in repository '{}': 0 entries", identity, name);
            return false;
        }
        log.info("Removed '{}' from repository '{}'", identity, name);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        requireIdentity(identity);
        T item = items.get(identity);
        if (item == null) {
            log.warn("Identity '{}' not found in repository '{}': 0 entries", identity, name);
        } else {
            log.info("Found '{}' in repository '{}': 1 entry", identity, name);
        }
        return Optional.ofNullable(item);
    }

    public List<T> findAll() {
        return List.copyOf(items.values());
    }

    public Stream<T> stream() {
        return items.values().stream();
    }

    public List<T> findAllSorted(Comparator<T> comparator) {
        return findAllSorted(comparator, "provided comparator");
    }

    public List<T> findAllSorted(Comparator<T> comparator, String criterion) {
        MusicUtils.requireNotNull(comparator, "Comparator");
        MusicUtils.requireNotBlank(criterion, "Sort criterion");
        List<T> result = stream().sorted(comparator).toList();
        log.info("Sorted repository '{}' by {}: {} entries", name, criterion, result.size());
        return result;
    }

    public List<T> findAllSortedByIdentity(SortOrder order) {
        MusicUtils.requireNotNull(order, "Sort order");
        Comparator<String> comparator = order == SortOrder.ASC
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        List<T> result = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .map(Map.Entry::getValue)
                .toList();
        log.info("Sorted repository '{}' by identity {}: {} entries", name, order, result.size());
        return result;
    }

    public int size() {
        return items.size();
    }

    private String requireIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            log.error("Invalid identity '{}' in repository '{}'", identity, name);
            throw new IllegalArgumentException("Identity must not be blank, got: '" + identity + "'");
        }
        return identity;
    }
}
