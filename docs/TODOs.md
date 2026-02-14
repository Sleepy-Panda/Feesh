## Players feedback

- Items counted after /gfs
- Troubled bubble?
- Hotspot is gone message when swapping server
- while i was fishing in the crystal hollows i noticed that the mob cap alert wasnt working for it
  - [I checked and for me it was working]
- Profit Tracker not paused after 5 minutes?
  - [I checked and for me it was paused]
- 1.21.11 support
- Rendering section from CT
- Settings are not saved after exiting the game, probably because user closes window using X button to exit

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays

## Versions

- Add support of 1.21.11
  - Adjust version checker which announces new versions found on Modrinth
- Will NEU prices API be available? Do I need to hop to another one?

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Data

Make sure old file from CT module can be moved to the mod with minor data loss.
Data backups.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
