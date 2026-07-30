# JustAFK

JustAFK is a lightweight AFK plugin for Bukkit-compatible Minecraft servers. It
marks players as AFK after a configurable period without movement input,
provides manual and administrative commands, announces state changes, and can
show the AFK duration in the player list.

## Features

- Global enable/disable control that leaves configuration commands available.
- Independently configurable automatic AFK detection.
- Input-aware automatic AFK detection with a default timeout of 300 seconds.
- `/afk` command for every player.
- Administrative control over any online player's AFK state.
- Configurable announcements for everyone, operators only, or nobody.
- Optional grey `[AFK 5m]` / `[AFK 2h]` prefix in the tab player list.
- Runtime configuration commands with tab completion.
- Player-list name preservation for compatibility with other formatting plugins.
- Folia-aware scheduling without adding a separate runtime dependency.
- No database, NMS, or external runtime libraries.

Forward, backward, left, right, jump, sneak, and sprint inputs count as player
activity. Any of them immediately removes the AFK state and restarts the
inactivity timer. Position changes without movement input, including entity
pushes, knockback, explosions, water, pistons, server teleports, death, and
respawn, do not remove AFK or restart the timer. Looking around also remains
ignored. The displayed duration starts when the player is marked as AFK, so a
newly AFK player is shown as `[AFK 0m]`.

## Compatibility

| Server | Status |
| --- | --- |
| Paper 1.21.3–26.2 | Supported; Paper 26.2 is the primary build target |
| Purpur 1.21.3–26.2 | Supported Paper fork |
| Folia 1.21.3–26.2 | Supported where Folia publishes a server build; uses Folia schedulers |
| Spigot / CraftBukkit 1.21.3–26.2 | Compatibility-compiled against Spigot 1.21.3 |
| Sponge | Not supported |
| BungeeCord / Velocity / Waterfall | Not supported; these are proxy platforms |

Sponge uses a different plugin API and lifecycle. Supporting it cleanly would
require a separate platform module rather than a small compatibility layer.

Version 1.1.0 is built against Paper 26.2 and compatibility-compiled against
Spigot 1.21.3. Both compilations must produce identical class files before the
build passes. The 1.21.3 minimum is required because this version uses the
movement-input API rather than inferring activity from changes in position.

The JAR uses Java 21 bytecode. Servers may require a newer Java runtime for
their own Minecraft version; Paper 26.2 requires Java 25.

`api-version: 1.21.3` makes servers below the supported baseline refuse to load
the plugin instead of failing later when the movement-input API is used.

## Installation

1. Download or build `JustAFK-1.1.0.jar`.
2. Place the JAR in the server's `plugins` directory.
3. Restart the server.
4. Edit `plugins/JustAFK/config.yml` if needed, then run `/justafk reload`.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/afk` | Toggle your own AFK state. | `justafk.use` |
| `/afk <player> [on\|off\|toggle]` | Change an online player's AFK state. | `justafk.use` and `justafk.others` |
| `/justafk help` | Show the command list. | `justafk.config` |
| `/justafk status` | Show the active configuration. | `justafk.config` |
| `/justafk reload` | Reload `config.yml`. | `justafk.config` |
| `/justafk set enabled <on\|off>` | Enable or disable JustAFK globally. | `justafk.config` |
| `/justafk set automatic-afk <on\|off>` | Enable or disable automatic AFK detection. | `justafk.config` |
| `/justafk set timeout <seconds>` | Change the automatic AFK timeout. | `justafk.config` |
| `/justafk set announcements <all\|ops\|none>` | Change the announcement audience. | `justafk.config` |
| `/justafk set playerlist <on\|off>` | Enable or disable the tab-list prefix. | `justafk.config` |

Running `/afk <player>` without an action defaults to `toggle`.

When `enabled` is `false`, `/afk`, automatic AFK detection, AFK state
management, announcements, and player-list effects are disabled. The
`/justafk help`, `/justafk status`, `/justafk reload`, and `/justafk set ...`
commands remain available so the plugin can be inspected, configured, and
enabled again.

When only `automatic-afk-enabled` is `false`, inactivity no longer marks
players as AFK, but `/afk` and administrative AFK changes continue to work.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `justafk.use` | Everyone | Use `/afk` for yourself. |
| `justafk.others` | Operators | Change another player's AFK state. |
| `justafk.config` | Operators | Configuration access. |
| `justafk.admin` | Operators | Grants every JustAFK permission. |

## Configuration

```yaml
# Enables or disables JustAFK as a whole.
# When disabled, only the help, status, reload, and configuration commands work.
enabled: true

# Marks inactive players as AFK automatically after the configured timeout.
# Manual AFK commands remain available when this is disabled.
automatic-afk-enabled: true

# Time without movement input before a player is marked as AFK.
# Movement input includes forward, backward, left, right, jump, sneak, and sprint.
inactivity-timeout-seconds: 300

announcements:
  # Available values: all, ops, none
  audience: all

player-list:
  enabled: true
  format: "&7[AFK {duration}]&r "

messages:
  command-prefix: "&8[&7JustAFK&8]&r "
  became-afk: "&8[&7JustAFK&8]&r &7{player} is now AFK."
  became-active: "&8[&7JustAFK&8]&r &7{player} is no longer AFK."
```

The `{duration}` placeholder is required in `player-list.format`. The
`{player}` placeholder is available in both state-change messages. Legacy
colour codes using `&` are supported.

Configuration commands update `config.yml` immediately.
Configurations created by earlier releases remain compatible: when either new
toggle is absent, it defaults to `true`.

YAML comments are preserved when the server configuration API supports comment
parsing. Configuration values still load and save on older servers, but their
YAML implementation may discard comments during a save.

## Building

The project uses Gradle 9.1 and a Java 25 toolchain. The release JAR is compiled
against the official Paper 26.2 build 84 stable API and emits Java 21 bytecode.

```bash
./gradlew build
```

The build also compiles the same source against Spigot 1.21.3, compares both
class outputs, runs the unit tests, verifies the Java 21 class-file version, and
checks that no runtime dependencies are declared.

The plugin JAR will be created at:

```text
build/libs/JustAFK-1.1.0.jar
```

Tests can be run separately with:

```bash
./gradlew test
```

## Licence

JustAFK is available under the [MIT License](LICENSE).
