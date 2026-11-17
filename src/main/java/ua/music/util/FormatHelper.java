package ua.music.util;

import java.util.Locale;

final class FormatHelper {
    private FormatHelper() {
    }

    static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    static String formatMoney(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    static String normalizeLowercase(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
