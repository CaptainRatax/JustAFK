# Troubleshooting

## Plugin does not enable

Start with the complete server log around the JustAFK load attempt. Java bytecode and API-baseline failures can occur before JustAFK's `onEnable()` method runs, in which case the server reports its own loader error.

If startup reaches JustAFK but its initialization fails, the plugin logs this message with the underlying exception and disables itself:

```text
JustAFK could not be enabled.
```

Common source-backed causes are below.

### Server is below the API baseline

JustAFK declares `api-version: 1.21.3` and uses the movement-input API. Versions below 1.21.3 are unsupported and should reject the plugin. Use a server in the [supported compatibility range](/getting-started/requirements).

### Java reports an unsupported class version

The plugin JAR contains Java 21 bytecode. Run the server on Java 21 or newer and also satisfy the server software's own Java requirement. The repository specifically requires Java 25 when running Paper 26.2.

### `config.yml` is malformed or invalid

Check `plugins/JustAFK/config.yml` for YAML indentation and quoting errors. Then verify these constraints:

- `inactivity-timeout-seconds` is a number of at least `1`;
- `announcements.audience` is `all`, `ops`, or `none`; and
- `player-list.format` contains the exact `{duration}` placeholder, even if the player-list display is disabled.

Use unquoted `true` and `false` for Boolean values. A quoted value is a YAML string, and Bukkit may silently use the fallback instead of interpreting it as a Boolean.

If a manual `/justafk reload` fails during YAML parsing or settings validation, the previous valid settings remain active. Fix the reported error and reload again. If initial startup fails, fix the file and restart the server.

## Commands are unavailable

### `/afk` says JustAFK is disabled

Run `/justafk status` and check the global `enabled` setting. Restore it with:

```text
/justafk set enabled on
```

The `/justafk` administration command remains usable while the global feature is disabled.

### A player gets a permission error

Check all permissions required by the command:

- self-toggle: `justafk.use`;
- targeted state change: both `justafk.use` and `justafk.others`;
- configuration commands: `justafk.config`.

`justafk.admin` grants all three as children. Supplying any player name to `/afk` requires `justafk.others`, including the sender's own name. See [Permissions](/usage/permissions).

### A target cannot be found

`/afk <player>` accepts an exact online player name, matched without case sensitivity. It does not accept partial names, UUIDs, or offline players.

### Console cannot run `/afk`

The no-argument form represents the sender's own state and requires a player. From console, supply an online target:

```text
/afk Alex on
```

## Automatic AFK does not trigger

Confirm that:

1. `enabled` is `true`;
2. `automatic-afk-enabled` is `true`;
3. `inactivity-timeout-seconds` is the intended value; and
4. the player is not producing a recognized or held movement input.

JustAFK checks each player every 20 server ticks. The transition appears on the first check after the wall-clock timeout is met. Changing the timeout does not restart an existing inactivity window.

Turning automatic detection off does not itself clear a player who is already AFK. While that player remains online and JustAFK remains globally enabled, the state persists until recognized activity or a manual `off`/toggle transition.

## AFK clears unexpectedly

Chat, movement input, block break/place, player-driven fishing, supported block/item interactions, and entity interactions count as activity. Their events count even when another plugin cancels the attempted action.

Use [How the AFK system works](/usage/afk-system) for the exact recognized and ignored activity lists.

## Announcements are missing or reach the wrong audience

Check `announcements.audience`:

- `all` means every online player;
- `ops` means players with actual Bukkit operator status; and
- `none` disables announcements.

The console never receives state announcements. `ops` does not check `justafk.admin` or another permission.

The `messages.command-prefix` value affects command feedback, not the two announcement strings. Include the desired prefix directly in `messages.became-afk` and `messages.became-active`.

## Player-list prefix issues

Check all of the following:

- global `enabled` is `true`;
- `player-list.enabled` is `true`;
- the player is currently AFK; and
- `player-list.format` contains `{duration}`.

JustAFK prepends its format to the existing player-list name. If another plugin writes a newer name, JustAFK adopts it as the new base and avoids overwriting it during restoration. A plugin that continually rewrites the same field can still cause visible contention; there is no named tab-plugin integration.

After changing only `player-list.format` and reloading, an already-AFK player can retain the previous format until the displayed duration label next changes. Leaving and re-entering AFK also applies the new format. Below one hour labels change at minute boundaries; from one hour onward they change at hour boundaries.

## State disappears after quit or restart

This is expected. AFK states and timers are held only in memory. They are removed when a player quits and cleared when the plugin shuts down. There is no player database, history, or cross-server synchronization.

## A new JAR did not take effect

`/justafk reload` reloads only the YAML configuration. Stop the server, replace the old JAR, ensure only one JustAFK JAR is in `plugins`, and start the server again. Follow the [updating procedure](/getting-started/updating).

## Configuration comments disappeared

JustAFK requests comment-preserving YAML behavior when the server API provides it. Older YAML implementations may discard comments when a runtime `set` command saves the file. The configuration values still remain; keep a backup if comments are important.
