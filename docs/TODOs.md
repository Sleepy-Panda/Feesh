# TODOs

## Reminders

- Test each release in 1.21.* and 26.1

## Alpha Lotus Atoll

https://hypixel.net/threads/may-6th-lotus-atoll-alpha-testing.6094444/

### Atoll

- Volcanic Snail highlight broken
- [NPC] Puddle Jumper: Let's get ready for a ride!
- [NPC] Puddle Jumper: Wow! You caught me!

### Sea creatures

- Need to migrate old sea creature names without removal from trackers.
- Sea Guardian => Jumpin' Jack (COMMON Spooky Sea Creature)
  - ???
- Night Squid => Inkling (UNCOMMON Water Hotspot Sea Creature)
  - You caught x2 Inkling Shards!
  - Make Shards trackable with both names.

### Drops

- Need to check RARE DROP! presence to understand if some alerts are broken.
- Add new items from trophy frogging
- Adjusted all Bayou Sea Creature stats and drop chances - ????
- Adjusted the drop tables, bestiaries, and stats of all Hotspot and Event Sea Creatures
- Check item IDs for Spooky Hook and Inkling Shard after release

## Other

- Junker Joel has bigger shop

WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!

You donated 9 Trophy Frogs and received Silver Lotus x3 and Lotus x304!
You donated 1 Trophy Frog and received Lotus x8!

RIBBIT! [MVP+] _etaF caught their first DIAMOND Common Frog!
♔ TROPHY FROG! You caught a Common Frog BRONZE!
♔ TROPHY FROG! You caught a Puddle Jumper SILVER!

SCHLURP! The Hotspot Tonic Mixin grants you effects for 86h 24m! They will pause if your God Potion expires.
SCHLURP! The effects of the Celestial Mason Jar have been extended by 86h 24m! They will pause if your God Potion expires.
SCHLURP! The Blended Fish Mixin grants you effects for 86h 24m! They will pause if your God Potion expires.

## 0.24.5

Newly released - https://hypixel.net/threads/hypixel-skyblock-0-24-5-assorted-qol-changes.6094244/

- Added the Personal Sea Creature Cap - 10 mobs (probably except Isle, there might be 5)
  - I need to write better "own" sea creature detection to alert before personal cap actually happens.
- Added the functionality of your current bait being slown in slot #9 while fishing
- Changing crafting recipe/name for ML Necklace
- Added a message for when The Primordial / Bloodshot Reforge activates
  - CAUGHT! You cocooned a Voracious Spider!
  - CAUGHT! You cocooned a Frozen Steve!

## Current issues & feedback

- Catches/h for treasures
- Would also be cool to have a double hook statistic for the total sc counter so you can view ur avg. dhc and how good/bad it is compared to ur actual dhc
- Improve link to changelog in settings, and update announcement
- Work on hotspot sounds to make them more unique
- Deployable in mineshaft
- 1.21 Fishing Hook armorstand
- Sometimes current world&zone is detected wrongly
- Ragnarok immunity timer
- Someone has no Vial title/sound (I checked and did not reproduce, I had my title/sound as usual)
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Settings are not saved after exiting the game, probably because user closes window using X button to exit

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Keybinds periodically reset, probably after MC crashes or turning off PC
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Enabled highlighters / slot renderers to update dynamically instead of check every render event.

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
