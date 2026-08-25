package pt.captainratax.justafk.afk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import pt.captainratax.justafk.api.JustAfkApi;
import pt.captainratax.justafk.api.event.PlayerAfkStateChangeEvent;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.JustAfkConfig;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.util.ColorText;
import pt.captainratax.justafk.util.DurationFormatter;

/**
 * Coordinates AFK state changes and their player-facing side effects.
 */
public final class AfkManager implements JustAfkApi {

    private final Supplier<PluginSettings> settingsSupplier;
    private final PlatformScheduler scheduler;
    private final LongSupplier currentTimeMillis;
    private final Consumer<PlayerAfkStateChangeEvent> stateChangeEventPublisher;
    private final Map<UUID, TrackedPlayer> trackedPlayers = new ConcurrentHashMap<>();

    public AfkManager(JustAfkConfig config, PlatformScheduler scheduler) {
        this(
            config::settings,
            scheduler,
            System::currentTimeMillis,
            AfkManager::publishStateChangeEvent
        );
    }

    AfkManager(
        JustAfkConfig config,
        PlatformScheduler scheduler,
        LongSupplier currentTimeMillis
    ) {
        this(
            config::settings,
            scheduler,
            currentTimeMillis,
            AfkManager::publishStateChangeEvent
        );
    }

    AfkManager(
        Supplier<PluginSettings> settingsSupplier,
        PlatformScheduler scheduler,
        LongSupplier currentTimeMillis
    ) {
        this(
            settingsSupplier,
            scheduler,
            currentTimeMillis,
            AfkManager::publishStateChangeEvent
        );
    }

    AfkManager(
        Supplier<PluginSettings> settingsSupplier,
        PlatformScheduler scheduler,
        LongSupplier currentTimeMillis,
        Consumer<PlayerAfkStateChangeEvent> stateChangeEventPublisher
    ) {
        this.settingsSupplier = Objects.requireNonNull(
            settingsSupplier,
            "settingsSupplier"
        );
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.currentTimeMillis = Objects.requireNonNull(
            currentTimeMillis,
            "currentTimeMillis"
        );
        this.stateChangeEventPublisher = Objects.requireNonNull(
            stateChangeEventPublisher,
            "stateChangeEventPublisher"
        );
    }

    public void register(Player player) {
        getOrRegister(player);
    }

    public void unregister(Player player) {
        TrackedPlayer tracked = trackedPlayers.remove(player.getUniqueId());
        if (tracked != null) {
            deactivateTrackedPlayer(player, tracked, false);
        }
    }

    public void recordInput(Player player, Input input) {
        if (!settings().enabled() || !PlayerInputActivity.isActive(input)) {
            return;
        }

        recordActivity(player);
    }

    /**
     * Records a real player action and clears AFK when necessary.
     *
     * @param player the player who acted
     */
    public void recordActivity(Player player) {
        if (!settings().enabled()) {
            return;
        }

        TrackedPlayer tracked = getOrRegister(player);
        if (tracked == null) {
            return;
        }
        applyTransition(
            player,
            tracked,
            tracked.afkState.recordActivity(currentTimeMillis.getAsLong()),
            false
        );
    }

