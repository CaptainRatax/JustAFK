# Permissions

JustAFK declares four permission nodes.

| Permission | Default | Purpose |
| --- | --- | --- |
| `justafk.use` | Everyone | Use `/afk` for your own state; also serves as the base permission for targeted `/afk` forms. |
| `justafk.others` | Operators | Supply a player argument to `/afk` and change that online player's state. |
| `justafk.config` | Operators | Use `/justafk` or `/jafk`, including help, status, reload, and every `set` operation. |
| `justafk.admin` | Operators | Parent permission that grants `justafk.use`, `justafk.others`, and `justafk.config`. |

Defaults are Bukkit permission defaults. A permissions plugin can grant or deny the nodes independently.

## Command combinations

- `/afk` requires `justafk.use`.
- `/afk <player> [action]` requires both `justafk.use` and `justafk.others`.
- `/justafk` and its `/jafk` alias require `justafk.config` for every subcommand.

Because any player argument selects the targeted path, `/afk <your-own-name>` also requires `justafk.others`. Use plain `/afk` to toggle yourself with only `justafk.use`.

## What is not permission-controlled

There is no permission for:

- exemption from automatic AFK detection;
- receiving announcements; or
- individual `/justafk` subcommands.

The `ops` announcement audience checks actual Bukkit operator status with `Player.isOp()`. Granting `justafk.admin` to a non-operator does not make that player an `ops` announcement recipient. Operators are not exempt from AFK detection.
