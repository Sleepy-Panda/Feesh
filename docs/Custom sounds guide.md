# Custom Sounds

This guide allows you to set custom sounds when you catch a rare sea creature, or drop a rare item. List of sea creatures and rare drops matches the alerts available in the module settings.

## Table of Contents

- [Quick Start](#quick-start)
- [Reset to default](#reset-to-default)
- [Detailed Guide](#detailed-guide)
    - [Custom Resource Pack](#custom-resource-pack)
    - [Sound Configuration Files](#sound-configuration-files)

## Quick Start

1. Make sure `Meme` sound mode is selected in the module settings.
1. Prepare your custom sounds converted to the `.ogg` format.
1. For custom sounds:
   - Prepare ResourcePack with your .ogg sounds and install it in Minecraft (see [Custom Resource Pack](#custom-resource-pack)).
   - If you wish, test the custom sounds ingame using `/feeshPlayTestSound <filename.ogg>`.
1. Update `<MC>/config/feesh/userCatchSounds.json` or `<MC>/config/feesh/userDropSounds.json` to use your custom sounds or embedded mod sounds (see [Sound Configuration Files](#sound-configuration-files)).

## Reset to default

- Navigate to the mod's config folder - `<MC>/config/feesh/`.
- Remove `userCatchSounds.json` or `userDropSounds.json` files.
- Restart your game, it will regenerate the files with default sounds.

## Detailed Guide

### Custom Resource Pack

The modern Minecraft requires you to package custom sounds as a resource pack, to make them available ingame.

1. The mod automatically creates a ResourcePack folder structure at:
```
<MC>/config/feesh/feesh-custom-sounds/
```

1. Make sure `Meme` sound mode is selected in the Feesh module settings.

1. Exit the game.

1. Copy your `.ogg` files into the `feesh-custom-sounds/assets/feesh/sounds/` folder. **IMPORTANT: Use only lowercase letters (a-z), numbers (0-9), `-` and `_` in file names** (`my-custom-sound_123.ogg` instead of `My custom sound.ogg`).

1. Run the game. As the result, the `sounds.json` file describing the Resource Pack sounds will be automatically generated.

1. Package all files/folders inside of `feesh-custom-sounds` folder to `feesh-custom-sounds.zip` (note: there should be no subfolder named `feesh-custom-sounds` inside of zip, zip should contain resource pack files directly).

1. Install the Resource Pack in Minecraft:
  - Open Minecraft Settings (Options)
  - Go to Resource Packs
  - Click "Open Pack Folder"
  - Copy your Resource Pack zip file to this folder
  - Return to the game and move the ResourcePack to the Selected resource packs list

1. Now you may want to test the custom sounds ingame using `/feeshPlayTestSound <my-custom-sound.ogg>`.

### Sound Configuration Files

The sound configuration files are located at:

```
<MC>/config/feesh/userCatchSounds.json
```

and

```
<MC>/config/feesh/userDropSounds.json
```

Those files are automatically created on first mod launch and contain default embedded mod sounds. **Exit the game before modifying them!**

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

- For custom sounds: use your file name from the Resource Pack (e.g., `"my-custom-emperor-sound.ogg"`)
- For embedded mod sounds: keep original sound file name existing in the mod by default (e.g., `"feesh_notification.ogg"`)

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
    "BABY_YETI;4": {
        "source": "feesh_sheesh.ogg"
    },
    "DYE_MIDNIGHT": {
        "source": "feesh_giga-chad.ogg"
    },
}
```

Each key is the ID of a rare drop in uppercase.
For pet IDs, `;4` suffix means Legendary, `;3` - Epic, `;2` - Rare, `;2` - Uncommon, `;1` - Common. E.g. `BABY_YETI;4` is a Legendary Baby Yeti.

The `source` field contains the sound file name:

- For custom sounds: use your file name from the Resource Pack (e.g., `"my-custom-dye-sound.ogg"`)
- For embedded mod sounds: keep original sound file name existing in the mod by default (e.g., `"feesh_notification.ogg"`)
