package ua.music.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.model.Artist;
import ua.music.model.MusicUser;
import ua.music.model.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CatalogLoader {
    private static final Logger log = LoggerFactory.getLogger(CatalogLoader.class);
    private static final String ARTIST_HEADER = "artistCode,stageName,country,debutYear";
    private static final String USER_HEADER = "accountEmail,username,subscription";

    public List<Artist> loadArtists(Path path) {
        return load(path, ARTIST_HEADER, this::parseArtist);
    }

    public List<MusicUser> loadUsers(Path path) {
        return load(path, USER_HEADER, this::parseUser);
    }

    public Artist parseArtist(String line, int lineNumber) throws InvalidDataException {
        try {
            String[] fields = columns(line, 4);
            return new Artist(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]));
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    public MusicUser parseUser(String line, int lineNumber) throws InvalidDataException {
        try {
            String[] fields = columns(line, 3);
            Subscription subscription = Subscription.valueOf(fields[2].toUpperCase(Locale.ROOT));
            return MusicUser.of(fields[0], fields[1], subscription);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    private String[] columns(String line, int expected) {
        String[] fields = line.split(",", -1);
        if (fields.length != expected) {
            throw new IllegalArgumentException("Expected " + expected + " columns, got: " + fields.length);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }

    private <T> List<T> load(Path path, String header, RowParser<T> parser) {
        List<T> items = new ArrayList<>();
        int rejected = 0;
        log.info("Reading {}", path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String actualHeader = reader.readLine();
            if (!header.equals(actualHeader)) {
                throw new InvalidDataException("Line 1: expected header '" + header + "', got: " + actualHeader);
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    items.add(parser.parse(line, lineNumber));
                } catch (InvalidDataException e) {
                    rejected++;
                    log.warn("Skipping invalid row in {}: {}", path, e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            log.warn("Catalog file not found: {}", path);
        } catch (IOException | InvalidDataException e) {
            log.error("Cannot read catalog file {}", path, e);
        } finally {
            // Report partial results even if opening or reading the file fails.
            log.info("Finished {}: loaded {} entries, rejected {} rows", path, items.size(), rejected);
        }
        return items;
    }

    @FunctionalInterface
    private interface RowParser<T> {
        T parse(String line, int lineNumber) throws InvalidDataException;
    }
}
