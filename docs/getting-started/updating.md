# Updating

JustAFK does not contain an automatic updater or update checker. Replace the JAR manually when moving to a newer release.

## Update procedure

1. Stop the server completely.
2. Back up the current JustAFK JAR and `plugins/JustAFK/config.yml`.
3. Remove the old JustAFK JAR from `plugins` and place the new JAR there. Keep only one JustAFK JAR in the directory.
4. Start the server and check its log for the JustAFK enable message.
5. Run `/justafk status` and review the current [configuration reference](/configuration/configuration).

!> `/justafk reload` reloads only `config.yml`. It cannot load new plugin code, so replacing the JAR requires a server restart.

## Existing configuration files

An existing `config.yml` is not overwritten or merged with the bundled default during startup. Missing settings use the current implementation's fallback values in memory, but those keys are not automatically inserted into the file.

Configurations from earlier releases that do not contain `enabled` or `automatic-afk-enabled` remain usable; both missing switches default to `true`. Add missing keys yourself if you want the active configuration to be explicit and retain the current comments.

Configuration changes made with `/justafk set ...` are saved immediately. On server APIs that cannot preserve YAML comments, such a save may remove comments while retaining the values.

## Player state during an update

AFK state and inactivity timers exist only in memory. Stopping the server for an update clears them, and players begin with a fresh active state and inactivity window when tracking starts again. A normal `/justafk reload` retains current state and timers unless the loaded configuration disables JustAFK globally.
