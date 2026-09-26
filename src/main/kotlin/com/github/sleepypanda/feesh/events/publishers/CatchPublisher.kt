package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.CatchEvent
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.InteractActionType
import com.github.sleepypanda.feesh.events.models.OwnFishingHookDespawnedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.GRAY
import java.util.Date
import net.minecraft.world.entity.item.ItemEntity
import kotlin.math.sqrt

object CatchPublisher {
    private const val CATCH_XP_ORB_SOUND_PATH = "entity.experience_orb.pickup"
    private const val TRASH_ITEM_SEARCH_RADIUS = 5.0
    // ♪ MUSICAL CATCH! You caught a Music Disc - Cat!
    private val TREASURE_CATCH_PATTERN = Regex("^. (GOOD|GREAT|OUTSTANDING|MUSICAL) CATCH!")
    private val JUNK_CATCH_PATTERN = Regex("^. (GOOD JUNK|GREAT JUNK|OUTSTANDING JUNK) CATCH!")
    private val TROPHY_CATCH_PATTERN = Regex("^. (TROPHY FISH|TROPHY FROG)!")

    private var lastCatchAt: Date? = null
    private var lastRodRightClickedAt: Date? = null
    private var lastCatchXpOrbSoundAt: Date? = null
    private var lastTreasureChatAt: Date? = null
    private var lastJunkChatAt: Date? = null
    private var lastSeaCreatureAt: Date? = null

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(OwnFishingHookDespawnedEvent::class, ::onOwnFishingHookDespawned)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)
        EventBus.subscribe(SoundPlayedEvent::class, ::onSoundPlayed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastRodRightClickedAt = null
        lastCatchAt = null
        lastCatchXpOrbSoundAt = null
        lastTreasureChatAt = null
        lastJunkChatAt = null
        lastSeaCreatureAt = null
    }

    // Backup for catches that do not have chat message (raw fish, log, etc.).
    // Chat message appears before fishing hook despawns, so those catches are tracked in chat message handlers.
    // Some successful catches do not have chat message, so we detect them by
    // - reel in fishing rod with right click while it's in fluid
    // - hear XP orb sound
    private fun onOwnFishingHookDespawned(@Suppress("UNUSED_PARAMETER") event: OwnFishingHookDespawnedEvent) {
        CommonUtils.runWithCatching("Failed to track fishing hook despawned in CatchPublisher") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (isSomethingCaughtRecently()) return

            if (!wasWithinMs(lastRodRightClickedAt, 500L)) return
            if (!FishingHookUtils.wasFishingHookSubmergedMillisecondsAgo(500)) return
            if (!wasWithinMs(lastCatchXpOrbSoundAt, 300L)) return

            val droppedItem = findClosestDroppedItemNearBobber()
            tryPublishTrashCatch(droppedItem)
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track sea creature catch in CatchPublisher") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (event.seaCreatureName == SeaCreatureNames.VANQUISHER) return

            lastSeaCreatureAt = Date()
            tryPublishCatchFromChat()
        }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle fishing rod interaction in CatchPublisher") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return
            if (FishingHookUtils.getActiveFishingHook() == null) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return
            if (!ItemUtils.isFishingRod(heldItem)) return

            lastRodRightClickedAt = Date()
        }
    }

    // Note: Treasure catch might happen together with SC catch if Precursor Drone pet equipped.
    // No double CatchEvent should happen in this case.
    private fun onChat(event: ChatCancellableEvent) {
        CommonUtils.runWithCatching("Failed to track catch chat message in CatchPublisher") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

            if (TREASURE_CATCH_PATTERN.containsMatchIn(event.unformattedText)) {
                lastTreasureChatAt = Date()
                tryPublishCatchFromChat()
                return
            }

            if (JUNK_CATCH_PATTERN.containsMatchIn(event.unformattedText)) {
                lastJunkChatAt = Date()
                tryPublishCatchFromChat()
            }

            if (TROPHY_CATCH_PATTERN.containsMatchIn(event.unformattedText)) {
                tryPublishCatchFromChat()
            }
        }
    }

    // Trash catches like raw fish usually play XP orb sound(s) (with volume 0.5 + 0.1 or just 0.1).
    // Used as backup signal on bobber despawn when there is no catch chat.
    private fun onSoundPlayed(event: SoundPlayedEvent) {
        CommonUtils.runWithCatching("Failed to track catch XP orb sound in CatchPublisher") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (event.soundId.path != CATCH_XP_ORB_SOUND_PATH) return
            if (event.volume != 0.1f && event.volume != 0.5f) return

            val player = FeeshMod.mc.player ?: return
            val distanceSqr = EntityUtils.getDistanceSqr(player.x, player.y, player.z, event.x, event.y, event.z)
            if (distanceSqr > 1.0) return // Usually sounds played with distance (non-sqr) 0.1 and 0.7

            lastCatchXpOrbSoundAt = Date()
        }
    }

    private fun tryPublishCatchFromChat() {
        if (isSomethingCaughtRecently()) return
        publishCatch()
    }

    private fun tryPublishTrashCatch(droppedItem: NearbyDroppedItem?) {
        if (isSomethingCaughtRecently()) return
        publishCatch(
            isTrash = true,
            itemNameFormatted = droppedItem?.nameFormatted,
            itemNameUnformatted = droppedItem?.nameUnformatted,
        )
    }

    private fun publishCatch(
        isTrash: Boolean = false,
        itemNameFormatted: String? = null,
        itemNameUnformatted: String? = null,
    ) {
        lastCatchAt = Date()
        EventBus.publish(
            CatchEvent(
                isTreasureCatch = wasWithinMs(lastTreasureChatAt, 750L),
                isJunkCatch = wasWithinMs(lastJunkChatAt, 750L),
                isSeaCreature = wasWithinMs(lastSeaCreatureAt, 750L),
                isTrash = isTrash,
                itemNameFormatted = itemNameFormatted,
                itemNameUnformatted = itemNameUnformatted,
            )
        )
    }

    // Dropped loot (Raw Fish, Salmon, Coal, etc.) spawns at the bobber as it despawns.
    private fun findClosestDroppedItemNearBobber(): NearbyDroppedItem? {
        val hook = FishingHookUtils.getLastActiveFishingHook() ?: return null
        val world = FeeshMod.mc.level ?: return null
        val maxDistanceSqr = TRASH_ITEM_SEARCH_RADIUS * TRASH_ITEM_SEARCH_RADIUS

        val closest = world.entitiesForRendering()
            .filterIsInstance<ItemEntity>()
            .mapNotNull { itemEntity ->
                val stack = itemEntity.item
                if (stack.isEmpty) return@mapNotNull null
                val distanceSqr = EntityUtils.getDistanceSqr(itemEntity.x, itemEntity.y, itemEntity.z, hook.x, hook.y, hook.z)
                if (distanceSqr > maxDistanceSqr) return@mapNotNull null
                val nameText = stack.customName ?: stack.hoverName
                NearbyDroppedItem(
                    distanceSqr = distanceSqr,
                    nameFormatted = nameText.getFormattedString(),
                    nameUnformatted = nameText.getUnformattedString(),
                )
            }
            .minByOrNull { it.distanceSqr }

        if (closest != null) {
            val distance = "%.2f".format(sqrt(closest.distanceSqr))
            ChatUtils.sendLocalChat("Trash item: ${closest.nameFormatted} ${GRAY}($distance blocks)", true)
        }

        return closest
    }

    private data class NearbyDroppedItem(
        val distanceSqr: Double,
        val nameFormatted: String,
        val nameUnformatted: String,
    )

    private fun isSomethingCaughtRecently(): Boolean {
        return wasWithinMs(lastCatchAt, 750L) // Flash V proc might be ~1s
    }

    private fun wasWithinMs(timestamp: Date?, windowMs: Long): Boolean {
        if (timestamp == null) return false
        return Date().time - timestamp.time <= windowMs
    }
}
