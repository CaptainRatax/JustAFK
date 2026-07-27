package pt.captainratax.justafk.afk;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.JustAfkConfig;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.util.ColorText;
import pt.captainratax.justafk.util.DurationFormatter;

/**
 * Coordinates AFK state changes and their player-facing side effects.
 */
public final class AfkManager {

    private final JustAfkConfig config;
    private final PlatformScheduler scheduler;
    private final LongSupplier currentTimeMillis;
    private final Map<UUID, TrackedPlayer> trackedPlayers = new ConcurrentHashMap<>();

    public AfkManager(JustAfkConfig config, PlatformScheduler scheduler) {
        this(config, scheduler, System::currentTimeMillis);
    }

    AfkManager(
        JustAfkConfig config,
        PlatformScheduler scheduler,
        LongSupplier currentTimeMillis
    ) {
        this.config = config;
        this.scheduler = scheduler;
        this.currentTimeMillis = currentTimeMillis;
    }

    public void register(Player player) {
        trackedPlayers.computeIfAbsent(
            player.getUniqueId(),
            ignored -> new TrackedPlayer(
                currentTimeMillis.getAsLong(),
                PositionSnapshot.from(player.getLocation())
            )
        );
    }

    public void unregister(Player player) {
        TrackedPlayer tracked = trackedPlayers.remove(player.getUniqueId());
        if (tracked != null) {
            restorePlayerListName(player, tracked);
        }
    }

    public void recordMovement(Player player, Location from, Location to) {
        if (PositionSnapshot.from(from).equals(PositionSnapshot.from(to))) {
            return;
        }

        TrackedPlayer tracked = getOrRegister(player);
        tracked.lastPosition = PositionSnapshot.from(to);
        applyTransition(
            player,
            tracked,
            tracked.afkState.recordActivity(currentTimeMillis.getAsLong())
        );
    }

    public void checkOnlinePlayers() {
        // The global task only fans out; each player is checked on their own scheduler.
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runForPlayer(player, () -> checkPlayer(player));
        }
    }

    public boolean isAfk(Player player) {
        return getOrRegister(player).afkState.isAfk();
    }

    public boolean setAfk(Player player, boolean afk) {
        TrackedPlayer tracked = getOrRegister(player);
        AfkTransition transition = tracked.afkState.setAfk(
            afk,
            currentTimeMillis.getAsLong()
        );
        applyTransition(player, tracked, transition);
        return tracked.afkState.isAfk();
    }

    public boolean toggleAfk(Player player) {
        return setAfk(player, !isAfk(player));
    }

    public void refreshPlayerLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runForPlayer(player, () -> {
                TrackedPlayer tracked = getOrRegister(player);
                refreshPlayerList(player, tracked, currentTimeMillis.getAsLong());
            });
        }
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            TrackedPlayer tracked = trackedPlayers.get(player.getUniqueId());
            if (tracked != null) {
                scheduler.runForPlayer(player, () -> restorePlayerListName(player, tracked));
            }
        }
        trackedPlayers.clear();
    }

    private void checkPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }

        TrackedPlayer tracked = getOrRegister(player);
        long nowMillis = currentTimeMillis.getAsLong();
        PositionSnapshot currentPosition = PositionSnapshot.from(player.getLocation());

        AfkTransition transition;
        if (!currentPosition.equals(tracked.lastPosition)) {
            // Looking around is ignored; only a position or world change counts as activity.
            tracked.lastPosition = currentPosition;
            transition = tracked.afkState.recordActivity(nowMillis);
        } else {
            transition = tracked.afkState.tryAutomaticAfk(
                nowMillis,
                config.settings().inactivityTimeoutSeconds()
            );
        }

        applyTransition(player, tracked, transition);
        refreshPlayerList(player, tracked, nowMillis);
    }

    private TrackedPlayer getOrRegister(Player player) {
        register(player);
        return trackedPlayers.get(player.getUniqueId());
    }

    private void applyTransition(
        Player player,
        TrackedPlayer tracked,
        AfkTransition transition
    ) {
        if (transition == AfkTransition.BECAME_AFK) {
            refreshPlayerList(player, tracked, currentTimeMillis.getAsLong());
            announce(player.getName(), true);
        } else if (transition == AfkTransition.BECAME_ACTIVE) {
            restorePlayerListName(player, tracked);
            announce(player.getName(), false);
        }
    }

    private void refreshPlayerList(
        Player player,
        TrackedPlayer tracked,
        long nowMillis
    ) {
        PluginSettings settings = config.settings();
        if (!settings.showInPlayerList() || !tracked.afkState.isAfk()) {
            restorePlayerListName(player, tracked);
            return;
        }

        String currentName = safePlayerListName(player);
        if (
            tracked.lastAppliedPlayerListName != null
                && !Objects.equals(currentName, tracked.lastAppliedPlayerListName)
        ) {
            // Another plugin changed the tab name, so adopt its value as the new base.
            tracked.originalPlayerListName = currentName;
            tracked.lastAppliedPlayerListName = null;
            tracked.lastDurationLabel = null;
        }

        if (tracked.originalPlayerListName == null) {
            tracked.originalPlayerListName = currentName;
        }

        String durationLabel = DurationFormatter.format(
            tracked.afkState.afkDurationMillis(nowMillis)
        );
        if (
            durationLabel.equals(tracked.lastDurationLabel)
                && Objects.equals(currentName, tracked.lastAppliedPlayerListName)
        ) {
            return;
        }

        String prefix = ColorText.colorize(
            settings.playerListFormat().replace("{duration}", durationLabel)
        );
        String updatedName = prefix + tracked.originalPlayerListName;
        player.setPlayerListName(updatedName);
        tracked.lastAppliedPlayerListName = updatedName;
        tracked.lastDurationLabel = durationLabel;
    }

    private void restorePlayerListName(Player player, TrackedPlayer tracked) {
        if (tracked.lastAppliedPlayerListName == null) {
            return;
        }

        String currentName = safePlayerListName(player);
        // Do not overwrite a newer value owned by another tab-list plugin.
        if (Objects.equals(currentName, tracked.lastAppliedPlayerListName)) {
            player.setPlayerListName(
                tracked.originalPlayerListName == null
                    ? player.getName()
                    : tracked.originalPlayerListName
            );
        }

        tracked.originalPlayerListName = null;
        tracked.lastAppliedPlayerListName = null;
        tracked.lastDurationLabel = null;
    }

    private String safePlayerListName(Player player) {
        String playerListName = player.getPlayerListName();
        return playerListName == null ? player.getName() : playerListName;
    }

    private void announce(String playerName, boolean becameAfk) {
        PluginSettings settings = config.settings();
        AnnouncementAudience audience = settings.announcementAudience();
        if (audience == AnnouncementAudience.NONE) {
            return;
        }

        String configuredMessage = becameAfk
            ? settings.becameAfkMessage()
            : settings.becameActiveMessage();
        String message = ColorText.colorize(
            configuredMessage.replace("{player}", playerName)
        );

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            // Message delivery also runs on the recipient's scheduler under Folia.
            scheduler.runForPlayer(recipient, () -> {
                if (
                    recipient.isOnline()
                        && (
                            audience == AnnouncementAudience.ALL
                                || recipient.isOp()
                        )
                ) {
                    recipient.sendMessage(message);
                }
            });
        }
    }
}
