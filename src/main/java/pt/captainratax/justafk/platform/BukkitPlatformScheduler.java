package pt.captainratax.justafk.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

final class BukkitPlatformScheduler implements PlatformScheduler {

    private final JavaPlugin plugin;

    BukkitPlatformScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledHandle repeatGlobal(
        Runnable task,
        long initialDelayTicks,
        long periodTicks
    ) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(
            plugin,
            task,
            initialDelayTicks,
            periodTicks
        );
        return bukkitTask::cancel;
    }

    @Override
    public void runForPlayer(Player player, Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public String platformName() {
        return "Bukkit-compatible scheduler";
    }
}
