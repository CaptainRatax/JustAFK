# Commands

JustAFK registers two commands: `/afk` and `/justafk`. Only `/justafk` has an alias: `/jafk`.

## Reference

| Syntax | Description | Permission | Console |
| --- | --- | --- | --- |
| `/afk` | Toggle the sender's own AFK state. | `justafk.use` | No; a player is required. |
| `/afk <player> [on\|off\|toggle]` | Set or toggle an online player's state. The action defaults to `toggle`. | `justafk.use` and `justafk.others` | Yes, with a target. |
| `/justafk` | Show command help. | `justafk.config` | Yes |
| `/justafk help` | Show command help. | `justafk.config` | Yes |
| `/justafk status` | Show the live enable switches, timeout, announcement audience, and player-list setting. | `justafk.config` | Yes |
| `/justafk reload` | Reload and apply `config.yml`. | `justafk.config` | Yes |
| `/justafk set enabled <on\|off>` | Enable or disable JustAFK globally. | `justafk.config` | Yes |
| `/justafk set automatic-afk <on\|off>` | Enable or disable inactivity-based AFK transitions. | `justafk.config` | Yes |
| `/justafk set timeout <seconds>` | Set the inactivity timeout to a whole number of at least `1`. | `justafk.config` | Yes |
| `/justafk set announcements <all\|ops\|none>` | Choose who receives state announcements. | `justafk.config` | Yes |
| `/justafk set playerlist <on\|off>` | Enable or disable the AFK prefix in the tab player list. | `justafk.config` | Yes |

All `/justafk` forms can also use `/jafk`. There are no per-subcommand permissions.

## `/afk`

### Toggle your own state

```text
/afk
```

This form is player-only. It toggles active to AFK or AFK to active and sends a confirmation to the player.

### Change an online player

```text
/afk Alex
/afk Alex on
/afk Alex off
/afk Alex toggle
```

The target must be an exact online player name, matched case-insensitively. UUIDs, partial names, and offline players are not accepted. Actions are also case-insensitive.

Supplying any argument invokes the targeted form and therefore requires `justafk.others`, even if the named target is the sender. The base `justafk.use` permission is still required by the command declaration.

The sender receives the resulting state. When sender and target are different, the target also receives a notice naming the sender. An explicit `on` or `off` still produces these command notices when the player already has the requested state, but duplicate state-change announcements and API events are not emitted. Calling `on` for an already-AFK player also leaves the existing AFK start time and displayed duration unchanged.

Leaving AFK with `off` starts a fresh inactivity window. Calling `off` for an already-active target also refreshes that inactivity timestamp.

!> Every `/afk` form is unavailable while `enabled: false`.

## `/justafk`

### Status and reload

```text
/justafk status
/justafk reload
```

`status` reports the live settings snapshot. `reload` parses and validates the file before applying it. If parsing or validation fails, the error is shown and the prior live settings remain active.

### Runtime settings

Runtime changes are written to `plugins/JustAFK/config.yml` immediately:

```text
/justafk set enabled off
/justafk set automatic-afk on
/justafk set timeout 600
/justafk set announcements ops
/justafk set playerlist off
```

For Boolean settings, the command accepts these case-insensitive values:

| Meaning | Accepted values |
| --- | --- |
| Enabled | `on`, `true`, `yes`, `enabled` |
| Disabled | `off`, `false`, `no`, `disabled` |

Tab completion and help present `on` and `off`, but the additional forms above are implemented. The timeout command accepts a whole number of seconds and rejects values below `1`. Announcement values are `all`, `ops`, or `none`; surrounding whitespace is ignored by the configuration parser, although command arguments cannot contain spaces.

Changing `enabled` applies the global state. Changing `playerlist` refreshes or restores online names; on Folia, player-specific work is queued on the appropriate entity scheduler. The other values are read on subsequent activity or monitor operations; they do not reset current timers or clear current AFK states.

If you edited `config.yml` directly since the last load, run `/justafk reload` before a `set` command. Runtime setters save the in-memory configuration and can otherwise overwrite those manual disk edits.

Unlike `/afk`, `/justafk` remains available while JustAFK's global `enabled` setting is false.

## Tab completion

JustAFK completes subcommands, setting names, common values, and online player names. `/afk` target and action suggestions are hidden when JustAFK is globally disabled or the sender lacks `justafk.others`.
