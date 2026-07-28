package pt.captainratax.justafk.afk;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The part of a location that counts as activity; yaw and pitch are ignored.
 */
public final class PositionSnapshot {

    private final UUID worldId;
    private final double x;
    private final double y;
    private final double z;

    public PositionSnapshot(UUID worldId, double x, double y, double z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PositionSnapshot from(Location location) {
        World world = location.getWorld();
        return new PositionSnapshot(
            world == null ? null : world.getUID(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }

    public UUID worldId() {
        return worldId;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PositionSnapshot)) {
            return false;
        }

        PositionSnapshot snapshot = (PositionSnapshot) other;
        return java.util.Objects.equals(worldId, snapshot.worldId)
            && Double.compare(x, snapshot.x) == 0
            && Double.compare(y, snapshot.y) == 0
            && Double.compare(z, snapshot.z) == 0;
    }

    @Override
    public int hashCode() {
        int result = worldId == null ? 0 : worldId.hashCode();
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        long zBits = Double.doubleToLongBits(z);
        result = 31 * result + (int) (xBits ^ (xBits >>> 32));
        result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        result = 31 * result + (int) (zBits ^ (zBits >>> 32));
        return result;
    }
}
