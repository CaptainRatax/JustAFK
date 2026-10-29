# Requirements

This page describes the requirements declared for JustAFK 1.3.0 and the compatibility checks present in its build.

## Supported server platforms

| Server platform | API compatibility range | Notes |
| --- | --- | --- |
| Paper | 1.21.3–26.3 | Paper 26.3 build 230 stable is the primary compile target. |
| Purpur | 1.21.3–26.3 | Expected compatibility as a Paper fork, where builds exist. |
| Folia | 1.21.3–26.3 | Conditional on available server builds and compatible Folia schedulers. |
| Spigot / CraftBukkit | 1.21.3–26.3 | Bukkit API compatibility; the source is compatibility-compiled against Spigot 1.21.3. |

The same JAR retains the 1.21.3 baseline. The build requires identical class files from Paper 26.3 and Spigot 1.21.3 compilation and runs unit tests. These checks do not establish runtime compatibility on all intermediate versions or forks. No real-server tests were performed for this update, and the range does not imply that every platform publishes every listed Minecraft version. Purpur and Folia must preserve the APIs and scheduler contracts used by JustAFK.

Before deployment, test plugin startup, movement input and other activity, automatic and manual AFK changes, announcements, tab-list updates, configuration reload, and shutdown on the intended server build. On Folia, also check player scheduling across region changes and disconnects.

JustAFK declares `api-version: 1.21.3`. A server below this baseline should refuse to load the plugin. The baseline is required because AFK activity detection uses the movement-input API.

!> Install JustAFK on a Minecraft backend server. Sponge and proxy platforms such as BungeeCord, Velocity, and Waterfall are not supported.

## Java

The distributed plugin JAR uses Java 21 bytecode, so its minimum runtime is **Java 21**. The Minecraft server may impose a newer requirement; [Paper 26.1 and newer require **Java 25**](https://docs.papermc.io/paper/getting-started/).

Building from source has a different requirement: the Gradle project uses a **Java 25 toolchain** while emitting Java 21-compatible plugin classes. See [Building from source](/development/building).

## Dependencies

JustAFK has no required or optional runtime plugin dependencies. It does not require PlaceholderAPI, Vault, a database driver, or an external library. Folia support is contained in the same JAR.

## Before installing

Confirm all three of the following:

1. Your server platform and version appear in the table above.
2. The Java runtime satisfies both JustAFK and the server software.
3. You are installing on the backend server's `plugins` directory, not on a proxy.

Continue with [Installation](/getting-started/installation).
