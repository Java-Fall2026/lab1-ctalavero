package ua.music.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.util.MusicUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            log.warn("Identity '{}' not found in repository '{}'", identity, name);
            return false;
        }
        log.info("Removed '{}' from repository '{}'", identity, name);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        requireIdentity(identity);
        T item = items.get(identity);
        if (item == null) {
            log.warn("Identity '{}' not found in repository '{}'", identity, name);
        } else {
            log.info("Found '{}' in repository '{}'", identity, name);
        }
        return Optional.ofNullable(item);
    }

    public List<T> findAll() {
        return List.copyOf(items.values());
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
