# JustAFK

JustAFK is a lightweight AFK plugin for Bukkit-compatible Minecraft servers. It tracks recognized player input and actions, marks inactive players as AFK, provides manual and administrative controls, announces state changes, and can show AFK duration in the tab player list.

The current implementation includes:

- automatic AFK detection with a configurable timeout;
- manual self-service and administrator-controlled AFK states;
- configurable announcements for all players, operators, or nobody;
- an optional AFK-duration prefix that preserves the existing player-list name;
- Folia-aware scheduling; and
- a read-only Bukkit service API and state-change event.

?> These pages document JustAFK **1.2.0** for Paper, Purpur, Folia (where a server build exists), and Spigot/CraftBukkit **1.21.3 through 26.2**. The plugin JAR requires **Java 21 or newer**; the server itself may require a newer Java version.

## Start here

- [Check the platform and Java requirements](/getting-started/requirements).
- [Install JustAFK](/getting-started/installation).
- [Review every configuration setting](/configuration/configuration).
- [Learn how AFK detection works](/usage/afk-system).
- Use the [command](/usage/commands) and [permission](/usage/permissions) references.

Developers can use the intentionally small [public API](/development/api) or [build the project from source](/development/building).

Source code is available in the [CaptainRatax/JustAFK repository](https://github.com/CaptainRatax/JustAFK).
