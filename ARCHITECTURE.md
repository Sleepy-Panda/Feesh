# Feesh Architecture

## Overview

Feesh is a client-only Fabric mod for Minecraft. It is built with Gradle, using Kotlin for most code and Java for Mixin classes.

The project supports multiple Minecraft versions through a multi-module Gradle setup:

- `1.21.10-fabric`
- `1.21.11-fabric`
- `26.1-fabric`

Each version module uses the same root build script and shares the same source tree.

## Key concepts

- **Fabric mod**: `fabric.mod.json` declares the mod and client-side entrypoint.
- **Client initializer**: `FeeshMod.kt` implements `ClientModInitializer` and starts the mod.
- **Event bus**: custom event system publishes and handles game events.
- **Mixins**: Java classes under `src/main/java/.../mixin` patch Minecraft client behavior.
- **Settings/config**: uses `resourcefulconfig` / `resourcefulconfigkt` to register and load mod settings.
- **Assets**: sound files, locale strings, and mixin metadata are in `src/main/resources`.

## Build and runtime architecture

- Root build: `build.gradle.kts`
  - applies Kotlin, Loom, Bloom, Shadow, and multiversion plugins
  - configures Java/Kotlin toolchain and Fabric dependencies per version
- Root settings: `settings.gradle.kts`
  - includes version-specific subprojects
- Version selector: `root.gradle.kts`
  - defines multiversion preprocessing and Minecraft mappings

## Main runtime flow

1. Fabric loads client mod entrypoint `FeeshMod`.
2. `FeeshMod.onInitializeClient()` runs.
3. Core managers initialize:
   - `PersistentDataManager`
   - `CustomSoundsManager`
   - `EventBus`
   - settings and commands
4. Utility systems initialize for chat, GUI, world data, player data, and price lookup.
5. Event publishers attach to Minecraft client events.
6. Feature modules initialize in categories such as alerts, chat, overlays, items, rendering, and commands.

## Package layout

- `com.github.sleepypanda.feesh`
  - `FeeshMod.kt` - main entrypoint
- `com.github.sleepypanda.feesh.events`
  - event bus, publishers, event models
- `com.github.sleepypanda.feesh.features`
  - alerts, chat, commands, overlays, items, rendering, help
- `com.github.sleepypanda.feesh.settings`
  - settings models and registration
- `com.github.sleepypanda.feesh.utils`
  - game helpers, GUI helpers, player/world access, pricing, file and sound helpers
- `com.github.sleepypanda.feesh.constants`
  - static data such as sounds, sea creatures, rare drops, and fishing profits

## Important files

- `fabric.mod.json` - Fabric metadata, entrypoint, dependencies, mixins
- `feesh.mixins.json` - Mixin configuration file
- `FeeshMod.kt` - mod initialization and feature wiring
- `src/main/java/.../mixin/*.java` - mixin hooks into Minecraft client classes
- `src/main/resources/assets/feesh` - sounds, language, icons
- `build.gradle.kts` - shared build logic and version dependency configuration
- `settings.gradle.kts` - module inclusion for version targets
- `root.gradle.kts` - multiversion mapping declarations

## Notes

- Focus on `FeeshMod.kt` for initialization flow.
- Use `events/publishers` and `events/models` when adding game event support.
- Update `settings` models when adding new features, nearly every feature has its own setting toggle.
- Keep client-only constraints in mind: this mod does not run on Minecraft servers.
- Use existing FeeshGui overlay, command, and alert patterns for new feature development.

## Build

Requires JDK 25.

For manual building the project in IDE:

```cmd
./gradlew build
```

As a result, `Feesh-<version>-fabric.jar` file appears in `/build/versions` folder. It contains mod versions built for every supported Minecraft version.

## Mappings

- https://mappings.dev/1.21.10/
- https://mappings.dev/1.21.11/

## Updating Gradle

When bumping Gradle version, it's necessary to follow the flow to update Gradle wrapper files.

https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper

```cmd
.\gradlew.bat :wrapper --gradle-version 9.6.0
.\gradlew :wrapper
```
