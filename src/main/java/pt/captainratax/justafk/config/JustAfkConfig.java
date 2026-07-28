package pt.captainratax.justafk.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Owns config.yml and publishes validated settings snapshots.
 */
public final class JustAfkConfig {

    private static final String TIMEOUT_PATH = "inactivity-timeout-seconds";
    private static final String AUDIENCE_PATH = "announcements.audience";
    private static final String PLAYER_LIST_ENABLED_PATH = "player-list.enabled";

    private final JavaPlugin plugin;
    private final File configFile;
    private YamlConfiguration configuration;
    private volatile PluginSettings settings;

    public JustAfkConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public synchronized void load() throws IOException, InvalidConfigurationException {
        Files.createDirectories(plugin.getDataFolder().toPath());
        if (!configFile.isFile()) {
            plugin.saveResource("config.yml", false);
        }

        YamlConfiguration loaded = new YamlConfiguration();
        enableCommentParsing(loaded);
        loaded.load(configFile);

        PluginSettings loadedSettings = readSettings(loaded);
        configuration = loaded;
        settings = loadedSettings;
    }

    public void reload() throws IOException, InvalidConfigurationException {
        load();
    }

    public PluginSettings settings() {
        PluginSettings current = settings;
        if (current == null) {
            throw new IllegalStateException("The JustAFK configuration has not been loaded.");
        }
        return current;
    }

    public synchronized void setInactivityTimeoutSeconds(long seconds) throws IOException {
        update(TIMEOUT_PATH, seconds);
    }

    public synchronized void setAnnouncementAudience(AnnouncementAudience audience)
        throws IOException {
        update(AUDIENCE_PATH, audience.configValue());
    }

    public synchronized void setPlayerListEnabled(boolean enabled) throws IOException {
        update(PLAYER_LIST_ENABLED_PATH, enabled);
    }

    private void update(String path, Object value) throws IOException {
        Object previousValue = configuration.get(path);
        configuration.set(path, value);

        PluginSettings updatedSettings;
        try {
            // Validate and save before exposing the new settings to the rest of the plugin.
            updatedSettings = readSettings(configuration);
            configuration.save(configFile);
        } catch (IOException | RuntimeException exception) {
            configuration.set(path, previousValue);
            throw exception;
        }

        settings = updatedSettings;
    }

    private void enableCommentParsing(YamlConfiguration yaml) {
        try {
            Method parseComments = yaml.options().getClass().getMethod(
                "parseComments",
                boolean.class
            );
            parseComments.invoke(yaml.options(), true);
        } catch (NoSuchMethodException ignored) {
            // Older Bukkit versions cannot preserve comments when saving YAML.
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not enable YAML comment parsing.", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null
                ? exception
                : exception.getCause();
            throw new IllegalStateException("Could not enable YAML comment parsing.", cause);
        }
    }

    private PluginSettings readSettings(YamlConfiguration yaml) {
        long timeout = yaml.getLong(TIMEOUT_PATH, 300L);
        AnnouncementAudience audience = AnnouncementAudience.fromConfig(
            yaml.getString(AUDIENCE_PATH, "all")
        );

        return new PluginSettings(
            timeout,
            audience,
            yaml.getBoolean(PLAYER_LIST_ENABLED_PATH, true),
            yaml.getString("player-list.format", "&7[AFK {duration}]&r "),
            yaml.getString("messages.command-prefix", "&8[&7JustAFK&8]&r "),
            yaml.getString(
                "messages.became-afk",
                "&8[&7JustAFK&8]&r &7{player} is now AFK."
            ),
            yaml.getString(
                "messages.became-active",
                "&8[&7JustAFK&8]&r &7{player} is no longer AFK."
            )
        );
    }
}
