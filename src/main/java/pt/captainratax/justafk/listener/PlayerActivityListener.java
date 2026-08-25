package pt.captainratax.justafk.listener;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.captainratax.justafk.afk.AfkManager;
import pt.captainratax.justafk.platform.PlatformScheduler;

/**
 * Feeds player lifecycle and activity events into the AFK manager.
 */
public final class PlayerActivityListener implements Listener {

    private final AfkManager afkManager;
    private final PlatformScheduler scheduler;

    public PlayerActivityListener(
        AfkManager afkManager,
        PlatformScheduler scheduler
    ) {
        this.afkManager = Objects.requireNonNull(afkManager, "afkManager");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        afkManager.register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInput(PlayerInputEvent event) {
        afkManager.recordInput(event.getPlayer(), event.getInput());
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        scheduler.runForPlayer(player, () -> {
            if (player.isOnline()) {
                afkManager.recordActivity(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        afkManager.recordActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        afkManager.recordActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerFish(PlayerFishEvent event) {
        // A bite is server-driven; casting and reeling are player actions.
        if (event.getState() != PlayerFishEvent.State.BITE) {
            afkManager.recordActivity(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            afkManager.recordActivity(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        afkManager.recordActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        afkManager.recordActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkManager.unregister(event.getPlayer());
    }
}
