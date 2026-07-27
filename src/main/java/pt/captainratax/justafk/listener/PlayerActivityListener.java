package pt.captainratax.justafk.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.captainratax.justafk.afk.AfkManager;

/**
 * Feeds player lifecycle and movement events into the AFK manager.
 */
public final class PlayerActivityListener implements Listener {

    private final AfkManager afkManager;

    public PlayerActivityListener(AfkManager afkManager) {
        this.afkManager = afkManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        afkManager.register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() != null) {
            afkManager.recordMovement(event.getPlayer(), event.getFrom(), event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkManager.unregister(event.getPlayer());
    }
}
