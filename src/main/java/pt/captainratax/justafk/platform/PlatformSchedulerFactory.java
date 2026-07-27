package pt.captainratax.justafk.platform;

import org.bukkit.plugin.java.JavaPlugin;

public final class PlatformSchedulerFactory {

    private PlatformSchedulerFactory() {
    }

    public static PlatformScheduler create(JavaPlugin plugin) {
        if (isFolia()) {
            return new FoliaPlatformScheduler(plugin);
        }
        return new BukkitPlatformScheduler(plugin);
    }

    private static boolean isFolia() {
        try {
            // Looking it up by name keeps Bukkit from resolving Folia classes.
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
