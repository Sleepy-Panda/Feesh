## Players feedback

- TODO: Test in 1.21
- Someone has no Vial title/sound
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Items counted after /gfs: Moved 39 Enchanted Sea Lumies from your Sacks to your inventory.
- Rendering section from CT
- Settings are not saved after exiting the game, probably because user closes window using X button to exit
- On 1.21.11, original fishing timer armorstand appears for a second when the fish starts swimming towards bobber

## Jerry update

https://hypixel.net/threads/march-23rd-jerry-island-qol-new-abiphone-contacts-reforge-anvil-removal-and-more.6075332/

- Double Hook possible for Reindrakes.
- Mythic Baby Yeti pet - add to calculation. Check that API will have it.
- New Lobby Wide catch message for Reindrake: WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
- Personal Reindrake alert works fine.
- Baby Yeti now drops as COMMON - adjust alerts, chat message, custom sounds, and fishing profit items. Add RARE DROP! It has no drop msg
- Renamed Hilt of true Ice to True Ice
- Added 50,000 Coin NPC Sell Prices to several Special Fish (Swamp, Fish)
- Mob rarities changed
- Check pet highlighter at Kat. Baby Yeti is invalid now.
- New loot table for winter SCS (actually nothing new)
- Enable hotspots logic for Jerry Workshop.
- New grinch nametag - count grinch in barn fishing timer
- Water Hotspot widget should work.
- Nessie sea creature. Alert, chat, custom sound, highlight, HP tracker.

- Can we catch hotspot SCS on Jerry? Can we catch Spooky? Carrot King, Agarimoo?
- New consumable for Jerry, Blizzard in a Bottle. Add alert when it expires. Overlay?
  - This item can’t be used while a Blizzard is already active!
  - The Blizzard petered out... - global lobby buff
- Mound of Seagrass (epic) and Vibrant Coral (legendary) drop - need alerts, chat etc?
- Loch hook (Shiffer Shard) attribute shard?
  - [537] ৫ [MVP+] haidm: You caught x2 Sniffer Shards! - going into huntingbox
- /warp murk for nessie?
- NPC price for baby yeti
- Nessie immunity?
- NPC price for Mound of Seagrassand Vibrant Coral drop

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Keybinds periodically reset, probably after MC crashes or turning off PC
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Will NEU prices API be available? Do I need to hop to another one?

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
