# TODOs

## Reminders

- Test each release in 1.21.* and 26.1

## Alpha Lotus Atoll

https://hypixel.net/threads/may-6th-lotus-atoll-alpha-testing.6094444/
https://hypixel.net/threads/may-14th-lotus-atoll-alpha-testing-2.6097759/

WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll! - 4 perks
WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!
WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +5♔ Trophy Chance for 30m while on the Lotus Atoll!

## 0.24.5

Newly released - https://hypixel.net/threads/hypixel-skyblock-0-24-5-assorted-qol-changes.6094244/

- Added the Personal Sea Creature Cap - 10 mobs (probably except Isle, there might be 5)
  - I need to write better "own" sea creature detection to alert before personal cap actually happens.

## Current issues & feedback

- 1.21.10 gonna be removed
- Overlays alignment for DH, BS, profit coins
- ea Creature Tracker where it says "Total" could you change that to "Total Sea Creatures"
- Empty bait name in no bait alert. Got treasure bait from quest
- Add treasure dye pity rng meter (millerzz)
- Some legion counting radius logic reported
- Catches/h for treasures
- Improve link to changelog in settings, and update announcement
- Work on various events sounds to make them more unique
- Deployable in mineshaft
- 1.21 Fishing Hook armorstand
- Sometimes current world&zone is detected wrongly
- Ragnarok immunity timer
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Settings are not saved after exiting the game, probably because user closes window using X button to exit

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Enabled highlighters / slot renderers to update dynamically instead of check every render event.

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.
