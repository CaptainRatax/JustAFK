# How the AFK system works

JustAFK keeps a live state for each tracked online player. It does not persist state, timers, or history to disk.

## Automatic detection

While JustAFK is globally enabled, tracking begins when a player joins. Under the same condition, players already online when the Bukkit plugin enables are also registered. A newly tracked player begins active with an inactivity timestamp set at registration.

Every 20 server ticks, JustAFK schedules a check for each online player. If all of the following are true, the player becomes AFK on the first check at or after the configured timeout:

1. `enabled` is `true`;
2. `automatic-afk-enabled` is `true`;
3. no recognized activity has refreshed the player's inactivity timestamp; and
4. `inactivity-timeout-seconds` has elapsed.

The timeout uses elapsed wall-clock milliseconds, while observation happens on the periodic monitor pass. The AFK duration starts when the transition is applied, so a new player-list label begins at `0m` rather than showing the preceding inactive time.

## Recognized activity

When processed, recognized activity refreshes the inactivity timestamp and returns an AFK player to active.

| Activity | Details |
| --- | --- |
| Movement input | Forward, backward, left, right, jump, sneak, or sprint. Input events catch taps; polling catches held input. |
| Chat | Submitting a chat message while still online. |
| Blocks | Breaking or placing a block. |
| Fishing | Player-driven fishing states such as casting or reeling; a server-driven `BITE` alone is excluded. |
| Block and item interaction | Bukkit player interactions other than `PHYSICAL`. This includes the handled block/item-use interactions. |
| Entity interaction | Both normal and position-specific player interaction with an entity. |

These action handlers observe cancelled events at monitor priority. For example, a block break or interaction denied by another plugin still counts as evidence of player activity.

### Activity that is not inferred

JustAFK does not use position changes as a proxy for input. Looking around and server- or world-driven movement do not reset the timer. This includes pushes, knockback, explosions, flowing water, pistons, server teleports, death, and respawn unless the player also produces a separately recognized input or action.

A `PHYSICAL` block interaction and a fishing `BITE` alone do not count. The current implementation also has no dedicated activity handler for commands, inventory clicks, item drops or pickups, item swaps, or combat damage/attacks. Those actions do not by themselves refresh the timer, although movement or another recognized event occurring alongside them can do so.

## Manual AFK state

A player with `justafk.use` can toggle their own state with `/afk`. A sender with the additional `justafk.others` permission can use `/afk <player> [on|off|toggle]` for an online target.

Manual AFK remains available when automatic detection is disabled. Recognized activity still clears a manual AFK state. Leaving AFK manually also starts a fresh inactivity window.

Unrelated commands are not treated as activity. The `/afk` command changes AFK state directly rather than being counted as a generic command action.

## State-change effects

An actual active-to-AFK or AFK-to-active transition can have three effects:

1. JustAFK adds or removes the configured player-list prefix.
2. It publishes its Bukkit `PlayerAfkStateChangeEvent`.
3. It sends the configured announcement to the selected audience.

Repeated requests for the state a player already has do not repeat the event or announcement. See [Messages and formatting](/configuration/messages) and the [Public API](/development/api) for their exact behavior.

## Player-list duration

When `player-list.enabled` is true, JustAFK prepends the configured format to the player's existing tab-list name. `{duration}` renders as:

- `0m` through `59m` below one hour; or
- whole hours such as `1h` or `2h` from one hour onward.

Minutes are not appended to an hour value. The display is refreshed by the monitor pass. JustAFK tracks the last value it wrote so it can preserve newer changes made by another name-formatting plugin where possible.

## Disabling and lifecycle

Setting `automatic-afk-enabled: false` prevents new timeout transitions but leaves existing AFK states and timers intact. Re-enabling it can immediately affect a sufficiently inactive player on the next check.

Setting `enabled: false` disables state management, clears tracked AFK states, restores player-list names, and blocks `/afk`. This cleanup publishes an active API event for a player who was AFK, but it does not send the normal active announcement.

Enabling it again registers currently online players with a fresh active state and inactivity window.

Quitting removes a player's tracking state and player-list decoration without publishing an active transition. Plugin shutdown also clears tracking without an active event. A later join or startup begins a new active session and timer.

## Scope of the plugin

AFK is a status only. JustAFK does not freeze players, cancel their actions, make them invulnerable, exempt them from AFK detection, or disable a server's own idle-kick behavior.
