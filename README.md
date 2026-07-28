# JustAFK

JustAFK is a lightweight AFK plugin for Bukkit-compatible Minecraft servers. It
marks players as AFK after a configurable period without a position change,
provides manual and administrative commands, announces state changes, and can
show the AFK duration in the player list.

## Features

- Automatic AFK detection with a default timeout of 300 seconds.
- `/afk` command for every player.
- Administrative control over any online player's AFK state.
- Configurable announcements for everyone, operators only, or nobody.
- Optional grey `[AFK 5m]` / `[AFK 2h]` prefix in the tab player list.
- Runtime configuration commands with tab completion.
- Player-list name preservation for compatibility with other formatting plugins.
- Folia-aware scheduling without adding a separate runtime dependency.
- No database, NMS, or external runtime libraries.

Only changes to the player's world position count as movement. Looking around
without changing X, Y, or Z does not reset the inactivity timer. Movement also
immediately removes the AFK state. The displayed duration starts when the player
is marked as AFK, so a newly AFK player is shown as `[AFK 0m]`.

## Compatibility

| Server | Status |
| --- | --- |
| Paper 1.8.8–26.2 | Compatible; runtime-tested at both endpoints, with 26.2 as the primary target |
| Purpur 1.14.1–26.2 | Compatible Paper fork; runtime-tested on 26.2 |
| Folia 1.19.4–26.2 | Uses Folia schedulers; runtime-tested on the latest public 26.1.2 build |
| Spigot / CraftBukkit 1.8.8–26.2 | Compatibility-compiled against Spigot 1.8.8; best-effort runtime support |
| Sponge | Not supported |
| BungeeCord / Velocity / Waterfall | Not supported; these are proxy platforms |

Sponge uses a different plugin API and lifecycle. Supporting it cleanly would
require a separate platform module rather than a small compatibility layer.

Version 1.0.1 is built against Paper 26.2 and compatibility-compiled against
Spigot 1.8.8. Both compilations must produce identical class files before the
build passes. A 1.0.1 JAR from this source and build configuration was
smoke-tested on Paper 1.8.8, Paper 26.2, Purpur 26.2, and the latest published
Folia 26.1.2 build.

At the time of the 1.0.1 release, PaperMC had not published a Folia 26.2 server
build. The scheduler API used by JustAFK is available throughout the stated
Folia range, and the 26.2 source branch is ready, but the latest runtime
validation is Folia 26.1.2.

The JAR uses Java 8 bytecode so it can run across the full version range. The
server itself must still use the Java version required by its Minecraft
version; Paper 26.2 requires Java 25.

`api-version: 1.13` is intentional. Modern servers use the oldest supported API
declaration instead of treating the plugin as legacy, while older Bukkit
loaders ignore the field. This was verified with Paper 1.8.8.

## Installation

1. Download or build `JustAFK-1.0.1.jar`.
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
| `/justafk set timeout <seconds>` | Change the automatic AFK timeout. | `justafk.config` |
| `/justafk set announcements <all\|ops\|none>` | Change the announcement audience. | `justafk.config` |
| `/justafk set playerlist <on\|off>` | Enable or disable the tab-list prefix. | `justafk.config` |

Running `/afk <player>` without an action defaults to `toggle`.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `justafk.use` | Everyone | Use `/afk` for yourself. |
| `justafk.others` | Operators | Change another player's AFK state. |
| `justafk.config` | Operators | Configuration access. |
| `justafk.admin` | Operators | Grants every JustAFK permission. |

## Configuration

```yaml
# Time without a position change before a player is marked as AFK.
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

YAML comments are preserved when the server configuration API supports comment
parsing. Configuration values still load and save on older servers, but their
YAML implementation may discard comments during a save.

## Building

The project uses Gradle 9.1 and a Java 25 toolchain. The release JAR is compiled
against the official Paper 26.2 build 84 stable API and emits Java 8 bytecode.

```bash
./gradlew build
```

The build also compiles the same source against Spigot 1.8.8, compares both
class outputs, runs the unit tests, verifies the Java 8 class-file version, and
checks that no runtime dependencies are declared.

The plugin JAR will be created at:

```text
build/libs/JustAFK-1.0.1.jar
```

Tests can be run separately with:

```bash
./gradlew test
```

## Licence

JustAFK is available under the [MIT License](LICENSE).
