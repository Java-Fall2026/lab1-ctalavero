package ua.music.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.repository.DuplicateIdentityException;
import ua.music.repository.GenericRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final Validator validator;

    public DataLoader(Validator validator) {
        this.validator = java.util.Objects.requireNonNull(validator, "Validator must not be null");
    }

    public <T> LoadResult load(Path file, FileStorage<T> storage, GenericRepository<T> repository) {
        List<RejectedRecord> rejected = new ArrayList<>();
        List<JsonNode> records;
        try {
            records = storage.readRecords(file);
        } catch (IOException | IllegalArgumentException e) {
            String message = e instanceof JsonProcessingException jsonError
                    ? jsonError.getOriginalMessage() : Objects.toString(e.getMessage(), e.getClass().getSimpleName());
            message = message.replaceAll("\\R", " ");
            log.error("Cannot load file {}: {}", file, message);
            rejected.add(new RejectedRecord(0, List.of("<file>: " + message)));
            return summarize(file, 0, 0, rejected);
        }

        int added = 0;
        for (int index = 0; index < records.size(); index++) {
            int number = index + 1;
            T item;
            try {
                item = storage.deserialize(records.get(index));
            } catch (JsonProcessingException e) {
                String field = e instanceof JsonMappingException mapping
                        ? mapping.getPath().stream().map(JsonMappingException.Reference::getFieldName)
                                .filter(Objects::nonNull).collect(Collectors.joining("."))
                        : "";
                String label = field.isEmpty() ? "<record>" : field;
                reject(file, number, List.of(label + ": " + e.getOriginalMessage().replaceAll("\\R", " ")), rejected);
                continue;
            }
            if (item == null) {
                reject(file, number, List.of("<record>: Record must not be null"), rejected);
                continue;
            }
            List<String> errors = validator.validate(item).stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted().toList();
            if (!errors.isEmpty()) {
                reject(file, number, errors, rejected);
                continue;
            }
            try {
                repository.add(item);
                added++;
            } catch (DuplicateIdentityException e) {
                // The repository already logs the rejected duplicate.
                rejected.add(new RejectedRecord(number, List.of("<identity>: " + e.getMessage())));
            }
        }
        return summarize(file, records.size(), added, rejected);
    }

    private void reject(Path file, int number, List<String> errors, List<RejectedRecord> rejected) {
        rejected.add(new RejectedRecord(number, errors));
        log.warn("Rejected record {} in {}: {}", number, file, String.join("; ", errors));
    }

    private LoadResult summarize(Path file, int read, int added, List<RejectedRecord> rejected) {
        log.info("File {}: read {}, added {}, rejected {}", file, read, added, rejected.size());
        return new LoadResult(read, added, rejected);
    }
}
