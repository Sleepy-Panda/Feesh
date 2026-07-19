# Editing tracker drops

## Table of contents

- [Intro](#intro)
- [Command](#command)
- [Examples](#examples)

## Intro

Several area-specific trackers show **rare drops statistics** alongside sea creature catch stats. Examples of those trackers are Bayou, Water Hotspots, Crimson Isle, Lotus Atoll, Treasure fishing.
You can use `/feeshSetTrackerDrops` chat command to initialize drop count, sea creatures/treasures since last drop, and the last drop date.

Each drop line in the tracker typically shows:

- Total drop count (e.g. `Titanoboa Sheds: 5`)
- Last drop time (e.g. `Last on: 7h 15m ago`)
- Sea creatures caught since last drop (e.g. `Last on: 30 Titanoboas ago`)

Make sure the relevant tracker is **enabled** and **visible** to verify the result.

## Command

```
/feeshSetTrackerDrops <DROP_ID> <DROP_COUNT> <SC_COUNT_SINCE_LAST> [LAST_ON_DATE]
```

| Argument | Required | Description |
|----------|----------|-------------|
| `DROP_ID` | Yes | Item identifier (see below) |
| `DROP_COUNT` | Yes | Total number of drops you have. Must be **> 0** |
| `SC_COUNT_SINCE_LAST` | Yes | Sea creatures caught since the last drop. Must be **>= 0** |
| `LAST_ON_DATE` | No | When the last drop happened. Format: `YYYY-MM-DD hh:mm:ss`. Cannot be in the future. |

`DROP_ID` should be one of the following:

| Tracker | Drop | `DROP_ID` |
|---------|------|-----------|
| Bayou | Titanoboa Shed | `TITANOBOA_SHED` |
| Bayou | Snake Eyes | `SNAKE_EYES` |
| Water Hotspots | Tiki Mask | `TIKI_MASK` |
| Crimson Isle | Radioactive Vial | `RADIOACTIVE_VIAL` |
| Lotus Atoll | Prince's Crown Jewel | `PRINCE_CROWN_JEWEL` |

### Treasure fishing (Treasure Dye)

Treasure fishing tracker uses a different third argument: good/great/outstanding catches breakdown to show RNG meter.

```
/feeshSetTrackerDrops DYE_TREASURE <DROP_COUNT> <GOOD>/<GREAT>/<OUTSTANDING> [LAST_ON_DATE]
```

| Argument | Required | Description |
|----------|----------|-------------|
| `DROP_ID` | Yes | `DYE_TREASURE` |
| `DROP_COUNT` | Yes | Total Treasure Dyes obtained. Must be **> 0** |
| `GOOD/GREAT/OUTSTANDING` | Yes | Treasure catches since last dye, split by type (e.g. `12/123/1234`). Each part must be **>= 0** |
| `LAST_ON_DATE` | No | When the last drop happened. Format: `YYYY-MM-DD hh:mm:ss`. Cannot be in the future. |

Treasures since last drop (shown as "N treasures ago") is the **sum** of good + great + outstanding. The breakdown also allows showing the RNG meter percentage.

## Examples

```text
/feeshSetTrackerDrops TITANOBOA_SHED 5 30 2025-05-30 23:59:00
/feeshSetTrackerDrops RADIOACTIVE_VIAL 2 30
/feeshSetTrackerDrops SNAKE_EYES 2 1500 2024-03-18 14:05:00
/feeshSetTrackerDrops TIKI_MASK 5 500
/feeshSetTrackerDrops PRINCE_CROWN_JEWEL 2 0 2025-01-01 12:00:00
/feeshSetTrackerDrops DYE_TREASURE 2 1234/123/12 2025-05-30 23:59:00
/feeshSetTrackerDrops DYE_TREASURE 2 0/0/0
```
