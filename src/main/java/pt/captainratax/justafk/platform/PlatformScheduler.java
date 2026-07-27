package pt.captainratax.justafk.platform;

import org.bukkit.entity.Player;

/**
 * Hides the scheduling differences between Bukkit and Folia.
 */
public interface PlatformScheduler {

    ScheduledHandle repeatGlobal(Runnable task, long initialDelayTicks, long periodTicks);

    void runForPlayer(Player player, Runnable task);

    void cancelAll();

    String platformName();
}
