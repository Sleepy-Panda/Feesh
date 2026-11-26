# Colors and formatting guide

This guide explains how to use the color codes, formatting codes, and gradients in your custom text templates.

## Color codes overview

The following table represents color codes:

| Code  | Description       |
|-------|-------------------|
| `§0`  | Black text        |
| `§1`  | Dark blue text    |
| `§2`  | Dark green text   |
| `§3`  | Dark aqua text    |
| `§4`  | Dark red text     |
| `§5`  | Dark purple text  |
| `§6`  | Gold-colored text |
| `§7`  | Gray text         |
| `§8`  | Dark gray text    |
| `§9`  | Blue text         |
| `§a`  | Green text        |
| `§b`  | Aqua text         |
| `§c`  | Red text          |
| `§d`  | Light purple text |
| `§e`  | Yellow text       |
| `§f`  | White text        |

## Formatting codes overview

The following table represents formatting codes applied style to a text:

| Code  | Effect                 |
|-------|------------------------|
| `§r`  | Resets all formatting  |
| `§l`  | Bold text              |
| `§m`  | Strikethrough text     |
| `§n`  | Underlined text        |
| `§o`  | Italic text            |
| `§k`  | Obfuscated text        |

## How to use these codes

- Insert the code directly before the text you want to format.
- Always use formatting code **after** color code, e.g. §l (bold) should follow after §b (aqua) if you want aqua bold text.
- By default, white color is used if no other color codes applied.
- Example: `§cThis text is red §land bold §rand normal again.`

## Gradients

Some of the features support gradients (each text character is colored with different color).

Format: `{gradient:#RRGGBB>#RRGGBB}`
Example: `{gradient:#158AB7>#52CC9D}`

You can combine gradients with regular color/formatting codes from the section above.

Examples:

- `§7[{gradient:#158AB7>#52CC9D}Text§7]` - gray bracket, then gradient-colored text fading from `#158AB7` to `#52CC9D`, then gray bracket again.
- `{gradient:#158AB7>#52CC9D}§lText` - bold text with the same gradient.
