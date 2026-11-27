package ua.music.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileStorage<T> {
    private final Class<T> type;
    private final ObjectMapper json = configure(new ObjectMapper());
    private final ObjectMapper yaml = configure(new ObjectMapper(new YAMLFactory()));

    public FileStorage(Class<T> type) {
        this.type = java.util.Objects.requireNonNull(type, "Type must not be null");
    }

    public List<T> load(Path file) throws IOException {
        List<T> result = new ArrayList<>();
        for (JsonNode record : readRecords(file)) {
            result.add(deserialize(record));
        }
        return result;
    }

    public List<JsonNode> readRecords(Path file) throws IOException {
        ObjectMapper mapper = mapper(file);
        JsonNode root;
        try (InputStream input = Files.newInputStream(file)) {
            root = mapper.readTree(input);
        }
        if (root == null || !root.isArray()) {
            throw new IOException("Expected a top-level array of records in " + file);
        }
        List<JsonNode> records = new ArrayList<>();
        root.forEach(records::add);
        return records;
    }

    public T deserialize(JsonNode record) throws JsonProcessingException {
        return json.treeToValue(record, type);
    }

    public void save(Path file, List<T> items) throws IOException {
        ObjectMapper mapper = mapper(file);
        Path target = file.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), ".catalog-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), items);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private ObjectMapper mapper(Path file) {
        return switch (DataFormat.from(file)) {
            case JSON -> json;
            case YAML -> yaml;
        };
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        return mapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }
}
