package pt.captainratax.justafk.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PluginSettingsTest {

    @Test
    void exposesValidatedSettings() {
        PluginSettings settings = settings(300L, "&7[AFK {duration}]&r ");

        assertEquals(300L, settings.inactivityTimeoutSeconds());
        assertEquals(AnnouncementAudience.ALL, settings.announcementAudience());
        assertTrue(settings.showInPlayerList());
        assertEquals("&7[AFK {duration}]&r ", settings.playerListFormat());
    }

    @Test
    void rejectsInvalidTimeout() {
        assertThrows(
            IllegalArgumentException.class,
            () -> settings(0L, "&7[AFK {duration}]&r ")
        );
    }

    @Test
    void requiresDurationPlaceholder() {
        assertThrows(
            IllegalArgumentException.class,
            () -> settings(300L, "&7[AFK]&r ")
        );
    }

    private PluginSettings settings(long timeout, String playerListFormat) {
        return new PluginSettings(
            timeout,
            AnnouncementAudience.ALL,
            true,
            playerListFormat,
            "&8[&7JustAFK&8]&r ",
            "{player} is now AFK.",
            "{player} is no longer AFK."
        );
    }
}
