package com.github.sleepypanda.feesh.events.models

enum class ShardCaughtSource {
    TREASURE,
    BLACK_HOLE,
    CHARM,
    LOOTSHARE,
}

/**
 * Event for when fishing attribute shards are gained (treasure catch, black hole, charm/naga/salt, lootshare).
 * @param shardName Full shard item name without formatting, e.g. "Loch Emperor Shard".
 * @param count Number of shards gained.
 * @param source How the shards were gained.
 */
data class ShardCaughtEvent(
    val shardName: String,
    val count: Int,
    val source: ShardCaughtSource,
)
