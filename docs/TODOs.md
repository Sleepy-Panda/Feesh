## Players feedback

- Catches/h for treasures
- Nessie pchat toggle
- TODO: Test in 1.21
- TODO: Test party drops message
- dropping a tiki mask or a shed does not update this trackers, i have to update the drop count manually each time
- Deployable in mineshaft
- 1.21 Fishing Hook armorstand
- Sometimes current world&zone is detected wrongly
- Ragnarok immunity timer
- Someone has no Vial title/sound (I checked and did not reproduce, I had my title/sound as usual)
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Settings are not saved after exiting the game, probably because user closes window using X button to exit

## Tech Debt

- 26.1 support -> https://fabricmc.net/2026/03/14/261.html
  - Modloader 0.19.*
  - Java version 25
  - Check resourcepack generation
  - Check new version announcements
  - Check all mixins
  - Update README for dependencies
- Go through TODOs in the code
- Rework ticks counters across all overlays
- Keybinds periodically reset, probably after MC crashes or turning off PC
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Will NEU prices API be available? Do I need to hop to another one?
- Enabled highlighters / slot renderers to update dynamically instead of check every render event.

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
