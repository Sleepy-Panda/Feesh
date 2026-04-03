## Players feedback

- TODO: Test in 1.21
- Magma Core fishing widget
- Limit SCs count in the view
- Someone has no Vial title/sound (I checked and did not reproduce, I had my title/sound as usual)
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Items counted after /gfs: Moved 39 Enchanted Sea Lumies from your Sacks to your inventory.
- Settings are not saved after exiting the game, probably because user closes window using X button to exit
- On 1.21.11, original fishing timer armorstand appears for a second when the fish starts swimming towards bobber

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
