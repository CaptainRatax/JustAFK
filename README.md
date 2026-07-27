# JustAFK

JustAFK is a lightweight AFK plugin for modern Minecraft servers. It marks
players as AFK after a configurable period without a position change, provides
manual and administrative commands, announces state changes, and can show the
AFK duration in the player list.

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
| Paper 26.2 | Primary target |
| Folia 26.2 | Folia-ready; see the validation note below |
| Purpur 26.2 | Supported as a Paper fork |
| Spigot / CraftBukkit 26.2 | Best-effort support through the Bukkit API |
| Sponge | Not supported |

Sponge uses a different plugin API and lifecycle. Supporting it cleanly would
require a separate platform module rather than a small compatibility layer.

At the time of the 1.0.0 release, PaperMC had not published a Folia 26.2 build.
The Folia scheduler path was therefore smoke-tested on the latest public Folia
26.1.2 build with a temporary 26.1.2 test manifest. The released JAR keeps
`api-version: 26.2` and is ready for the matching Folia release.

Minecraft 26.2 servers require Java 25.

## Installation

1. Download or build `JustAFK-1.0.0.jar`.
2. Place the JAR in the server's `plugins` directory.
3. Restart the server.
4. Edit `plugins/JustAFK/config.yml` if needed, then run `/justafk reload`.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/afk` | Toggle your own AFK state. | `justafk.use` |
| `/afk <player> [on\|off\|toggle]` | Change an online player's AFK state. | `justafk.others` |
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

## Building

The project uses Gradle and the official Paper 26.2 build 84 stable API.

```bash
./gradlew build
```

The plugin JAR will be created at:

```text
build/libs/JustAFK-1.0.0.jar
```

Tests can be run separately with:

```bash
./gradlew test
```

## Licence

JustAFK is available under the [MIT License](LICENSE).
