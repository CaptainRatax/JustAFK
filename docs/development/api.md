# Public API

JustAFK 1.2.0 introduced an intentionally read-only public API. The supported surface consists of:

- `pt.captainratax.justafk.api.JustAfkApi`
- `pt.captainratax.justafk.api.event.PlayerAfkStateChangeEvent`

Other packages are implementation details even where a class or method has Java `public` visibility.

## Add JustAFK to a consumer plugin

This repository does not configure Maven publication or declare public dependency coordinates. To compile an integration, place a downloaded or locally built JustAFK JAR in the consumer project's `libs` directory and use it only at compile time. For a Gradle Kotlin DSL project:

```kotlin
dependencies {
    compileOnly(files("libs/JustAFK-1.2.0.jar"))
}
```

The consumer plugin should describe its relationship to JustAFK in its own `plugin.yml`. If the integration is required:

```yaml
depend: [JustAFK]
```

If the integration is optional, request load ordering but continue safely when the service is absent:

```yaml
softdepend: [JustAFK]
```

These entries belong to the consuming plugin. JustAFK itself has no plugin dependencies.

For an optional integration, keep classes that directly reference JustAFK API or event types behind the availability check. Do not instantiate or register those classes when JustAFK is absent.

## Obtain the service

JustAFK registers one `JustAfkApi` provider with Bukkit's `ServicesManager` at normal priority during its Bukkit enable lifecycle. It unregisters the provider when Bukkit disables the plugin.

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pt.captainratax.justafk.api.JustAfkApi;

public boolean isAfk(Player player) {
    JustAfkApi justAfk = Bukkit.getServicesManager().load(JustAfkApi.class);
    return justAfk != null && justAfk.isAfk(player);
}
```

Always handle a `null` provider when the integration is optional. The service is unavailable before the Bukkit plugin enables, after Bukkit disables it, or when it is not installed. Setting JustAFK's configuration key `enabled: false` does not unregister the service; it clears tracked state, so queries return `false`.

## Query AFK state

The interface exposes exactly two methods:

```java
import java.util.UUID;
import org.bukkit.entity.Player;

public interface JustAfkApi {
    boolean isAfk(Player player);
    boolean isAfk(UUID playerId);
}
```

Both methods read the state JustAFK already maintains. An unknown or untracked UUID returns `false` without creating a tracking entry. Player state is removed on quit, so this API is not an offline history lookup.

There is no supported method for setting or toggling AFK state from another plugin.

## Listen for state changes

Register a normal Bukkit listener for `PlayerAfkStateChangeEvent`:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pt.captainratax.justafk.api.event.PlayerAfkStateChangeEvent;

public final class AfkStateListener implements Listener {

    @EventHandler
    public void onAfkStateChange(PlayerAfkStateChangeEvent event) {
        String playerName = event.getPlayer().getName();

        if (event.isAfk()) {
            String cause = event.isAutomatic() ? "inactivity" : "manual";
            System.out.println(playerName + " became AFK via " + cause);
        } else {
            System.out.println(playerName + " became active");
        }
    }
}
```

Register the listener from the consuming plugin as usual:

```java
getServer().getPluginManager().registerEvents(new AfkStateListener(), this);
```

The event extends Bukkit's `PlayerEvent` and is not cancellable. It exposes the inherited `getPlayer()` plus:

| Method | Meaning |
| --- | --- |
| `isAfk()` | The new state: `true` for AFK and `false` for active. |
| `isAutomatic()` | `true` only when an inactivity timeout caused entry into AFK. |

`isAutomatic()` is `false` for manual transitions, recognized-activity transitions back to active, and active events caused by globally disabling JustAFK.

The event is emitted only when the tracked state actually changes. Repeated attempts to set the current state do not emit another event. Globally disabling JustAFK publishes active events for tracked AFK players, while player quit and plugin shutdown cleanup do not represent an active transition and do not publish one.

The API declares no callback-thread guarantee. In particular, a Folia consumer must not assume an event handler runs on one global main thread; keep follow-up work safe for the player's execution context and use the appropriate scheduler when changing server state.

## API boundaries

The public API provides current-state observation, not persistence, mutation, AFK history, or cross-server synchronization. Do not cast the service provider to its implementation or depend on classes outside the `api` packages; those classes are not presented by the project as stable extension points.
