package ua.music.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    @TempDir
    Path directory;

    @Test
    @DisplayName("Reads a configured path without retaining mutable Properties")
    void readsConfiguredPath() {
        Properties properties = new Properties();
        Path expected = directory.resolve("artists.json");
        properties.setProperty("artists.input", expected.toString());
        AppConfig config = new AppConfig(properties);
        properties.setProperty("artists.input", "changed");
        assertEquals(expected, config.path("artists.input"));
    }

    @Test
    @DisplayName("Reports a missing key in the exception message")
    void rejectsMissingKey() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new AppConfig(new Properties()).path("artists.input"));
        assertTrue(error.getMessage().contains("artists.input"));
    }

    @Test
    @DisplayName("Reports a blank required value as a missing property")
    void rejectsBlankValue() {
        Properties properties = new Properties();
        properties.setProperty("albums.input", " ");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new AppConfig(properties).path("albums.input"));
        assertTrue(error.getMessage().contains("albums.input"));
    }

    @Test
    @DisplayName("Loads the packaged application configuration")
    void loadsClasspathConfiguration() throws IOException {
        assertNotNull(AppConfig.load());
    }
}
