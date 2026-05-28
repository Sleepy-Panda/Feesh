./node scripts/migrate_profit_tracker.mjs scripts/migration/json1.json scripts/migration/json2.json scripts/migration/feesh-merged.json


PROMPT:

/caveman 

I will provide you with 2 JSON files. They both represent some kind of in-game "profit tracker" which aggregates different items that I gained while playing. They are captured by 2 different mods. I want to swap to the second file's format and need to migrate past data from old JSON. I want you to merge content of the first JSON file into the second JSON file. Create an entry if missing; adjust the item count if it already exists. Second JSON is exact Feesh format, so it should be compatible with Feesh profit tracker drops @src/main/kotlin/com/github/sleepypanda/feesh/constants/FishingProfitDrops.kt 

Logic:
Look into "items" of JSON1, it should be migrated into "fishingProfit" -> "total" -> "profitTrackerItems" of JSON2. The rest of JSON2 should remain untouched. For each item, use maximum of (totalAmount, amount) as "amount". Set totalItemProfit to 0.0 for new entries or keep as is for existing. For key, itemId, and itemName, find the closest match by ID in the FishingProfitDrops -> items:
@src/main/kotlin/com/github/sleepypanda/feesh/constants/FishingProfitDrops.kt 

If the item exists in JSON2, use maximum value of (totalAmount, amount) as a new amount, do not change itemId/name.

Skip items: HOT_BAIT, BABY_YETI;3, BABY_YETI;4, FISH_BAIT, SPOOKY_BAIT, SHARK_BAIT

Remap the following items by IDs:
ATTRIBUTE_SHARD_SPIRIT_AXE;1 -> SHARD_ENT
ATTRIBUTE_SHARD_UNITY_IS_STRENGTH;1 -> SHARD_TADGANG
ATTRIBUTE_SHARD_FISHERMAN;1 -> SHARD_NIGHT_SQUID
ATTRIBUTE_SHARD_CHEAPSTAKE;1 -> SHARD_COD
ATTRIBUTE_SHARD_FOREST_FISHING;1 -> SHARD_VERDANT
ATTRIBUTE_SHARD_BAYOU_BITER;1 -> SHARD_TITANOBOA
ATTRIBUTE_SHARD_BLAZING_FORTUNE;1 -> SHARD_LAVA_FLAME
ATTRIBUTE_SHARD_FISHING_SPEED;1 -> SHARD_WATER_HYDRA
ATTRIBUTE_SHARD_LOST_AND_FOUND;1 -> SHARD_SALMON
ATTRIBUTE_SHARD_RABBIT_CREW;1 -> SHARD_CARROT_KING

Also summarize entries for those you did not find exact match by item ID, skip processing for them. Make sure the result JSON does not contain entries with duplicated keys/itemIDs.

Example:
Let's say for the item from JSON1
```
"INK_SACK": {
                    "timesGained": 4419,
                    "totalAmount": 23693,
                    "hidden": false
                  },
```

lets assume we have it in JSON2 - 
```
"INK_SACK": {
          "itemName": "Ink Sack",
          "itemId": "INK_SACK",
          "amount": 5,
          "totalItemProfit": 100.0
        },
```

the migrated entry should be 
```
"INK_SACK": {
          "itemName": "Ink Sack",
          "itemId": "INK_SACK",
          "amount": 23693,
          "totalItemProfit": 100.0
        },
```

Amount is maxuimum value from both values.


JSON1:
```

```

JSON2 (Feesh data.json):
```

```
