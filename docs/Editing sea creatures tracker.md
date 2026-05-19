# Editing Sea creatures tracker

## Table of contents

- [Intro](#intro)
- [Editing sea creatures](#editing-sea-creatures)

## Intro

The **Sea creatures tracker** has two view modes: **[Session]** (resets on game close by default, unless disabled) and **[Total]** (persists until you reset it manually). This guide explains how to add, change, or remove sea creatures in each mode.

It can be used if some sea creature was not tracked during fishing, or you want to initialize your past catches history in Feesh.

Each tracker line (sea creature) may have the following information in the overlay, depending on overlay settings:

- `Yeti: 100`
- `Yeti: 100 | DH: 5`
- `Yeti: 100 | DH: 5 | BS: 2`

Basically, it contains:

- Overall count of this sea creature from all sources (caught + cocooned)
- DH: count of times the caught sea creature was double hooked on catch
- BS: count of times when the sea creature was cocooned via your Bloodshot

So, `Yeti: 100 | DH: 5 | BS: 2` means that you've got 100 own Yetis - 98 caught and 2 from cocoon; Double Hook proc'ed 5 times.

As long as you modify any of those parameters, the rest of statistics (totals, percents) adjust automatically.

## Editing sea creatures

### 1. Overlay buttons

Open your **inventory** while the overlay is visible. Each sea creature line has click actions: `[x]`

| Button | Action |
|--------|--------|
| `[x]` | Delete the sea creature from tracker (with confirmation) |

The action affects only the currently selected view mode. Use `Click to show [Total] / [Session]` if you need to edit the other view mode.

### 2. Commands

You can use chat commands when you need to set or adjust counts for specific sea creature. Make sure the overlay is **enabled**, to see the results of commands execution.

| Action | [Session] | [Total] |
|--------|-----------|---------|
| Set / adjust counts | `/feeshSetSeaCreatureCount <SEA_CREATURE_NAME> <TOTAL_COUNT> [DH_COUNT] [BS_COUNT]` | `/feeshSetSeaCreatureCountTotal <SEA_CREATURE_NAME> <TOTAL_COUNT> [DH_COUNT] [BS_COUNT]` |
| Delete sea creature (with confirmation) | `/feeshDeleteSeaCreature <SEA_CREATURE_NAME>` | `/feeshDeleteSeaCreatureTotal <SEA_CREATURE_NAME>` |

**`<SEA_CREATURE_NAME>`** - exact name from the game (e.g. `Yeti`, `Lord Jawbus`, `The Loch Emperor`, `Jumpin' Jack`); case insensitive.

**Count arguments** (each of `TOTAL_COUNT`, `DH_COUNT`, `BS_COUNT`):

| Format | Meaning |
|--------|---------|
| `100` | Set count to **100** (replace current value) |
| `+1` | Add **1** to current count |
| `-1` | Subtract **1** from current count |
| `+0` | No change |
| `0` | Set count to **0** |

- **`TOTAL_COUNT`** - required. Affects total count from all sources (caught + cocooned). Total count must be **> 0** after applying `TOTAL_COUNT`.
- **`DH_COUNT`** - optional. Affects double hook count if provided. If omitted, the current value remains unchanged. Double hook count must be **>= 0** and **<= caught count / 2** after applying `DH_COUNT`.
- **`BS_COUNT`** - optional. Affects cocooned count if provided. If omitted, the current value remains unchanged. Cocooned count must be **>= 0** and **<= total** after applying `BS_COUNT`.

### Examples

```text
/feeshSetSeaCreatureCount Yeti 100
/feeshSetSeaCreatureCount Yeti 100 10 1
/feeshSetSeaCreatureCount Yeti +1
/feeshSetSeaCreatureCount Yeti +3 +1 +1
/feeshSetSeaCreatureCount Yeti +0 +0 -10
/feeshSetSeaCreatureCount Lord Jawbus 100 -10 0
/feeshSetSeaCreatureCountTotal Reindrake 5 1 0
/feeshDeleteSeaCreature Yeti
```
