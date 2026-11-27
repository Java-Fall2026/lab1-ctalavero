package ua.music.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig(Properties properties) {
        this.properties.putAll(properties);
    }

    public static AppConfig load() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (input == null) {
                throw new IOException("Configuration resource app.properties was not found");
            }
            properties.load(input);
        }
        return new AppConfig(properties);
    }

    public Path path(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return Path.of(value.trim());
    }
}
