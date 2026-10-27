# Custom Sounds

This guide allows you to change custom sounds for the alerts when you catch a rare sea creature, or drop a rare item. Sounds are played for the sea creatures and drops which are enabled in the Alerts settings.

## Table of Contents

- [Quick Start](#quick-start)
- [Reset to default](#reset-to-default)
- [Detailed Guide](#detailed-guide)
  - [Sound Files](#sound-files)
  - [Alert Configuration Files](#sound-configuration-files)

## Quick Start

1. Make sure `Meme` sound mode is selected in the module settings.
1. Prepare your custom sounds converted to the `.ogg` format.
1. Copy them into `<MC>/config/feesh/sounds/` (the folder is created on MC launch).
1. Test files in-game using `/feeshPlayTestSound filename.ogg`.
1. Update `<MC>/config/feesh/userCatchSounds.json` or `<MC>/config/feesh/userDropSounds.json` to select which custom sound to use for specific alerts (see [Alert Configuration Files](#alert-configuration-files)). Changes are applied on file save; no game restart is needed.

## Reset to default

- Close the game.
- Navigate to the mod's config folder - `<MC>/config/feesh/`.
- Remove `userCatchSounds.json` and/or `userDropSounds.json` files.
- Restart your game, it will regenerate the files with default embedded sounds.

## Detailed Guide

### Sound Files

The folder `<MC>/config/feesh/sounds/` is created the first time the mod launches. Put your custom `.ogg` files there. You can add more files while game is open, no need to exit/restart.

Default built-in game sounds (`feesh_*.ogg`) are already inside the mod. They do not need to be copied into this folder.

### Alert Configuration Files

The alert configuration files are located at:

```
<MC>/config/feesh/userCatchSounds.json
```

and

```
<MC>/config/feesh/userDropSounds.json
```

Those files define which sound should be played for each rare catch / rare drop alert. They contain default embedded sound names, which can be replaced with the custom files you have in the folder.

Both json files are automatically created on game launch. Then you can edit them while the game is open, changes are applied on save.

#### userCatchSounds.json File Structure

The file has the following structure:

```json
{
    "YETI": {
        "source": "feesh_notification.ogg"
    },
    "REINDRAKE": {
        "source": "feesh_notification.ogg"
    },
    "THE LOCH EMPEROR": {
        "source": "custom-sound.ogg"
    },
}
```

Each key is the name of a sea creature in uppercase (e.g., "YETI", "REINDRAKE", "THE LOCH EMPEROR").

The `source` field contains the sound file name:

- For custom sounds: use the file name from `config/feesh/sounds` (e.g., `"my-custom-emperor-sound.ogg"`)
- For embedded mod sounds: keep the built-in file name (e.g., `"feesh_notification.ogg"`)

#### userDropSounds.json File Structure

The file has the following structure:

```json
{
    "PET_ITEM_LUCKY_CLOVER_DROP": {
        "source": "feesh_oh-my-god.ogg"
    },
    "RADIOACTIVE_VIAL": {
        "source": "feesh_minecraft-challenge-completed.ogg"
    },
    "DYE_MIDNIGHT": {
        "source": "feesh_giga-chad.ogg"
    },
}
```

Each key is the ID of a rare drop in uppercase.
For pet IDs, `;4` suffix means Legendary, `;3` - Epic, `;2` - Rare, `;2` - Uncommon, `;1` - Common. E.g. `FLYING_FISH;4` is a Legendary Flying Fish.

The `source` field contains the sound file name:

- For custom sounds: use the file name from `config/feesh/sounds` (e.g., `"my-custom-dye-sound.ogg"`)
- For embedded mod sounds: keep the built-in file name (e.g., `"feesh_notification.ogg"`)
