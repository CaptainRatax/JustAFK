package pt.captainratax.justafk.config;

import java.util.Arrays;
import java.util.Locale;

public enum AnnouncementAudience {
    ALL,
    OPS,
    NONE;

    public static AnnouncementAudience fromConfig(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Announcement audience cannot be null.");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(audience -> audience.name().equals(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid announcement audience '" + value + "'. Use all, ops, or none."
            ));
    }

    public String configValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
