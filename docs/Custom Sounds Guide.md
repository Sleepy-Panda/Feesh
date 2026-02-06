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

1. The mod automatically creates a ResourcePack folder structure on game load at:
```
<MC>/config/feesh/feesh-custom-sounds/
```

1. Copy your `.ogg` files into the `feesh-custom-sounds/assets/feeshcustom/sounds/` folder. 

1. Run the `/feeshGenerateSoundsResourcePack` command from the game.
As the result, the `sounds.json` file will be automatically generated in the folder above.

1. Package all files inside of `feesh-custom-sounds` folder to `feesh-custom-sounds.zip` (note: there should be no subfolder named `feesh-custom-sounds` inside of zip).

1. Install the Resource Pack in Minecraft:
  - Open Minecraft Settings (Options)
  - Go to Resource Packs
  - Click "Open Pack Folder"
  - Copy your ResourcePack ZIP file to this folder
  - Return to the game and move the ResourcePack to the active packs list

### Sound Configuration Files

The sound configuration files are located at:

```
<MC>/config/feesh/userCatchSounds.json
```

and 

```
<MC>/config/feesh/userDropSounds.json
```

Those files are automatically created on first mod launch and contain default embedded mod sounds.

### userCatchSounds.json File Structure

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
        "source": "custom_emperor_sound.ogg"
    },
}
```

Each key is the name of a rare sea creature in uppercase (e.g., "YETI", "REINDRAKE", "THE LOCH EMPEROR").

The `source` field contains the sound file name:
- For embedded mod sounds: use the full file name with `.ogg` extension (e.g., `"feesh_notification.ogg"`)
- For custom sounds: use the file name (e.g., `"custom_emperor_sound.ogg"`)

### userDropSounds.json File Structure

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
- For custom sounds: use the file name (e.g., `"custom_dye_sound.ogg"`)
- For embedded mod sounds: use the file name with `.ogg` extension (e.g., `"feesh_oh-my-god.ogg"`)
