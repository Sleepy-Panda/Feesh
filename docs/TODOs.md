## Versions

- Add support of 1.21.11?
- Publish on Modrinth
- Make a version checker which announces new versions found on Modrinth

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Data

Make sure old file from CT module can be moved to the mod with minor data loss.

## Fishing Hook timer

Should cancel rendering of the original armor stand.

## Player death alert

Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Sounds

- Custom sounds setup by user.

## Overlays

- Make lines clickable for Reset/Pause/etc
- Remove command hint from overlays, add buttons to sample overlays in /feeshMoveAllGuis
- Refactor logic which counts ticks in each overlay
- Check that it works fine after loading the module without coords file, without data file
- Use Center alignment as default for some widgets
- Make overlays rendered under TabList and Chat
- Check if "0" key works for numpad in /feeshMoveAllGuis

## Sea creatures

- Do not count Vanquisher into overlay if not fishing
- Vanquisher's message is compacted