    public void checkOnlinePlayers() {
        if (!settings().enabled()) {
            return;
        }

        // The global task only fans out; each player is checked on their own scheduler.
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runForPlayer(player, () -> checkPlayer(player));
        }
    }

    @Override
    public boolean isAfk(Player player) {
        Objects.requireNonNull(player, "player");
        return isAfk(player.getUniqueId());
    }

    @Override
    public boolean isAfk(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        TrackedPlayer tracked = trackedPlayers.get(playerId);
        return tracked != null && tracked.afkState.isAfk();
    }

    public boolean setAfk(Player player, boolean afk) {
        TrackedPlayer tracked = getOrRegister(player);
        if (tracked == null) {
            return false;
        }

        AfkTransition transition = tracked.afkState.setAfk(
            afk,
            currentTimeMillis.getAsLong()
        );
        applyTransition(player, tracked, transition, false);
        return tracked.afkState.isAfk();
    }

    public boolean toggleAfk(Player player) {
        return setAfk(player, !isAfk(player));
    }

    public void refreshPlayerLists() {
        if (!settings().enabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runForPlayer(player, () -> {
                if (!settings().enabled()) {
                    return;
                }
                TrackedPlayer tracked = getOrRegister(player);
                if (tracked != null) {
                    refreshPlayerList(player, tracked, currentTimeMillis.getAsLong());
                }
            });
        }
    }

    public void applyConfiguration() {
        if (!settings().enabled()) {
            clearTrackedPlayers(true);
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runForPlayer(player, () -> {
                if (!settings().enabled() || !player.isOnline()) {
                    return;
                }
                TrackedPlayer tracked = getOrRegister(player);
                if (tracked != null) {
                    refreshPlayerList(player, tracked, currentTimeMillis.getAsLong());
                }
            });
        }
    }

    public void shutdown() {
        clearTrackedPlayers(false);
    }

    private void clearTrackedPlayers(boolean publishActiveEvents) {
        clearTrackedPlayers(Bukkit.getOnlinePlayers(), publishActiveEvents);
    }

    void clearTrackedPlayers(
        Iterable<? extends Player> onlinePlayers,
        boolean publishActiveEvents
    ) {
        Map<UUID, TrackedPlayer> playersToRestore = new HashMap<>();
        trackedPlayers.forEach((playerId, tracked) -> {
            if (trackedPlayers.remove(playerId, tracked)) {
                playersToRestore.put(playerId, tracked);
            }
        });

        for (Player player : onlinePlayers) {
            TrackedPlayer tracked = playersToRestore.remove(player.getUniqueId());
            if (tracked != null) {
                scheduler.runForPlayer(
                    player,
                    () -> deactivateTrackedPlayer(
                        player,
                        tracked,
                        publishActiveEvents
                    )
                );
            }
        }
    }

    void checkPlayer(Player player) {
        PluginSettings settings = settings();
        if (!settings.enabled() || !player.isOnline()) {
            return;
        }

        TrackedPlayer tracked = getOrRegister(player);
        if (tracked == null) {
            return;
        }
        long nowMillis = currentTimeMillis.getAsLong();

        AfkTransition transition;
        boolean automaticTransition = false;
        if (PlayerInputActivity.isActive(player.getCurrentInput())) {
            transition = tracked.afkState.recordActivity(nowMillis);
        } else if (settings.automaticAfkEnabled()) {
            automaticTransition = true;
            transition = tracked.afkState.tryAutomaticAfk(
                nowMillis,
                settings.inactivityTimeoutSeconds()
            );
        } else {
            transition = AfkTransition.NONE;
        }

        // A config command can run concurrently from another Folia region.
        if (
            automaticTransition
                && transition == AfkTransition.BECAME_AFK
                && !automaticTransitionAllowed()
        ) {
            cancelAutomaticTransition(player, tracked, nowMillis);
            transition = AfkTransition.NONE;
        }

        applyTransition(
            player,
            tracked,
            transition,
            automaticTransition
        );
        refreshPlayerList(player, tracked, nowMillis);
    }

    private TrackedPlayer getOrRegister(Player player) {
        if (!settings().enabled()) {
            return null;
        }

        UUID playerId = player.getUniqueId();
        TrackedPlayer tracked = trackedPlayers.computeIfAbsent(
            playerId,
            ignored -> new TrackedPlayer(
                currentTimeMillis.getAsLong()
            )
        );

        if (!settings().enabled()) {
            if (trackedPlayers.remove(playerId, tracked)) {
                deactivateTrackedPlayer(player, tracked, true);
            }
            return null;
        }
        return tracked;
    }

    private void applyTransition(
        Player player,
        TrackedPlayer tracked,
        AfkTransition transition,
        boolean automaticTransition
    ) {
        if (
            !settings().enabled()
                || trackedPlayers.get(player.getUniqueId()) != tracked
        ) {
            return;
        }

        if (transition == AfkTransition.BECAME_AFK) {
            if (automaticTransition && !automaticTransitionAllowed()) {
                cancelAutomaticTransition(
                    player,
                    tracked,
                    currentTimeMillis.getAsLong()
                );
                return;
            }

            refreshPlayerList(player, tracked, currentTimeMillis.getAsLong());
            if (automaticTransition && !automaticTransitionAllowed()) {
                cancelAutomaticTransition(
                    player,
                    tracked,
                    currentTimeMillis.getAsLong()
                );
                return;
            }
            if (!publishTransition(player, tracked, true, automaticTransition)) {
                return;
            }
            announce(player.getName(), true, automaticTransition);
        } else if (transition == AfkTransition.BECAME_ACTIVE) {
            restorePlayerListName(player, tracked);
            if (!publishTransition(player, tracked, false, false)) {
                return;
            }
            announce(player.getName(), false, false);
        }
    }

    private boolean automaticTransitionAllowed() {
        PluginSettings settings = settings();
        return settings.enabled() && settings.automaticAfkEnabled();
    }

    private boolean transitionStillCurrent(
        Player player,
        TrackedPlayer tracked,
        boolean afk
    ) {
        return settings().enabled()
            && trackedPlayers.get(player.getUniqueId()) == tracked
            && tracked.afkState.isAfk() == afk;
    }

    private boolean publishTransition(
        Player player,
        TrackedPlayer tracked,
        boolean afk,
        boolean automaticTransition
    ) {
        PlayerAfkStateChangeEvent event;
        synchronized (tracked) {
            if (
                !transitionStillCurrent(player, tracked, afk)
                    || tracked.afkEventPublished == afk
            ) {
                return false;
            }
            tracked.afkEventPublished = afk;
            event = new PlayerAfkStateChangeEvent(
                player,
                afk,
                automaticTransition
            );
        }

        stateChangeEventPublisher.accept(event);
        return true;
    }

    private void deactivateTrackedPlayer(
        Player player,
        TrackedPlayer tracked,
        boolean publishActiveEvent
    ) {
        boolean shouldPublish;
        synchronized (tracked) {
            shouldPublish = publishActiveEvent && tracked.afkEventPublished;
            tracked.afkEventPublished = false;
            tracked.afkState.setAfk(false, currentTimeMillis.getAsLong());
        }

        restorePlayerListName(player, tracked);
        if (shouldPublish && !isAfk(player.getUniqueId())) {
            stateChangeEventPublisher.accept(
                new PlayerAfkStateChangeEvent(player, false, false)
            );
        }
    }

    private void cancelAutomaticTransition(
        Player player,
        TrackedPlayer tracked,
        long nowMillis
    ) {
        if (trackedPlayers.get(player.getUniqueId()) != tracked) {
            return;
        }

        tracked.afkState.setAfk(false, nowMillis);
        restorePlayerListName(player, tracked);
    }

    private void refreshPlayerList(
        Player player,
        TrackedPlayer tracked,
        long nowMillis
    ) {
        PluginSettings settings = settings();
        if (
            !settings.enabled()
                || trackedPlayers.get(player.getUniqueId()) != tracked
                || !settings.showInPlayerList()
                || !tracked.afkState.isAfk()
        ) {
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

        settings = settings();
        if (
            !settings.enabled()
                || trackedPlayers.get(player.getUniqueId()) != tracked
                || !settings.showInPlayerList()
                || !tracked.afkState.isAfk()
        ) {
            restorePlayerListName(player, tracked);
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

    private void announce(
        String playerName,
        boolean becameAfk,
        boolean automaticTransition
    ) {
        PluginSettings settings = settings();
        if (
            !settings.enabled()
                || (
                    automaticTransition
                        && !automaticTransitionAllowed()
                )
        ) {
            return;
        }

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
                    settings().enabled()
                        && (
                            !automaticTransition
                                || automaticTransitionAllowed()
                        )
                        && recipient.isOnline()
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

    private PluginSettings settings() {
        return settingsSupplier.get();
    }

    private static void publishStateChangeEvent(
        PlayerAfkStateChangeEvent event
    ) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
