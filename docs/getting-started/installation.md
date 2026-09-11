# Installation

?> JustAFK 1.2.0 supports the documented Paper, Purpur, Folia, Spigot, and CraftBukkit range from 1.21.3 through 26.2. The JAR requires Java 21 or newer, Paper 26.2 requires Java 25, and there are no plugin dependencies. Confirm the complete [requirements](/getting-started/requirements) before installing.

## 1. Obtain the plugin JAR

Download JustAFK from one of the distribution pages linked by the project:

- [Modrinth](https://modrinth.com/plugin/justafk)
- [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/justafk-plugin)

You can instead [build the JAR from source](/development/building). For the current release, the expected filename is `JustAFK-1.2.0.jar`.

## 2. Install the JAR

1. Stop the Minecraft server.
2. Place `JustAFK-1.2.0.jar` in the server's `plugins` directory.
3. Start the server.

No dependency plugins or database setup are required.

## 3. First start

On its first successful start, JustAFK:

- creates `plugins/JustAFK/`;
- copies the bundled defaults to `plugins/JustAFK/config.yml`;
- registers its commands and activity listeners;
- starts the AFK monitor; and
- logs that JustAFK was enabled with either the Bukkit-compatible or Folia scheduler.

The configuration file is copied only when it does not already exist. If the file is malformed or fails validation during initial startup, JustAFK logs `JustAFK could not be enabled.` with the underlying error and disables itself.

## 4. Verify and configure

As an operator, run:

```text
/justafk status
```

The command reports whether JustAFK and automatic detection are enabled, the timeout, the announcement audience, and the player-list setting.

Edit `plugins/JustAFK/config.yml` if needed, then apply a manual edit with:

```text
/justafk reload
```

The `/justafk` command requires `justafk.config`, which defaults to operators. Runtime `set` commands can update several settings without editing YAML. See the complete [configuration reference](/configuration/configuration) and [command reference](/usage/commands).
