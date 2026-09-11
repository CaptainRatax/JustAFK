# Building from source

JustAFK uses the included Gradle wrapper. No system Gradle installation, Maven build, npm project, or documentation build step is involved.

## Requirements

- JDK 25 available to Gradle
- Network access for the wrapper and dependencies when they are not already cached

The project uses Gradle 9.1 and a Java 25 toolchain. The resulting plugin classes target Java 21.

## Build the plugin

From the repository root on Linux or macOS:

```bash
sh ./gradlew build
```

From PowerShell on Windows:

```powershell
.\gradlew.bat build
```

The current artifact is written to:

```text
build/libs/JustAFK-1.2.0.jar
```

The version comes from `build.gradle.kts` and is expanded into `plugin.yml` during resource processing, so a later project version produces the corresponding filename.

## What `build` verifies

In addition to compilation and unit tests, the configured `check` lifecycle:

- compiles the main source against the minimum Spigot 1.21.3 API;
- compiles against the primary Paper 26.2 build 84 stable API;
- requires both compilation outputs to contain identical class bytecode;
- verifies the JAR's plugin version and `api-version: 1.21.3` metadata;
- verifies Java 21 class-file bytecode; and
- verifies that no runtime dependencies are declared.

Run only the unit tests with:

```bash
sh ./gradlew test
```

## Source layout

| Path | Contents |
| --- | --- |
| `src/main/java` | Plugin implementation and public Java API |
| `src/main/resources/plugin.yml` | Bukkit plugin metadata, commands, and permissions |
| `src/main/resources/config.yml` | Default user configuration |
| `src/test/java` | JUnit tests for state, activity, API events, configuration, and duration formatting |

JustAFK is licensed under the [MIT License](https://github.com/CaptainRatax/JustAFK/blob/main/LICENSE).
