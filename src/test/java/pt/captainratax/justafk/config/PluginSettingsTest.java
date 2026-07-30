package pt.captainratax.justafk.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PluginSettingsTest {

    @Test
    void exposesValidatedSettings() {
        PluginSettings settings = settings(300L, "&7[AFK {duration}]&r ");

        assertTrue(settings.enabled());
        assertTrue(settings.automaticAfkEnabled());
        assertEquals(300L, settings.inactivityTimeoutSeconds());
        assertEquals(AnnouncementAudience.ALL, settings.announcementAudience());
        assertTrue(settings.showInPlayerList());
        assertEquals("&7[AFK {duration}]&r ", settings.playerListFormat());
    }

    @Test
    void exposesFeatureFlagsIndependently() {
        assertFeatureFlags(true, true);
        assertFeatureFlags(true, false);
        assertFeatureFlags(false, true);
        assertFeatureFlags(false, false);
    }

    @Test
    void equalityHashCodeAndToStringIncludeFeatureFlags() {
        PluginSettings enabled = settings(true, true, 300L, "&7[AFK {duration}]&r ");
        PluginSettings equal = settings(true, true, 300L, "&7[AFK {duration}]&r ");
        PluginSettings pluginDisabled = settings(
            false,
            true,
            300L,
            "&7[AFK {duration}]&r "
        );
        PluginSettings automaticAfkDisabled = settings(
            true,
            false,
            300L,
            "&7[AFK {duration}]&r "
        );

        assertEquals(enabled, equal);
        assertEquals(enabled.hashCode(), equal.hashCode());
        assertNotEquals(enabled, pluginDisabled);
        assertNotEquals(enabled.hashCode(), pluginDisabled.hashCode());
        assertNotEquals(enabled, automaticAfkDisabled);
        assertNotEquals(enabled.hashCode(), automaticAfkDisabled.hashCode());
        assertTrue(enabled.toString().contains("enabled=true"));
        assertTrue(enabled.toString().contains("automaticAfkEnabled=true"));
        assertTrue(pluginDisabled.toString().contains("enabled=false"));
        assertTrue(
            automaticAfkDisabled.toString().contains("automaticAfkEnabled=false")
        );
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
        return settings(true, true, timeout, playerListFormat);
    }

    private void assertFeatureFlags(boolean enabled, boolean automaticAfkEnabled) {
        PluginSettings settings = settings(
            enabled,
            automaticAfkEnabled,
            300L,
            "&7[AFK {duration}]&r "
        );

        assertEquals(enabled, settings.enabled());
        assertEquals(automaticAfkEnabled, settings.automaticAfkEnabled());
    }

    private PluginSettings settings(
        boolean enabled,
        boolean automaticAfkEnabled,
        long timeout,
        String playerListFormat
    ) {
        return new PluginSettings(
            enabled,
            automaticAfkEnabled,
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
