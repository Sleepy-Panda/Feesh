#!/usr/bin/env node
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, "..");
const DROPS_KT = path.join(
  ROOT,
  "src/main/kotlin/com/github/sleepypanda/feesh/constants/FishingProfitDrops.kt"
);

const SKIP_IDS = new Set([
  "HOT_BAIT",
  "BABY_YETI;3",
  "BABY_YETI;4",
  "FISH_BAIT",
  "SPOOKY_BAIT",
  "SHARK_BAIT",
]);

const REMAP = {
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
};

const ENCHANT_BOOK_BASES = new Set([
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
]);

function loadFeeshItems() {
  const text = fs.readFileSync(DROPS_KT, "utf8");
  const map = new Map();
  const blockRe =
    /FishingProfitDropInfo\(\s*itemId = "([^"]+)"[\s\S]*?itemName = "((?:\\.|[^"\\])*)"/g;
  let m;
  while ((m = blockRe.exec(text)) !== null) {
    map.set(m[1], m[2].replace(/\\"/g, '"').replace(/\\u0027/g, "'"));
  }
  return map;
}

function normalizeSourceId(sourceId) {
  if (REMAP[sourceId]) return REMAP[sourceId];
  if (sourceId.startsWith("LAVA_HORSE")) return sourceId.replace("LAVA_HORSE", "LAVAHORSE");
  const dash = sourceId.match(/^([A-Z0-9_]+)-(\d+)$/);
  if (dash) return `${dash[1]}:${dash[2]}`;
  if (sourceId.includes(";")) {
    const [base, suffix] = sourceId.split(";", 2);
    if (ENCHANT_BOOK_BASES.has(base) && /^\d+$/.test(suffix)) {
      return `ENCHANTMENT_${base}_${suffix}`;
    }
  }
  return sourceId;
}

function resolveExactFeeshId(sourceId, feeshItems) {
  const normalized = normalizeSourceId(sourceId);
  if (feeshItems.has(normalized)) return normalized;
  if (feeshItems.has(sourceId)) return sourceId;
  return null;
}

function similarity(a, b) {
  if (!a.length && !b.length) return 1;
  const matrix = Array.from({ length: a.length + 1 }, () =>
    Array(b.length + 1).fill(0)
  );
  for (let i = 0; i <= a.length; i++) matrix[i][0] = i;
  for (let j = 0; j <= b.length; j++) matrix[0][j] = j;
  for (let i = 1; i <= a.length; i++) {
    for (let j = 1; j <= b.length; j++) {
      const cost = a[i - 1] === b[j - 1] ? 0 : 1;
      matrix[i][j] = Math.min(
        matrix[i - 1][j] + 1,
        matrix[i][j - 1] + 1,
        matrix[i - 1][j - 1] + cost
      );
    }
  }
  const dist = matrix[a.length][b.length];
  return 1 - dist / Math.max(a.length, b.length);
}

function closestFeeshIdForReport(sourceId, feeshItems) {
  const normalized = normalizeSourceId(sourceId);
  let bestId = null;
  let best = 0;
  for (const id of feeshItems.keys()) {
    const ratio = similarity(normalized, id);
    if (ratio > best) {
      best = ratio;
      bestId = id;
    }
  }
  return best >= 0.5 ? bestId : null;
}

function findTargetKey(target, feeshId) {
  if (target[feeshId]) return feeshId;
  for (const [key, entry] of Object.entries(target)) {
    if (entry.itemId === feeshId) return key;
  }
  return null;
}

function dedupeByItemId(target) {
  const byItemId = new Map();
  const removed = [];
  for (const [key, entry] of Object.entries({ ...target })) {
    const itemId = entry.itemId;
    if (!byItemId.has(itemId)) {
      byItemId.set(itemId, key);
      continue;
    }
    const keepKey = byItemId.get(itemId);
    target[keepKey].amount = Math.max(
      Number(target[keepKey].amount),
      Number(entry.amount)
    );
    delete target[key];
    removed.push(key);
  }
  return removed;
}

function migrate(json1, json2, feeshItems) {
  const items = json1.fishing.fishingProfitTracker.items;
  const target = json2.fishingProfit.total.profitTrackerItems;

  const report = {
    migratedEntries: 0,
    updatedExisting: 0,
    createdNew: 0,
    skippedExplicit: [],
    skippedNoExactMatch: [],
    dedupedKeysRemoved: [],
  };

  for (const [sourceKey, data] of Object.entries(items)) {
    if (SKIP_IDS.has(sourceKey)) {
      report.skippedExplicit.push(sourceKey);
      continue;
    }
    const incomingAmount = Number(data.totalAmount);
    const feeshId = resolveExactFeeshId(sourceKey, feeshItems);
    if (!feeshId) {
      report.skippedNoExactMatch.push({
        sourceKey,
        normalized: normalizeSourceId(sourceKey),
        totalAmount: incomingAmount,
        closestFeeshItemId: closestFeeshIdForReport(sourceKey, feeshItems),
      });
      continue;
    }
    const existingKey = findTargetKey(target, feeshId);
    if (existingKey) {
      target[existingKey].amount = Math.max(
        Number(target[existingKey].amount),
        incomingAmount
      );
      report.updatedExisting++;
    } else {
      target[feeshId] = {
        itemName: feeshItems.get(feeshId),
        itemId: feeshId,
        amount: incomingAmount,
        totalItemProfit: 0.0,
      };
      report.createdNew++;
    }
    report.migratedEntries++;
  }

  report.dedupedKeysRemoved = dedupeByItemId(target);
  return report;
}

const [json1Path, json2Path, outPath] = process.argv.slice(2);
if (!json1Path || !json2Path || !outPath) {
  console.error(
    "Usage: node migrate_profit_tracker.mjs <json1> <json2> <output.json>"
  );
  process.exit(1);
}

const feeshItems = loadFeeshItems();
const json1 = JSON.parse(fs.readFileSync(json1Path, "utf8"));
const json2 = JSON.parse(fs.readFileSync(json2Path, "utf8"));
const report = migrate(json1, json2, feeshItems);
fs.writeFileSync(outPath, JSON.stringify(json2, null, 2) + "\n", "utf8");
const reportPath = outPath.replace(/\.json$/i, ".migration-report.json");
fs.writeFileSync(reportPath, JSON.stringify(report, null, 2) + "\n", "utf8");
console.log(JSON.stringify(report, null, 2));
