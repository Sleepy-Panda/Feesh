# Editing Treasure fishing tracker

## Table of contents

- [Intro](#intro)
- [Treasure catches](#treasure-catches)
- [Treasure Dye](#treasure-dye)
- [Examples](#examples)

## Intro

The **Treasure fishing tracker** has two view modes: **[Session]** (resets on game close by default, unless disabled) and **[Total]** (persists until you reset it manually). This guide explains how to set Good / Great / Outstanding catch counts and Treasure Dye drop statistics.

Make sure the overlay is **enabled**. Catch counts and dye stats are independent: changing catches does not change dye stats, and changing dye stats does not change catch totals, as they might be not the same.

## Treasure catches

| Action | [Session] | [Total] |
|--------|-----------|---------|
| Set counts | `/feeshSetTreasureCatches <GOOD>/<GREAT>/<OUTSTANDING>` | `/feeshSetTreasureCatchesTotal <GOOD>/<GREAT>/<OUTSTANDING>` |

Each part must be an integer number **>= 0** (e.g. `100/50/10`). Values replace the current counts.

## Treasure Dye

Treasure Dye counter and RNG meter uses `/feeshSetTrackerDrops` with a good/great/outstanding breakdown (catches **since last dye**, not overall totals).

```
/feeshSetTrackerDrops DYE_TREASURE <DROP_COUNT> <GOOD>/<GREAT>/<OUTSTANDING> [LAST_ON_DATE]
```

| Argument | Required | Description |
|----------|----------|-------------|
| `DROP_ID` | Yes | `DYE_TREASURE` |
| `DROP_COUNT` | Yes | Total Treasure Dyes obtained. Must be **>= 0**. Use `0` if you have not dropped a dye yet |
| `GOOD/GREAT/OUTSTANDING` | Yes | Treasure catches since last dye, split by type (e.g. `1234/123/12`). Each count must be **>= 0** |
| `LAST_ON_DATE` | No | When the last Treasure Dye drop happened. Format: `YYYY-MM-DD hh:mm:ss`. Cannot be in the future. Ignored when `DROP_COUNT` is `0` |

Treasures since last drop (shown as "N treasures ago") and RNG meter % are calculated automatically based on the sum of good + great + outstanding.

## Examples

```text
/feeshSetTreasureCatches 100/50/10
/feeshSetTreasureCatchesTotal 1234/123/12
/feeshSetTrackerDrops DYE_TREASURE 2 1234/123/12 2025-05-30 23:59:00
/feeshSetTrackerDrops DYE_TREASURE 2 0/0/0
/feeshSetTrackerDrops DYE_TREASURE 0 12/123/1234
```
