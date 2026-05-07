# TODOs

## Reminders

- Test each release in 1.21.* and 26.1

## Alpha Lotus Atoll

https://hypixel.net/threads/may-6th-lotus-atoll-alpha-testing.6094444/

### Atoll

Register new fishing world - Lotus Atoll
- Has hotspots? YES
- Has rain, 19m left
- Prevent picking up drops like from Odger
- New sea creatures and drops
  - An inquisitive Atoll Croaker takes the bait! COMMON
  - Lotus
  - A Lotus Guardian emerges, ready to protect the Atoll. UNCOMMON
  - Lotus, Frogcoin (Uncommon, no npc)
  - A Drowned Captain takes hold of your bobber! EPIC
  - Ench Gold Ingot, Silver Lotus (Rare, 1200), Ench Gold Block, Unlucky Doubloon (EPIC 1% 100k coins)
  - RARE DROP! Unlucky Doubloon (+153 ✯ Magic Find)
  - What even is that?! A... gorF? RARE
  - eroC enotS egrofeR (RARE, 1%, 25K)
  - RARE DROP! eroC enotS egrofeR (+192 ✯ Magic Find)
  - A Puddle Jumper is preparing for liftoff—cast your rod into it and hold on tight! LEG, No DH
  - Gold Lotus (Epic, 96K), Epic, 0.5%; PUDDLE JUMPER BRONZE
  - 
  - Add to MYTHIC announce

- Lotum (frog)
- Lotus Shard

### Bayou

- Added rain to the Backwater Bayou (52m left)

### Sea creatures

- Need to migrate old sea creature names without removal from trackers.
- Sea Guardian => Jumpin' Jack (COMMON Spooky Sea Creature)
- Night Squid => Inkling (UNCOMMON Water Hotspot Sea Creature)
  - You get an inkling that you've caught... an Inkling!
  - CAUGHT! You cocooned a Inkling!
- Manta Ray (EPIC Water Hotspot Sea Creature)
  - A majestic creature rises from the water. It's a Manta Ray.
  - Add to default alert/highlight/HP?
- Volcanic Snail (UNCOMMON Lava Hotspot Sea Creature)
  - You feel a burning sensation as you reel in a Volcanic Snail!
  - Seared Escargot (Rare, 25K)
- Magma Pillar (EPIC Lava Hotspot Sea Creature)
  - A Magma Pillar rises from the lava. EPIC, 1,25M HP
  - Magmarizer VI - 0.05%
- Scarecrow from COMMON -> UNCOMMON
- Grim Reaper from LEGENDARY -> MYTHIC
- Reduced Thunder rarity from MYTHIC -> LEGENDARY
  - Change color in Isle tracker
- SLudge is EPIC

### Drops

- Need to check RARE DROP! presence to understand if some alerts are broken.
- Need to migrate old item names, without removal from trackers.
- Add new items from trophy frogging
- Titanoboa - Snake Eyes (1 mil, Legendary)
- Magmarizer V - Magma Pillar Sea Creature drop
- Check Alligator Shard to be in drop pool (from salt)
- Luck of the Sea VI probably removed
- Renamed Phantom Hook to Spooky Hook
- Adjusted all Bayou Sea Creature stats and drop chances - ????
- Adjusted the drop tables, bestiaries, and stats of all Hotspot and Event Sea Creatures
- Removed RARE Fishing EXP Boost from Sea Leech
- Removed EPIC Fishing EXP Boost from Sea Leech, Deep Sea Protector, Water Hydra
- Pumpkin, Ench Pumpkin, Polished Pumpkin
- Ench Ink Sac, Ink Splat (RARE, 25K)

## Other

- Junker Joel has bigger shop
- Odgers gui named Trophy Fish
- Trophy Frogs

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
- Some people need other sea creatures alerts, e.g. Ent or Night Squid
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
