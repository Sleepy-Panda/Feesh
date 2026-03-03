## Players feedback

- Sc/h tracker gets active - maybe vanquisher spawn triggers it?
- Rare/Uncommon squid alert
- Carmine dye into tracker
- RARE DROP! Troubled Bubble (+406 ✯ Magic Find)
- RARE DROP! Octopus Tendril (+190 ✯ Magic Find)
- Items counted after /gfs
- Rendering section from CT
- Settings are not saved after exiting the game, probably because user closes window using X button to exit
- On 1.21.11, original fishing timer armorstand appears for a second when the fish starts swimming towards bobber

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
