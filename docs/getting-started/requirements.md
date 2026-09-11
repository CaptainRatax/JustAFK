# Requirements

This page describes the supported requirements declared for JustAFK 1.2.0 and the compatibility checks present in its build.

## Supported server platforms

| Server platform | Supported range | Notes |
| --- | --- | --- |
| Paper | 1.21.3–26.2 | Paper 26.2 is the primary compile target. |
| Purpur | 1.21.3–26.2 | Supported as a Paper fork. |
| Folia | 1.21.3–26.2 | Supported where Folia publishes a server build. JustAFK detects Folia and uses its schedulers. |
| Spigot / CraftBukkit | 1.21.3–26.2 | The source is compatibility-compiled against the Spigot 1.21.3 API. |

JustAFK declares `api-version: 1.21.3`. A server below this baseline should refuse to load the plugin. The baseline is required because AFK activity detection uses the movement-input API.

!> Install JustAFK on a Minecraft backend server. Sponge and proxy platforms such as BungeeCord, Velocity, and Waterfall are not supported.

## Java

The distributed plugin JAR uses Java 21 bytecode, so its minimum runtime is **Java 21**. The Minecraft server may impose a newer requirement; in particular, the repository specifies **Java 25 for Paper 26.2**.

Building from source has a different requirement: the Gradle project uses a **Java 25 toolchain** while emitting Java 21-compatible plugin classes. See [Building from source](/development/building).

## Dependencies

JustAFK has no required or optional runtime plugin dependencies. It does not require PlaceholderAPI, Vault, a database driver, or an external library. Folia support is contained in the same JAR.

## Before installing

Confirm all three of the following:

1. Your server platform and version appear in the table above.
2. The Java runtime satisfies both JustAFK and the server software.
3. You are installing on the backend server's `plugins` directory, not on a proxy.

Continue with [Installation](/getting-started/installation).
