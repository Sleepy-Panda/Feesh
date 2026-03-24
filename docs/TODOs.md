## Players feedback

- TODO: Test in 1.21
- Ragnarok timer for immunity - when it goes below 50% hp (e.g. 62.4). Max HP is 125 mil, or 250 on Derpy. Around 12 seconds immunity?
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

- Users to enable new alerts

- Double Hook possible for Reindrakes.
- Mythic Baby Yeti pet - add to calculation.
- New Lobby Wide catch message for Reindrake: WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
- Baby Yeti now drops as COMMON - adjust alerts, chat message, custom sounds, and fishing profit items. Add RARE DROP! It has no drop msg
- Removed alerts & chat message & custom sound on Legenadary Baby Yeti as it does not drop anymore.
- Renamed Hilt of true Ice to True Ice.
- Added 50,000 Coin NPC Sell Prices to several Special Fish (Mob, Swamp, Fish)
- Winter mob rarities changed.
- New loot table for winter SCS (actually nothing new)
- Enable hotspots logic for Jerry Workshop.
- Water Hotspot overlay should work.
- Hotspot found / Share / Gone should work.
- Nessie sea creature. Alert, chat, custom sound, highlight, HP tracker.
- See Reindrake nametag even if more than 30 blocks away.
- New consumable for Jerry, Blizzard in a Bottle. Add alert when Blizzard expires. Overlay with Rain/Thunder/Blizzard.
- Enchanted Book - old format, or Combinable in Anvil, empty line, name
  - In trash highlighter and in profit tracker
- /warp murk for nessie?

- Killed by Nessie.  ☠ You were killed by Nessie.
- Split water hotspots as separate widget, not joined with Bayou.
- Can we catch hotspot SCS on Jerry? - Yes. Can we catch Spooky? Carrot King, Agarimoo? - no
- Mound of Seagrass (epic) and Vibrant Coral (legendary) drop - need alerts, chat etc?
- Blizzard for tunnels?
- Loch hook (Shiffer Shard) attribute shard? Add into fishing drops
  - [537] ৫ [MVP+] haidm: You caught x2 Sniffer Shards! - going into huntingbox

- Nessie immunity?
- NPC price for Mound of Seagrassand Vibrant Coral drop
- - 2% mound, 0.5% coral
- Be able to delete old baby yetis from tracker and /feeshDelete... - [Feesh] Item not found by ID: BABY_YETI;4

- IDs to check post-update: Hilt of Truce Ice, Mythic Baby Yeti, Sniffer Shard

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Keybinds periodically reset, probably after MC crashes or turning off PC
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Will NEU prices API be available? Do I need to hop to another one?
- Enabled highlighters / slot renderers to update dynamically instead of check every render event.

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
