package pt.captainratax.justafk.config;

import java.util.Objects;

public record PluginSettings(
    long inactivityTimeoutSeconds,
    AnnouncementAudience announcementAudience,
    boolean showInPlayerList,
    String playerListFormat,
    String commandPrefix,
    String becameAfkMessage,
    String becameActiveMessage
) {

    public PluginSettings {
        if (inactivityTimeoutSeconds < 1L) {
            throw new IllegalArgumentException(
                "inactivity-timeout-seconds must be at least 1."
            );
        }
        Objects.requireNonNull(announcementAudience, "announcementAudience");
        Objects.requireNonNull(playerListFormat, "playerListFormat");
        Objects.requireNonNull(commandPrefix, "commandPrefix");
        Objects.requireNonNull(becameAfkMessage, "becameAfkMessage");
        Objects.requireNonNull(becameActiveMessage, "becameActiveMessage");

        if (!playerListFormat.contains("{duration}")) {
            throw new IllegalArgumentException(
                "player-list.format must contain the {duration} placeholder."
            );
        }
    }
}
