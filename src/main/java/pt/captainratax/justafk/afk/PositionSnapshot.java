package pt.captainratax.justafk.afk;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The part of a location that counts as activity; yaw and pitch are ignored.
 */
public record PositionSnapshot(UUID worldId, double x, double y, double z) {

    public static PositionSnapshot from(Location location) {
        World world = location.getWorld();
        return new PositionSnapshot(
            world == null ? null : world.getUID(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }
}
