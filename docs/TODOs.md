## Players feedback

- Legion & Bobbing Time - want to choose in settings which one to show, or labels customization to make shorter display
- 1.21.11 support

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays

## Versions

- Publish on Modrinth
- Add support of 1.21.11
- Make a version checker which announces new versions found on Modrinth
- Will NEU prices API be available? Do I need to hop to another one?

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Data

Make sure old file from CT module can be moved to the mod with minor data loss.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
