# TODOs

## Reminders

- Test each release in 1.21.* and 26.1

## Alpha Lotus Atoll

https://hypixel.net/threads/may-6th-lotus-atoll-alpha-testing.6094444/
https://hypixel.net/threads/may-14th-lotus-atoll-alpha-testing-2.6097759/

### Atoll

- Jumpin' Jack - retest highlight
- [NPC] Puddle Jumper: Let's get ready for a ride!
- [NPC] Puddle Jumper: Wow! You caught me!

### Sea creatures

- Do some of them have immunity?
- Line to Puddle Jumper?
- Need to migrate old sea creature names without removal from trackers.

### Drops

- Need to check RARE DROP! presence to understand if some alerts are broken.
- Add new items from trophy frogging
- Adjusted all Bayou Sea Creature stats and drop chances - ????
- Adjusted the drop tables, bestiaries, and stats of all Hotspot and Event Sea Creatures

## Other

You donated 9 Trophy Frogs and received Silver Lotus x3 and Lotus x304!
You donated 1 Trophy Frog and received Lotus x8!

SCHLURP! The Hotspot Tonic Mixin grants you effects for 86h 24m! They will pause if your God Potion expires.
SCHLURP! The effects of the Celestial Mason Jar have been extended by 86h 24m! They will pause if your God Potion expires.
SCHLURP! The Blended Fish Mixin grants you effects for 86h 24m! They will pause if your God Potion expires.

WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll! - 4 perks
WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!
WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +5♔ Trophy Chance for 30m while on the Lotus Atoll!

## 0.24.5

Newly released - https://hypixel.net/threads/hypixel-skyblock-0-24-5-assorted-qol-changes.6094244/

- Added the Personal Sea Creature Cap - 10 mobs (probably except Isle, there might be 5)
  - I need to write better "own" sea creature detection to alert before personal cap actually happens.
- Added the functionality of your current bait being slown in slot #9 while fishing
  - Show current baits amount as icon or as movable text

## Current issues & feedback

- Add treasure dye pity rng meter (millerzz)
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
