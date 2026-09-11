# Messages and formatting

JustAFK stores its configurable messages in `config.yml`; there is no separate message or language file.

## Message settings

```yaml
messages:
  command-prefix: "&8[&7JustAFK&8]&r "
  became-afk: "&8[&7JustAFK&8]&r &7{player} is now AFK."
  became-active: "&8[&7JustAFK&8]&r &7{player} is no longer AFK."
```

| YAML path | Used for | Placeholders |
| --- | --- | --- |
| `messages.command-prefix` | Prefix for plugin-generated command confirmations, errors, and headers that use the prefix, plus the direct notice sent when someone changes another player's state. | None |
| `messages.became-afk` | Announcement after an actual transition from active to AFK. | `{player}` |
| `messages.became-active` | Announcement after an actual transition from AFK to active. | `{player}` |

`messages.command-prefix` is not automatically prepended to the two announcement strings. Their defaults include a JustAFK prefix directly; include any prefix you want when replacing those messages.

The command prefix may be empty. The two announcement strings may also be empty, although an enabled audience will then receive an empty message.

Most command text is hard-coded in English. Only the prefix and the two state-change announcements are configurable.

The prefix is not added to every command output line. The individual lines listed after the `/justafk help` and `/justafk status` headers are sent without it, and permission-denial text produced by the server is outside this setting.

## Announcement delivery

The `announcements.audience` setting controls both configured state-change messages:

- `all` sends them to all online players;
- `ops` sends them to online operator players; and
- `none` suppresses them.

The server console is not an announcement recipient. Reapplying a player's current state does not create another announcement. Disabling JustAFK globally clears state without sending the normal active announcement.

Use `announcements.audience: none` to disable broadcasts.

## Placeholders

| Placeholder | Available in | Value |
| --- | --- | --- |
| `{player}` | `messages.became-afk`, `messages.became-active` | The player's current name. |
| `{duration}` | `player-list.format` | Whole AFK minutes below one hour, then whole AFK hours. |

Placeholders are literal and case-sensitive. Every occurrence is replaced. `{duration}` is mandatory in `player-list.format`; `{player}` is optional in announcement messages.

These are JustAFK's own replacements. The plugin has no PlaceholderAPI integration and does not resolve arbitrary placeholders.

## Colours and formatting

All three message strings and `player-list.format` pass through Bukkit's legacy alternate-colour-code translator using `&`. For example:

```yaml
announcements:
  audience: all

player-list:
  enabled: true
  format: "&8[&bAway {duration}&8]&r "

messages:
  command-prefix: "&8[&bJustAFK&8]&r "
  became-afk: "&b{player}&7 is now away."
  became-active: "&b{player}&7 is back."
```

MiniMessage tags are not parsed.

## Reload behavior

Apply manual message or format edits with `/justafk reload`. A new format may not repaint an already-AFK player's unchanged duration label immediately; it is applied when the label next changes or when that player leaves and re-enters AFK. Newly AFK players use the new format immediately.
