package pt.captainratax.justafk.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired after JustAFK changes a player's AFK state.
 *
 * @since 1.2.0
 */
public final class PlayerAfkStateChangeEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean afk;
    private final boolean automatic;

    /**
     * Creates an AFK state-change event.
     *
     * @param player the player whose state changed
     * @param afk the new AFK state
     * @param automatic whether an inactivity timeout caused the change
     */
    public PlayerAfkStateChangeEvent(
        Player player,
        boolean afk,
        boolean automatic
    ) {
        super(player);
        this.afk = afk;
        this.automatic = automatic;
    }

    /**
     * Gets the player's new AFK state.
     *
     * @return {@code true} when the player is now AFK
     */
    public boolean isAfk() {
        return afk;
    }

    /**
     * Reports whether an inactivity timeout caused this transition.
     *
     * <p>This is {@code false} for manual changes and activity-driven
     * transitions back to active.</p>
     *
     * @return {@code true} for an automatic inactivity transition
     */
    public boolean isAutomatic() {
        return automatic;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
