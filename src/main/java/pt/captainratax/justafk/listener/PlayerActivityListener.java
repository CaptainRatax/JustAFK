package pt.captainratax.justafk.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.captainratax.justafk.afk.AfkManager;

/**
 * Feeds player lifecycle and movement-input events into the AFK manager.
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInput(PlayerInputEvent event) {
        afkManager.recordInput(event.getPlayer(), event.getInput());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkManager.unregister(event.getPlayer());
    }
}
