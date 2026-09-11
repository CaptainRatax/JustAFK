# Configuration reference

JustAFK has one user configuration file:

```text
plugins/JustAFK/config.yml
```

The file is created from the bundled resource on first start and is never overwritten on later starts. JustAFK reads and validates a complete settings snapshot before making it active.

## Default configuration

```yaml
# Enables or disables JustAFK as a whole.
# When disabled, only the help, status, reload, and configuration commands work.
enabled: true

# Marks inactive players as AFK automatically after the configured timeout.
# Manual AFK commands remain available when this is disabled.
automatic-afk-enabled: true

# Time without recognized player activity before a player is marked as AFK.
# Activity includes movement input, chat messages, breaking or placing blocks,
# player-driven fishing actions, block, item, and entity interactions, and item use.
# The default is 300 seconds (5 minutes).
inactivity-timeout-seconds: 300

announcements:
  # Available values: all, ops, none
  audience: all

player-list:
  # Adds a grey AFK duration before the player's existing tab-list name.
  enabled: true
  format: "&7[AFK {duration}]&r "

messages:
  # Legacy colour codes using '&' are supported.
  command-prefix: "&8[&7JustAFK&8]&r "
  became-afk: "&8[&7JustAFK&8]&r &7{player} is now AFK."
  became-active: "&8[&7JustAFK&8]&r &7{player} is no longer AFK."
```

## Settings

| YAML path | Type | Default | What it controls |
| --- | --- | --- | --- |
| `enabled` | Boolean | `true` | Global switch for AFK tracking, `/afk`, announcements, and player-list changes. |
| `automatic-afk-enabled` | Boolean | `true` | Whether inactivity may automatically mark a player AFK. |
| `inactivity-timeout-seconds` | 64-bit integer | `300` | Required inactivity, in whole seconds, before automatic AFK. Minimum `1`. |
| `announcements.audience` | String | `all` | Recipients for AFK and active announcements: `all`, `ops`, or `none`. |
| `player-list.enabled` | Boolean | `true` | Whether an AFK-duration prefix is shown in the tab player list. |
| `player-list.format` | String | `"&7[AFK {duration}]&r "` | Prefix placed before the player's existing player-list name. Must contain `{duration}`. |
| `messages.command-prefix` | String | `"&8[&7JustAFK&8]&r "` | Prefix used by JustAFK's prefixed command messages and administrative target notices. |
| `messages.became-afk` | String | Default shown above | Announcement used for a transition to AFK. Supports `{player}`. |
| `messages.became-active` | String | Default shown above | Announcement used for a transition back to active. Supports `{player}`. |

Use unquoted YAML booleans (`true` or `false`) and a whole numeric timeout. Bukkit's YAML accessors can fall back silently for values of the wrong type, so a value such as `enabled: "false"` should not be used.

### `enabled`

When `false`, JustAFK clears tracked AFK states, restores player-list names, ignores activity, stops automatic transitions, and rejects `/afk`. The following remain available so an administrator can inspect and enable the plugin again:

- `/justafk` and `/justafk help`
- `/justafk status`
- `/justafk reload`
- `/justafk set ...`

`automatic-afk-enabled` has no effect while this global setting is off.

Enabling JustAFK again registers online players with a fresh active state and inactivity window.

### `automatic-afk-enabled`

When `false`, inactivity alone cannot put a player into AFK. Manual `/afk` changes still work, and recognized activity still returns an AFK player to active.

Changing this setting does not clear current AFK states or reset inactivity timestamps. After it is enabled again, a player whose existing inactivity already exceeds the timeout can become AFK on the next monitor pass.

### `inactivity-timeout-seconds`

The timeout must be at least one second. The plugin defines no upper limit below Java's positive signed 64-bit integer range. The monitor checks players every 20 server ticks and uses elapsed wall-clock time, so it applies the transition on the first monitor pass at or after the threshold.

Changing the timeout does not reset current inactivity windows. Shortening it can therefore affect a player on the next check.

### `announcements.audience`

Values are case-insensitive and surrounding whitespace is ignored.

| Value | Recipients |
| --- | --- |
| `all` | Every online player, including the player whose state changed. |
| `ops` | Online players for whom Bukkit reports operator status. This is not a permission check. |
| `none` | Nobody. |

Announcements are not sent to the server console. See [Messages and formatting](/configuration/messages) for message placeholders.

### `player-list.enabled`

When enabled, JustAFK prepends `player-list.format` while a player is AFK. It remembers the existing player-list name and restores it when the player becomes active, quits, the display is disabled, or JustAFK stops tracking.

If another plugin changes the player-list name after JustAFK last wrote it, JustAFK adopts that newer name as its base. During restoration it also avoids overwriting a newer value owned by another plugin. This is defensive coexistence behavior, not a guarantee for every tab-list plugin.

### `player-list.format`

The literal, case-sensitive `{duration}` placeholder is required, even when `player-list.enabled` is `false`. Omitting it makes the configuration invalid. The configured string is a prefix, so retain a trailing space or another separator if one is wanted before the player's name.

Duration values are whole minutes below one hour (`0m` through `59m`) and whole hours from one hour onward (`1h`, `2h`, and so on). A player's displayed AFK duration starts at the moment they are marked AFK, not at the start of the preceding inactivity period.

## Applying changes

After editing the file manually, run:

```text
/justafk reload
```

A successful reload applies the new settings. If parsing or validation fails, the command reports the error and the previous live settings remain in use. An invalid configuration during initial plugin startup prevents JustAFK from enabling.

Several settings can be changed and saved directly:

| Setting | Runtime command |
| --- | --- |
| `enabled` | `/justafk set enabled <on|off>` |
| `automatic-afk-enabled` | `/justafk set automatic-afk <on|off>` |
| `inactivity-timeout-seconds` | `/justafk set timeout <seconds>` |
| `announcements.audience` | `/justafk set announcements <all|ops|none>` |
| `player-list.enabled` | `/justafk set playerlist <on|off>` |

These commands save `config.yml` immediately. The player-list format and all three message strings require a manual edit followed by reload.

!> Do not edit the file and then run a `set` command before reloading. A `set` command saves the configuration snapshot already held in memory and can overwrite unsaved-on-server manual edits. Run `/justafk reload` first, then use runtime setters.

?> JustAFK asks Bukkit to preserve comments when the server's YAML implementation supports it. On implementations without that support, a runtime command that saves the file may discard comments without discarding values.
