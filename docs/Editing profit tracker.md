# Editing Fishing profit tracker

The **Fishing profit tracker** has two view modes: **[Session]** (resets on game close by default, unless disabled) and **[Total]** (persists until you reset it manually). This guide explains how to add, change, or remove items in each mode.

It can be used if some item was not tracked during fishing, or you want to initialize your past drops history in Feesh.

## Table of contents

- [Editing tracker items](#editing-tracker-items)
- [Editing elapsed time](#editing-elapsed-time)

## Editing tracker items

### 1. Drop and pick up

While the tracker is **active** (visible and not paused), drop and pickup an item / stack of items. Newly picked up item(s) will be added to both [Session] and [Total] modes.

Please note, if the picked up items went to a sack, it may take up to 30 seconds to be counted. Basically, sack items get counted when SkyBlock shows `[Sacks] +N items` chat message.

### 2. Overlay buttons

Open your **inventory** while the overlay is visible. It makes no difference if it's paused or active. Each item line has click actions: `[+][-][x]`

| Button | Action |
|--------|--------|
| `[+]` | Add **1** to current count |
| `[-]` | Subtract **1** from current count |
| `[x]` | Delete the item from tracker (with confirmation) |

The commands affect only the currently selected view mode. Use `Click to show [Total] / [Session]` if you need to edit the other view mode.

### 3. Commands

You can use chat commands when you need to set or adjust count for the items. Make sure the overlay is **visible**, to control the results of commands execution.

| Action | [Session] | [Total] |
|--------|-----------|---------|
| Set / adjust item count | `/feeshSetItemCountFishingProfit <ITEM_ID> <COUNT>` | `/feeshSetItemCountFishingProfitTotal <ITEM_ID> <COUNT>` |
| Delete item (with confirmation) | `/feeshDeleteItemFishingProfit <ITEM_ID>` | `/feeshDeleteItemFishingProfitTotal <ITEM_ID>` |

**`<COUNT>` formats**

| Format | Meaning |
|--------|---------|
| `100` | Set item count to **100** - replace current count |
| `+1` | Add **1** to current count |
| `-1` | Subtract **1** from current count |

- `ITEM_ID` - look at the section below to find item ID.

### Examples

```text
/feeshSetItemCountFishingProfit RADIOACTIVE_VIAL +1
/feeshSetItemCountFishingProfit MAGMA_FISH 64000
/feeshSetItemCountFishingProfitTotal MAGMA_FISH_SILVER 10000
/feeshSetItemCountFishingProfit FLYING_FISH;4+100 3
/feeshSetItemCountFishingProfit ENCHANTED_SPONGE -10
/feeshDeleteItemFishingProfit ENCHANTED_SPONGE
```

### Item IDs

Commands and internal storage use **item IDs** aligned with **Bazaar** and **Auction House lowest bin** APIs.

1. **Built-in list** - Most fishing drops are defined in [`FishingProfitDrops.kt`](https://github.com/Sleepy-Panda/Feesh/blob/develop/src/main/kotlin/com/github/sleepypanda/feesh/constants/FishingProfitDrops.kt). Search that file for the item name; use the `itemId` field (e.g. `MAGMA_FISH`, `MAGMA_FISH_SILVER`, `CORRUPTED_NETHER_STAR`).
2. **Bazaar items** - Alternatively, for bazaar items, you can rely on `product_id` field of [Bazaar API](https://api.hypixel.net/skyblock/bazaar?product).
3. **Auction items** - Alternatively, for Auction items, you can rely on auction API (Settings -> **Auction price API**), use `product_id`.
4. **Max level pets** - see section below.

#### Max level pets (level 100 / 200)

Max level pets use a structured ID:

```text
<PET_NAME>;<RARITY_CODE>+<LEVEL>
```

- **PET_NAME** - Skyblock ID style, words joined with `_`, uppercase (e.g. `FLYING_FISH`, `BABY_YETI`, `HERMIT_CRAB`).
- **RARITY_CODE** - numeric: `0` Common, `1` Uncommon, `2` Rare, `3` Epic, `4` Legendary, `5` Mythic.
- **LEVEL** - `100`, or `200` for Dragon pets.

Examples:

- Legendary Flying Fish level 100: `FLYING_FISH;4+100`
- Epic Squid level 100: `SQUID;3+100`
- Legendary Golden Dragon level 200: `GOLDEN_DRAGON;4+200`

## Editing elapsed time

Coins/h is calculated from total profit and elapsed time. You might need to adjust elapsed time to fix coins/h after editing drop list.

While the overlay is **visible**, you can use the following commands:

| Action | [Session] | [Total] |
|--------|-----------|---------|
| Set / adjust time | `/feeshSetTimeFishingProfit <SECONDS>` | `/feeshSetTimeFishingProfitTotal <SECONDS>` |

**`<SECONDS>` formats**

| Format | Meaning |
|--------|---------|
| `10000` | Set elapsed time to **10000** seconds |
| `+500` | Add **500** seconds to current elapsed time |
| `-500` | Subtract **500** seconds from current elapsed time |

Result must be more than or equal to 0. Invalid input shows an error in chat.

Examples:

```text
/feeshSetTimeFishingProfit 3600
/feeshSetTimeFishingProfitTotal +7200
/feeshSetTimeFishingProfit -300
```

If you don't know elapsed time for [Total], you might use **Hide timer and coins/h in [Total]** to show only the items list.
