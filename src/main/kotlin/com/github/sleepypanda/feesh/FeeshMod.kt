package com.github.sleepypanda.feesh

import com.github.sleepypanda.feesh.features.alerts.*
import com.github.sleepypanda.feesh.features.chat.*
import com.github.sleepypanda.feesh.features.commands.*
import com.github.sleepypanda.feesh.features.help.*
import com.github.sleepypanda.feesh.features.items.background.*
import com.github.sleepypanda.feesh.features.items.slottext.*
import com.github.sleepypanda.feesh.features.overlays.*
import com.github.sleepypanda.feesh.features.personalbests.*
import com.github.sleepypanda.feesh.features.rendering.*
import com.github.sleepypanda.feesh.features.sounds.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.publishers.*
import com.github.sleepypanda.feesh.settings.Settings
import com.github.sleepypanda.feesh.utils.*
import com.github.sleepypanda.feesh.utils.data.*
import com.github.sleepypanda.feesh.utils.gui.*
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

class FeeshMod : ClientModInitializer {
    companion object {
        internal const val MOD_ID = "feesh"
        internal const val MOD_NAME = "Feesh"

        internal val LOGGER = LoggerFactory.getLogger(MOD_ID)

        lateinit var version: String

        @JvmField
        val mc: Minecraft = Minecraft.getInstance()
        @JvmField
        val mcVersion: String = SharedConstants.getCurrentVersion().id()

        @JvmStatic
        lateinit var INSTANCE: FeeshMod
            private set
    }

    val configurator = Configurator("feesh")
    val settings = Settings.register(configurator)

    init {
        INSTANCE = this
    }

    override fun onInitializeClient() {
        version = getModVersion()
        LOGGER.info("Loading $MOD_NAME v$version for ${mcVersion}...")

        PersistentDataManager.init()
        CustomSoundsManager.init()
        EventBus.init()
        FeeshSettingsCommand.init()
        Welcome.init()
        VersionChecker.init()

        // Utils
        WorldUtils.init()
        PlayerUtils.init()
        FishingHookUtils.init()
        BaitUtils.init()
        ChatUtils.init()
        GuiUtils.init()
        PriceUtils.init()
        KeybindUtils.init()

        // Event publishers
        SeaCreaturesPublisher.init()
        SeaCreaturesCocoonPublisher.init()
        RareDropsPublisher.init()
        PartyChatPublisher.init()
        TrophyDiscoveredPublisher.init()
        PetLevelUpPublisher.init()
        SacksItemPickupPublisher.init()
        IceEssenceStatusBarPublisher.init()
        ArmorStandPublisher.init()
        ItemEntityPublisher.init()

        // Rendering
        RareMobHighlight.init()
        HidePlayersNearBobber.init()

        // Alerts
        RareCatchAlert.init()
        RareDropAlert.init()
        SpiritMaskAlert.init()
        ChumBucketAutoPickupAlert.init()
        PetLevelUpAlert.init()
        ThunderBottleChargedAlert.init()
        AnyReindrakeAlert.init()
        NonFishingArmorAlert.init()
        GoldenFishSpawnAlert.init()
        HotspotGoneAlert.init()
        WormholeGoneAlert.init()
        SaltExpiredAlert.init()
        PlayerDeathAlert.init()
        LootshareAlert.init()
        FishingBagDisabledAlert.init()
        BaitAlert.init()
        WormTheFishCaughtAlert.init()
        NessieDestinationAlert.init()
        PuddleJumperTimerAlert.init()
        TrophyFrogDiscoveredAlert.init()
        TrophyFishDiscoveredAlert.init()

        // Chat
        RareCatchMessage.init()
        RareDropMessage.init()
        RareCatchAllChatMessage.init()
        CompactCatchMessages.init()
        PlayerDeathMessage.init()
        HotspotFoundMessage.init()
        LootshareMessage.init()
        TrophyFrogDiscoveredMessage.init()
        TrophyFishDiscoveredMessage.init()
        DoubleHookPersonalBest.init()

        // Overlays
        JerryWorkshopTracker.init()
        NearbyEntitiesCounter.init()
        SeaCreaturesTracker.init()
        SeaCreatureHpTracker.init()
        TreasureFishingTracker.init()
        FishingHookTimer.init()
        BaitTracker.init()
        BarnFishingTimer.init()
        DeployablesTimer.init()
        ConsumablesTimer.init()
        BayouTracker.init()
        WaterHotspotsTracker.init()
        CrimsonIsleTracker.init()
        GalateaWaterTracker.init()
        LotusAtollTracker.init()
        ArchfiendDiceProfitTracker.init()
        FishingFestivalTracker.init()
        FishingProfitTracker.init()
        MagmaCoreFishingTracker.init()
        SeaCreaturesPerHourTracker.init()
        RainTimer.init()
        MuteReindrakeGifts.init()

        MoveGuis.init() // After all overlays are initialized and registered FeeshGui objects

        // Items
        KatWrongPetsHighlighter.init()
        TrashBooksHighlighter.init()
        BackgroundHighlighterManager.init() // After all highlighters are initialized and registered

        ThunderBottleProgress.init()
        MobyDuckProgress.init()
        AutoRecombFlag.init()
        SlotTextRendererManager.init() // After all slot text renderers are initialized and registered

        // Commands
        PersonalBestCommand.init()
        SpiderDenRainScheduleCommand.init()
        PetLevelUpPricesCommand.init()
        GearCraftPricesCommand.init()
        FearMongererShopPricesCommand.init()
        JunkerJoelShopPricesCommand.init()
        TerryShopPricesCommand.init()
        PlayTestSoundCommand.init()
        SetTrackerDropsCommand.init()
        PauseAllTrackersCommand.init()

        LOGGER.info("$MOD_NAME loaded successfully!")
    }

    private fun getModVersion(): String {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unspecified")
    }
}
