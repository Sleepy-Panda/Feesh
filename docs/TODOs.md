## Tech Debt

- Check if buttons are clickable at different scales (check places before of after the button - x), same for moving samples.
- Go through TODOs in the code
- Rework ticks counters across all overlays

## Versions

- Publish on Modrinth
- Add icon and mod descriptions
- Add support of 1.21.11
- Make a version checker which announces new versions found on Modrinth
- Will NEU prices API be available? Do I need to hop to another one?

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Data

Make sure old file from CT module can be moved to the mod with minor data loss.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.
- Rare Drop Notifications counter for pchat

## Sounds

- Custom sounds setup by user.

## Overlays

- Hide timer/profit per hour for Total using setting
- Make lines clickable for Reset/Pause/etc
- Remove command hint from overlays, add buttons to sample overlays in /feeshMoveAllGuis
- Refactor logic which counts ticks in each overlay
- Check that it works fine after loading the module without coords file, without data file
- Use Center alignment as default for some widgets
- Check if "0" key works for numpad in /feeshMoveAllGuis
