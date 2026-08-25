package pt.captainratax.justafk.api;

import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Provides read-only access to the AFK state tracked by JustAFK.
 *
 * @since 1.2.0
 */
public interface JustAfkApi {

    /**
     * Checks whether a player is currently tracked as AFK.
     *
     * @param player the player to check
     * @return {@code true} when the player is tracked as AFK
     */
    boolean isAfk(Player player);

    /**
     * Checks whether a player UUID is currently tracked as AFK.
     *
     * <p>An unknown or untracked UUID returns {@code false} without creating
     * tracking state.</p>
     *
     * @param playerId the player UUID to check
     * @return {@code true} when the UUID is tracked as AFK
     */
    boolean isAfk(UUID playerId);
}
