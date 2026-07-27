package pt.captainratax.justafk;

import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pt.captainratax.justafk.afk.AfkManager;
import pt.captainratax.justafk.command.AfkCommand;
import pt.captainratax.justafk.command.JustAfkCommand;
import pt.captainratax.justafk.config.JustAfkConfig;
import pt.captainratax.justafk.listener.PlayerActivityListener;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.platform.PlatformSchedulerFactory;
import pt.captainratax.justafk.platform.ScheduledHandle;

/**
 * Wires together configuration, commands, listeners, and platform scheduling.
 */
public final class JustAfkPlugin extends JavaPlugin {

    private PlatformScheduler scheduler;
    private AfkManager afkManager;
    private ScheduledHandle monitorTask;

    @Override
    public void onEnable() {
        try {
            JustAfkConfig config = new JustAfkConfig(this);
            config.load();

            scheduler = PlatformSchedulerFactory.create(this);
            afkManager = new AfkManager(config, scheduler);

            Bukkit.getPluginManager().registerEvents(
                new PlayerActivityListener(afkManager),
                this
            );

            registerCommands(config);

            // Players may already be online when the plugin is reloaded.
            Bukkit.getOnlinePlayers().forEach(player ->
                scheduler.runForPlayer(player, () -> afkManager.register(player))
            );

            // This pass drives timeouts and refreshes duration labels once per second.
            monitorTask = scheduler.repeatGlobal(afkManager::checkOnlinePlayers, 20L, 20L);

            getLogger().info(
                "JustAFK " + getDescription().getVersion()
                    + " enabled on " + scheduler.platformName() + "."
            );
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "JustAFK could not be enabled.", exception);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        if (afkManager != null) {
            afkManager.shutdown();
        }
        if (scheduler != null) {
            scheduler.cancelAll();
        }
    }

    private void registerCommands(JustAfkConfig config) {
        PluginCommand afkPluginCommand = Objects.requireNonNull(
            getCommand("afk"),
            "The afk command is missing from plugin.yml."
        );
        AfkCommand afkCommand = new AfkCommand(afkManager, scheduler, config);
        afkPluginCommand.setExecutor(afkCommand);
        afkPluginCommand.setTabCompleter(afkCommand);

        PluginCommand adminPluginCommand = Objects.requireNonNull(
            getCommand("justafk"),
            "The justafk command is missing from plugin.yml."
        );
        JustAfkCommand justAfkCommand = new JustAfkCommand(config, afkManager);
        adminPluginCommand.setExecutor(justAfkCommand);
        adminPluginCommand.setTabCompleter(justAfkCommand);
    }
}
