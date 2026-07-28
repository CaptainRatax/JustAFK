package pt.captainratax.justafk.config;

import java.util.Objects;

public final class PluginSettings {

    private final long inactivityTimeoutSeconds;
    private final AnnouncementAudience announcementAudience;
    private final boolean showInPlayerList;
    private final String playerListFormat;
    private final String commandPrefix;
    private final String becameAfkMessage;
    private final String becameActiveMessage;

    public PluginSettings(
        long inactivityTimeoutSeconds,
        AnnouncementAudience announcementAudience,
        boolean showInPlayerList,
        String playerListFormat,
        String commandPrefix,
        String becameAfkMessage,
        String becameActiveMessage
    ) {
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

        this.inactivityTimeoutSeconds = inactivityTimeoutSeconds;
        this.announcementAudience = announcementAudience;
        this.showInPlayerList = showInPlayerList;
        this.playerListFormat = playerListFormat;
        this.commandPrefix = commandPrefix;
        this.becameAfkMessage = becameAfkMessage;
        this.becameActiveMessage = becameActiveMessage;
    }

    public long inactivityTimeoutSeconds() {
        return inactivityTimeoutSeconds;
    }

    public AnnouncementAudience announcementAudience() {
        return announcementAudience;
    }

    public boolean showInPlayerList() {
        return showInPlayerList;
    }

    public String playerListFormat() {
        return playerListFormat;
    }

    public String commandPrefix() {
        return commandPrefix;
    }

    public String becameAfkMessage() {
        return becameAfkMessage;
    }

    public String becameActiveMessage() {
        return becameActiveMessage;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PluginSettings)) {
            return false;
        }

        PluginSettings settings = (PluginSettings) other;
        return inactivityTimeoutSeconds == settings.inactivityTimeoutSeconds
            && showInPlayerList == settings.showInPlayerList
            && announcementAudience == settings.announcementAudience
            && playerListFormat.equals(settings.playerListFormat)
            && commandPrefix.equals(settings.commandPrefix)
            && becameAfkMessage.equals(settings.becameAfkMessage)
            && becameActiveMessage.equals(settings.becameActiveMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            inactivityTimeoutSeconds,
            announcementAudience,
            showInPlayerList,
            playerListFormat,
            commandPrefix,
            becameAfkMessage,
            becameActiveMessage
        );
    }

    @Override
    public String toString() {
        return "PluginSettings["
            + "inactivityTimeoutSeconds=" + inactivityTimeoutSeconds
            + ", announcementAudience=" + announcementAudience
            + ", showInPlayerList=" + showInPlayerList
            + ", playerListFormat=" + playerListFormat
            + ", commandPrefix=" + commandPrefix
            + ", becameAfkMessage=" + becameAfkMessage
            + ", becameActiveMessage=" + becameActiveMessage
            + ']';
    }
}
