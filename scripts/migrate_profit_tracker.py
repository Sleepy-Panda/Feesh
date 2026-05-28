#!/usr/bin/env python3
"""Merge old fishing profit tracker JSON into Feesh format."""

from __future__ import annotations

import json
import re
import sys
from difflib import SequenceMatcher
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DROPS_KT = ROOT / "src/main/kotlin/com/github/sleepypanda/feesh/constants/FishingProfitDrops.kt"

SKIP_IDS = {
    "HOT_BAIT",
    "BABY_YETI;3",
    "BABY_YETI;4",
    "FISH_BAIT",
    "SPOOKY_BAIT",
    "SHARK_BAIT",
}

REMAP = {
    "ATTRIBUTE_SHARD_SPIRIT_AXE;1": "SHARD_ENT",
    "ATTRIBUTE_SHARD_UNITY_IS_STRENGTH;1": "SHARD_TADGANG",
    "ATTRIBUTE_SHARD_FISHERMAN;1": "SHARD_NIGHT_SQUID",
    "ATTRIBUTE_SHARD_CHEAPSTAKE;1": "SHARD_COD",
    "ATTRIBUTE_SHARD_FOREST_FISHING;1": "SHARD_VERDANT",
    "ATTRIBUTE_SHARD_BAYOU_BITER;1": "SHARD_TITANOBOA",
    "ATTRIBUTE_SHARD_BLAZING_FORTUNE;1": "SHARD_LAVA_FLAME",
    "ATTRIBUTE_SHARD_FISHING_SPEED;1": "SHARD_WATER_HYDRA",
    "ATTRIBUTE_SHARD_LOST_AND_FOUND;1": "SHARD_SALMON",
    "ATTRIBUTE_SHARD_RABBIT_CREW;1": "SHARD_CARROT_KING",
}

ENCHANT_BOOK_BASES = {
    "MAGNET",
    "FRAIL",
    "LURE",
    "ANGLER",
    "SPIKED_HOOK",
    "RESPITE",
    "CHARM",
    "PROSPERITY",
    "ULTIMATE_FLASH",
    "PISCARY",
    "LUCK_OF_THE_SEA",
    "CORRUPTION",
    "CASTER",
    "BLESSING",
    "FIRE_PROTECTION",
    "MAGMARIZER",
}


def load_feesh_items() -> dict[str, str]:
    text = DROPS_KT.read_text(encoding="utf-8")
    items: dict[str, str] = {}
    for block in re.split(r"FishingProfitDropInfo\(", text)[1:]:
        id_m = re.search(r'itemId = "([^"]+)"', block)
        name_m = re.search(r'itemName = "((?:\\.|[^"\\])*)"', block)
        if id_m:
            name = name_m.group(1) if name_m else id_m.group(1)
            items[id_m.group(1)] = name.replace(r"\"", '"').replace(r"\u0027", "'")
    return items


def normalize_source_id(source_id: str) -> str:
    if source_id in REMAP:
        return REMAP[source_id]
    if source_id.startswith("LAVA_HORSE"):
        return source_id.replace("LAVA_HORSE", "LAVAHORSE", 1)
    dash = re.fullmatch(r"([A-Z0-9_]+)-(\d+)", source_id)
    if dash:
        return f"{dash.group(1)}:{dash.group(2)}"
    if ";" in source_id:
        base, suffix = source_id.split(";", 1)
        if base in ENCHANT_BOOK_BASES and suffix.isdigit():
            return f"ENCHANTMENT_{base}_{suffix}"
    return source_id


def resolve_exact_feesh_id(source_id: str, feesh_items: dict[str, str]) -> str | None:
    normalized = normalize_source_id(source_id)
    if normalized in feesh_items:
        return normalized
    if source_id in feesh_items:
        return source_id
    return None


def closest_feesh_id_for_report(source_id: str, feesh_items: dict[str, str]) -> str | None:
    normalized = normalize_source_id(source_id)
    candidates = list(feesh_items.keys())
    best_id: str | None = None
    best_ratio = 0.0
    for candidate in candidates:
        ratio = SequenceMatcher(None, normalized, candidate).ratio()
        if ratio > best_ratio:
            best_ratio = ratio
            best_id = candidate
    return best_id if best_ratio >= 0.5 else None


def find_target_key(target: dict, feesh_id: str) -> str | None:
    if feesh_id in target:
        return feesh_id
    for key, entry in target.items():
        if entry.get("itemId") == feesh_id:
            return key
    return None


def dedupe_by_item_id(target: dict) -> list[str]:
    by_item_id: dict[str, str] = {}
    removed: list[str] = []
    for key, entry in list(target.items()):
        item_id = entry["itemId"]
        if item_id not in by_item_id:
            by_item_id[item_id] = key
            continue
        keep_key = by_item_id[item_id]
        keep = target[keep_key]
        keep["amount"] = max(int(keep["amount"]), int(entry["amount"]))
        del target[key]
        removed.append(key)
    return removed


def migrate(json1: dict, json2: dict) -> tuple[dict, dict]:
    feesh_items = load_feesh_items()
    items = json1["fishing"]["fishingProfitTracker"]["items"]
    target = json2["fishingProfit"]["total"]["profitTrackerItems"]

    skipped_explicit: list[str] = []
    skipped_no_exact_match: list[dict] = []
    migrated_count = 0
    updated_existing = 0
    created_new = 0

    for source_key, data in items.items():
        if source_key in SKIP_IDS:
            skipped_explicit.append(source_key)
            continue

        incoming_amount = int(data["totalAmount"])
        feesh_id = resolve_exact_feesh_id(source_key, feesh_items)
        if feesh_id is None:
            skipped_no_exact_match.append(
                {
                    "sourceKey": source_key,
                    "normalized": normalize_source_id(source_key),
                    "totalAmount": incoming_amount,
                    "closestFeeshItemId": closest_feesh_id_for_report(source_key, feesh_items),
                }
            )
            continue

        existing_key = find_target_key(target, feesh_id)
        if existing_key is not None:
            entry = target[existing_key]
            entry["amount"] = max(int(entry["amount"]), incoming_amount)
            updated_existing += 1
        else:
            target[feesh_id] = {
                "itemName": feesh_items[feesh_id],
                "itemId": feesh_id,
                "amount": incoming_amount,
                "totalItemProfit": 0.0,
            }
            created_new += 1
        migrated_count += 1

    deduped_keys = dedupe_by_item_id(target)

    report = {
        "migratedEntries": migrated_count,
        "updatedExisting": updated_existing,
        "createdNew": created_new,
        "skippedExplicit": skipped_explicit,
        "skippedNoExactMatch": skipped_no_exact_match,
        "dedupedKeysRemoved": deduped_keys,
    }
    return json2, report


def main() -> None:
    if len(sys.argv) != 4:
        print("Usage: migrate_profit_tracker.py <json1> <json2> <output_json>", file=sys.stderr)
        sys.exit(1)
    json1_path, json2_path, out_path = map(Path, sys.argv[1:4])
    with json1_path.open(encoding="utf-8") as f:
        json1 = json.load(f)
    with json2_path.open(encoding="utf-8") as f:
        json2 = json.load(f)
    result, report = migrate(json1, json2)
    out_path.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    report_path = out_path.with_suffix(".migration-report.json")
    report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
