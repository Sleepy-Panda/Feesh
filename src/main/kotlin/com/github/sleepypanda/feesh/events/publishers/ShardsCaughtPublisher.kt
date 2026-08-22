package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.ShardCaughtEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ShardsCaughtPublisher {
    // ⛃ GOOD CATCH! You caught a Shinyfish Shard!
    // ⛃ GOOD CATCH! You caught an Abyssal Lanternfish Shard!
    //  GREAT CATCH! You caught a Giant Water Bug Shard!
    //  GOOD CATCH! You caught Solar Shard!
    //  GOOD CATCH! You caught a Water Snake Shard!
    private val SHARD_CATCH_PATTERN = Regex("^. (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught (?:(?:a|an) )?(?<shardName>.+) Shard!")

    //  OUTSTANDING CATCH! You caught Giant Water Bug Shard x7!
    //  GOOD CATCH! You caught Ember Shard x2!
    //  GOOD CATCH! You caught Shinyfish Shard x2!
    private val SHARDS_CATCH_PATTERN = Regex("^. (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught (?<shardName>.+) Shard x(?<count>[\\d]+)!")

    // You caught a Sea Archer Shard!
    // You caught x4 Sea Archer Shards!
    // You caught x4 Carrot King Shards!
    // You caught x2 Loch Emperor Shards!
    private val SHARDS_BLACK_HOLE_PATTERN = Regex("^You caught (?<shardsText>.+) Shard[s]?!")

    // CHARM! You charmed the Haggard and received 2 Haggard Shards!
    // CHARM! You charmed the Haggard and received 1 Haggard Shard!
    // CHARM! You charmed the Flaming Spider and received 2 Flaming Spider Shards!
    private val SHARDS_CHARMED_PATTERN = Regex("^CHARM! You charmed (?:a|an|the) .+ and received (?<count>[\\d]+) (?<shardName>.+) Shard[s]!")

    // LOOT SHARE You received 2 Titanoboa Shards for assisting CuzImCrzz!
    // LOOT SHARE You received 3 Magma Slug Shards for assisting OmeRuben!
    private val SHARDS_LOOTSHARED_PATTERN = Regex("^LOOT SHARE You received (?<shardsText>.+) Shard.*")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        CommonUtils.runWithCatching("Failed to handle Shard catch in publisher.") {
            val text = event.unformattedText

            SHARD_CATCH_PATTERN.find(text)?.run {
                val shard = groups["shardName"]?.value ?: return@onChat
                val shardName = "$shard Shard"
                publish(shardName, 1)
                return@onChat
            }

            SHARDS_CATCH_PATTERN.find(text)?.run {
                val shard = groups["shardName"]?.value ?: return@onChat
                val count = groups["count"]?.value?.replace(",", "")?.toIntOrNull() ?: return@onChat
                publish("$shard Shard", count)
                return@onChat
            }

            SHARDS_BLACK_HOLE_PATTERN.find(text)?.run {
                val shardsText = groups["shardsText"]?.value ?: return@onChat
                val (shardName, count) = parseCountAndName(shardsText, hasXPrefix = true)
                publish(shardName, count)
                return@onChat
            }

            SHARDS_CHARMED_PATTERN.find(text)?.run {
                val shard = groups["shardName"]?.value ?: return@onChat
                val count = groups["count"]?.value?.toIntOrNull() ?: return@onChat
                publish("$shard Shard", count)
                return@onChat
            }

            SHARDS_LOOTSHARED_PATTERN.find(text)?.run {
                val shardsText = groups["shardsText"]?.value ?: return@onChat
                val (shardName, count) = parseCountAndName(shardsText, hasXPrefix = false)
                publish(shardName, count)
                return@onChat
            }
        }
    }

    /** Parses count and name from texts like "a Sea Archer", "x4 Carrot King", "2 Titanoboa". */
    private fun parseCountAndName(shardsText: String, hasXPrefix: Boolean): Pair<String, Int> {
        val parts = shardsText.split(" ")
        val countText = parts.firstOrNull() ?: "a"
        val count = when (countText) {
            "a", "an" -> 1
            else -> {
                val normalized = if (hasXPrefix) countText.replace("x", "") else countText
                normalized.toIntOrNull() ?: 1
            }
        }
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        return shardName to count
    }

    private fun publish(shardName: String, count: Int) {
        if (shardName.isBlank() || count <= 0) return
        EventBus.publish(ShardCaughtEvent(shardName = shardName, count = count))
    }
}
