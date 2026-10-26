# Custom Sounds

This guide allows you to set custom sounds when you catch a rare sea creature, or drop a rare item. List of sea creatures and rare drops matches the alerts available in the module settings.

## Table of Contents

- [Quick Start](#quick-start)
- [Reset to default](#reset-to-default)
- [Detailed Guide](#detailed-guide)
    - [Sound Files](#sound-files)
    - [Sound Configuration Files](#sound-configuration-files)

## Quick Start

1. Make sure `Meme` sound mode is selected in the module settings.
1. Prepare your custom sounds converted to the `.ogg` format.
1. Copy them into `<MC>/config/feesh/sounds/` (the folder is created on launch). No resource pack and no restart.
1. Test with `/feeshPlayTestSound <filename.ogg>`.
1. Point a catch or drop at that file in `userCatchSounds.json` or `userDropSounds.json` (see [Sound Configuration Files](#sound-configuration-files)). Saving the file is enough. The game re-reads it on the next alert.

## Reset to default

- Navigate to the mod's config folder - `<MC>/config/feesh/`.
- Remove `userCatchSounds.json` or `userDropSounds.json` files.
- Restart your game, it will regenerate the files with default sounds.

## Detailed Guide

### Sound Files

Put `.ogg` files here:

```
<MC>/config/feesh/sounds/
```

The folder is created the first time the mod launches. If you still have files in `<MC>/config/feesh/feesh-custom-sounds/assets/feesh/sounds/`, they are copied into the new folder on that first create. Files left in the old path still play as a fallback.

Use the file name, including `.ogg`, in the config and in `/feeshPlayTestSound`. The name is a single file name (`my sound.ogg` is fine). No resource pack, no `sounds.json`, and no restart.

Built-in meme sounds (`feesh_notification.ogg` and the other `feesh_*.ogg` names) stay inside the mod. They do not need to be copied into this folder. A file in the folder with the same name replaces the built-in one.

### Sound Configuration Files

The sound configuration files are located at:

```
<MC>/config/feesh/userCatchSounds.json
```

and

```
<MC>/config/feesh/userDropSounds.json
```

Those files are automatically created on first mod launch and contain default embedded mod sounds. Edit them while the game is open. The next catch or drop alert reads the saved file.

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
        "source": "custom-emperor-sound.ogg"
    },
}
```

Each key is the name of a rare sea creature in uppercase (e.g., "YETI", "REINDRAKE", "THE LOCH EMPEROR").

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
