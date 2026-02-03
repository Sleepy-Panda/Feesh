# Feesh Mod

`Feesh` is a Fabric 1.21.10 mod for Hypixel Skyblock. It introduces many fishing-related QOL features. Do `/feesh`, set up the mod, and enjoy fishing! <3

This mod is an evolution of [FeeshNotifier ChatTriggers module](https://chattriggers.com/modules/v/FeeshNotifier) made for earlier MC versions.

**THIS MOD IS IN EARLY DEVELOPMENT STAGE!** It's not assumed to be widely used yet, due to incomplete / not well tested features.
Once it's ready for beta, I plan to publish it on Modrinth.

## Table of Contents

- [Releases](#releases)
- [Dependencies](#dependencies)
- [Features](#features)
- [Contacts](#contacts)
- [Developer's stuff](#developers-stuff)

## Releases

[Modrinth](https://modrinth.com/project/feesh) - nothing to see there yet

### Pre-releases

If you want access to pre-releases, Github automatically builds every version pushed to develop branch. They can be found in Actions - <select latest action> - Artifacts - find attached `.zip` which contains `.jar` file(s).

## Dependencies

This mod requires (Fabric API)[https://modrinth.com/mod/fabric-api] and (Fabric Language Kotlin)[https://modrinth.com/mod/fabric-language-kotlin] mods for 1.21.10 Minecraft version.

## Features

### General

- **Sound modes** — Meme, Normal (default MC sounds), or Off.

### Alerts

- **Rare sea creatures** — Shows a title and plays a sound when a rare sea creature is caught by you or your party members.
- **Rare drops** — Shows a title and plays a sound when a rare item drops. Supports price display and customizable drop types.
- **Any Reindrake** — Alerts when any Reindrake spawns in the lobby, even if caught by others. Offers option to warp to island spawn on click.
- **Spirit Mask** — Alerts when Spirit Mask's Second Wind is activated and when it's back.
- **Barn fishing timer** — Alerts when sea creatures are alive for 5+ minutes or when their count hits threshold (configurable per location).
- **Deployables** — Alerts when a deployable item expires in 10 seconds.
- **Pet level up** — Alerts when a pet reaches max level. Can show estimated leveling price in chat.
- **Hotspot found or gone** — Alerts when a hotspot is found or is gone.
- **Fishing Bag disabled** — Alerts when you start fishing with Fishing Bag disabled.
- **No fishing armor** — Alerts when fishing without fishing armor equipped.
- **Lootshare** — Alerts when "Lootshare!" message appears in party chat.
- **Chum bucket auto-pickup** — Alerts when Chum/Chumcap bucket is automatically picked up.
- **Golden Fish** — Alerts when a Golden Fish has spawned.
- **Thunder/Storm/Hurricane Bottle charged** — Alerts when bottle is fully charged.
- **Salt expired** — Alerts when a Salt has expired.
- **Worm the Fish** — Alerts when Worm the Fish is detected (Dirt Rod fishing).

### Overlays

You can change position, scale and alignment (Left, Center, Right) for each overlay in **/feeshMoveAllGuis**!

- **Legion & Bobbin' Time tracker** — Shows players and fishing hooks count within 30 blocks.
- **Barn fishing timer** — Shows count of sea creatures nearby and how long they've been alive.
- **Deployables timer** — Shows remaining time of deployable items placed nearby.
- **Sea creatures HP** — Shows HP of nearby rare sea creatures in lootshare range with immunity indicator.
- **Sea creatures tracker** — Overview of caught sea creatures with Session/Total modes, percentages, and double hook stats.
- **Fishing hook timer** — Displays hook timer and fish arrival indicator. Requires Skyblock Fishing Timer enabled.
- **Sea creatures per hour** — Shows sea creatures per hour and total caught per session.
- **Jerry's Workshop tracker** — Yeti/Reindrake catch statistics in Jerry Workshop.
- **Water hotspots & Bayou tracker** — Titanoboa, Wiki Tiki, and drop statistics for Backwater Bayou and Water Hotspots.
- **Crimson Isle tracker** — Fiery Scuttler, Ragnarok, Plhlegblast, Thunder, Lord Jawbus catch and Radioactive Vial drop stats.
- **Treasure fishing tracker** — Good/Great/Outstanding treasure catches and Treasure Dye drop statistics.
- **Archfiend Dice profit tracker** — Archfiend Dice / High Class Archfiend Dice profit overlay with Session/Total modes.

### Chat

- **Compact sea creature messages** — Shortens double hook and catch messages in your chat.
- **Share rare sea creatures** — Sends PARTY chat when you catch a rare sea creature.
- **Share rare sea creatures location - ALL chat** — Sends ALL chat with coordinates when you catch selected rare creatures.
- **Player death message** — Sends party chat when killed by Mythic sea creature (Thunder, Lord Jawbus, etc.).
- **Share rare drops** — Sends PARTY chat when rare item drops.
- **Hotspot sharing** — Clickable message to share found hotspot location to PARTY or ALL chat. Optional autoshare.

### Commands

- **/feesh** — Opens mod settings.
- **/feeshMoveAllGuis** — Move and resize all enabled overlay GUIs.
- **/feeshPauseAllTrackers** — Pause all active trackers which have a timer (also available as Keybind).
- **/feeshPetLevelUpPrices** — Calculates profits for leveling fishing pets from 1 to 100.
- **/feeshGearCraftPrices** — Calculates profits for crafting gear from fishing drops.
- **/feeshSpiderDenRainSchedule** — Displays nearest Spider's Den Rain/Thunderstorm events.
- **/feeshSetTrackerDrops** — Initialize drop history (Titanoboa Shed, Tiki Mask, Radioactive Vial, Treasure Dye).

Also, each overlay has individual commands to reset or pause.

## Contacts

In case of questions, bug reports, feature requests, bored, feeling alone - please feel free to contact me:

- IGN: MoonTheSadFisher
- Discord: m00nlight_sky
- Discussions or Issues in the repository (Feesh)[https://github.com/Sleepy-Panda/Feesh/discussions]

## Developer's stuff

### Build

For manual building the project in IDE:

```
./gradlew --refresh-dependencies --stacktrace
```

```
./gradlew build
```

As a result, `Feesh-<version>-fabric.jar` file appears in `/build/versions` folder.

### Debug

https://docs.fabricmc.net/develop/getting-started/vscode/launching-the-game#generating-launch-targets

Gradlew tasks - IDE - Vscode - Run

Run & Tests - Minecraft Client
