package ua.music.io;

import java.nio.file.Path;
import java.util.Locale;

public enum DataFormat {
    JSON, YAML;

    public static DataFormat from(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) {
            return JSON;
        }
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return YAML;
        }
        throw new IllegalArgumentException("Unsupported data file extension: " + file);
    }
}
