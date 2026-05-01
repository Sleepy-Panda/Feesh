## Players feedback

- Alpha:
  - Added the Personal Sea Creature Cap from the Crimson Isle to every island in the game
  - Added the functionality of your current bait being slown in slot #9 while fishing
  - Changing crafting recipies for THunder/ML gear
  - Added a message for when The Primordial / Bloodshot Reforge activates
    - CAUGHT! You cocooned a Voracious Spider!
    - CAUGHT! You cocooned a Frozen Steve!
- Catches/h for treasures
- Link to changelog? Change hotspot sounds?
- Some people need other sea creatures alerts, e.g. Ent or Night Squid
- TODO: Test in 1.21

- Deployable in mineshaft
- 1.21 Fishing Hook armorstand
- Sometimes current world&zone is detected wrongly
- Ragnarok immunity timer
- Someone has no Vial title/sound (I checked and did not reproduce, I had my title/sound as usual)
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Carmine dye into tracker
- Option to select some API instead of NEU prices API, if it might be decomissioned in a while
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
