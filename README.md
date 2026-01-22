# Feesh Mod

`Feesh` is a Fabric 1.21.10 mod for Hypixel Skyblock. It introduces many fishing-related QOL features. Do `/feesh`, set up the mod, and enjoy fishing! <3

This mod is an evolution of [FeeshNotifier ChatTriggers module](https://chattriggers.com/modules/v/FeeshNotifier) made for earlier MC versions.

**THIS MOD IS IN EARLY DEVELOPMENT STAGE!** It's not assumed to be widely used yet, due to incomplete / not well tested features.
Once it's ready for beta, I plan to publish it on Modrinth.

## Links

[Modrinth](https://modrinth.com/project/feesh) - nothing to see there yet

## Dependencies

This mod requires Fabric API and Fabric Language Kotlin mods for respective Minecraft version.

## Build

Github automatically builds every version pushed to develop branch.

For manual building the solution:

```
./gradlew --refresh-dependencies --stacktrace
```

```
./gradlew build
```

As a result, `Feesh-<version>-fabric.jar` file appears in `/build/versions` folder.

## Debug

https://docs.fabricmc.net/develop/getting-started/vscode/launching-the-game#generating-launch-targets

Gradlew tasks - IDE - Vscode - Run

Run & Tests - Minecraft Client
