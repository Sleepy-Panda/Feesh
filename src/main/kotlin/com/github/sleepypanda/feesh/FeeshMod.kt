package com.github.sleepypanda.feesh

import com.github.sleepypanda.feesh.features.alerts.RareCatchAlert
import com.github.sleepypanda.feesh.features.alerts.ChumBucketAutoPickupAlert
import com.github.sleepypanda.feesh.features.alerts.PetLevelUpAlert
import com.github.sleepypanda.feesh.features.alerts.SpiritMaskAlert
import com.github.sleepypanda.feesh.features.alerts.ThunderBottleChargedAlert
import com.github.sleepypanda.feesh.features.alerts.AnyReindrakeAlert
import com.github.sleepypanda.feesh.features.alerts.NonFishingArmorAlert
import com.github.sleepypanda.feesh.features.alerts.GoldenFishSpawnAlert
import com.github.sleepypanda.feesh.features.alerts.HotspotGoneAlert
import com.github.sleepypanda.feesh.features.alerts.SaltExpiredAlert
import com.github.sleepypanda.feesh.features.alerts.PlayerDeathAlert
import com.github.sleepypanda.feesh.features.alerts.LootshareAlert
import com.github.sleepypanda.feesh.features.alerts.FishingBagDisabledAlert
import com.github.sleepypanda.feesh.features.alerts.RareDropAlert
import com.github.sleepypanda.feesh.features.alerts.WormTheFishCaughtAlert
import com.github.sleepypanda.feesh.features.alerts.NessieDestinationAlert
import com.github.sleepypanda.feesh.features.chat.RareCatchMessage
import com.github.sleepypanda.feesh.features.chat.RareDropMessage
import com.github.sleepypanda.feesh.features.chat.RareCatchAllChatMessage
import com.github.sleepypanda.feesh.features.chat.CompactCatchMessages
import com.github.sleepypanda.feesh.features.chat.PlayerDeathMessage
import com.github.sleepypanda.feesh.features.chat.HotspotFoundMessage
import com.github.sleepypanda.feesh.features.chat.LootshareMessage
import com.github.sleepypanda.feesh.features.help.VersionChecker
import com.github.sleepypanda.feesh.features.help.Welcome
import com.github.sleepypanda.feesh.features.commands.PersonalBestCommand
import com.github.sleepypanda.feesh.features.commands.SpiderDenRainScheduleCommand
import com.github.sleepypanda.feesh.features.commands.PetLevelUpPricesCommand
import com.github.sleepypanda.feesh.features.commands.FearMongererShopPricesCommand
import com.github.sleepypanda.feesh.features.commands.GearCraftPricesCommand
import com.github.sleepypanda.feesh.features.commands.JunkerJoelShopPricesCommand
import com.github.sleepypanda.feesh.features.commands.TerryShopPricesCommand
import com.github.sleepypanda.feesh.features.commands.FeeshSettingsCommand
import com.github.sleepypanda.feesh.features.commands.PlayTestSoundCommand
import com.github.sleepypanda.feesh.features.commands.SetTrackerDropsCommand
import com.github.sleepypanda.feesh.features.commands.PauseAllTrackersCommand
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.NearbyEntitiesCounter
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreatureHpTracker
import com.github.sleepypanda.feesh.features.overlays.FishingHookTimer
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.BarnFishingTimer
import com.github.sleepypanda.feesh.features.overlays.DeployablesTimer
import com.github.sleepypanda.feesh.features.overlays.RainTimer
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.GalateaWaterTracker
import com.github.sleepypanda.feesh.features.overlays.ArchfiendDiceProfitTracker
import com.github.sleepypanda.feesh.features.overlays.FishingFestivalTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesPerHourTracker
import com.github.sleepypanda.feesh.features.items.background.KatWrongPetsHighlighter
import com.github.sleepypanda.feesh.features.items.background.TrashBooksHighlighter
import com.github.sleepypanda.feesh.features.items.background.BackgroundHighlighterManager
import com.github.sleepypanda.feesh.features.items.slottext.ThunderBottleProgress
import com.github.sleepypanda.feesh.features.items.slottext.AutoRecombFlag
import com.github.sleepypanda.feesh.features.items.slottext.MobyDuckProgress
import com.github.sleepypanda.feesh.features.items.slottext.SlotTextRendererManager
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.publishers.SeaCreaturesPublisher
import com.github.sleepypanda.feesh.events.publishers.RareDropsPublisher
import com.github.sleepypanda.feesh.events.publishers.PartyChatPublisher
import com.github.sleepypanda.feesh.events.publishers.PetLevelUpPublisher
import com.github.sleepypanda.feesh.events.publishers.SacksItemPickupPublisher
import com.github.sleepypanda.feesh.events.publishers.ArmorStandPublisher
import com.github.sleepypanda.feesh.events.publishers.ItemEntityPublisher
import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight
import com.github.sleepypanda.feesh.settings.Settings
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.KeybindUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.GuiUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.utils.gui.MoveGuis
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.minecraft.SharedConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
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
        ChatUtils.init()
        GuiUtils.init()
        PriceUtils.init()
        KeybindUtils.init()

        // Event publishers
        SeaCreaturesPublisher.init()
        RareDropsPublisher.init()
        PartyChatPublisher.init()
        PetLevelUpPublisher.init()
        SacksItemPickupPublisher.init()
        ArmorStandPublisher.init()
        ItemEntityPublisher.init()

        // Rendering
        RareMobHighlight.init()

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
        SaltExpiredAlert.init()
        PlayerDeathAlert.init()
        LootshareAlert.init()
        FishingBagDisabledAlert.init()
        WormTheFishCaughtAlert.init()
        NessieDestinationAlert.init()

        // Chat
        RareCatchMessage.init()
        RareDropMessage.init()
        RareCatchAllChatMessage.init()
        CompactCatchMessages.init()
        PlayerDeathMessage.init()
        HotspotFoundMessage.init()
        LootshareMessage.init()

        // Overlays
        JerryWorkshopTracker.init()
        NearbyEntitiesCounter.init()
        SeaCreaturesTracker.init()
        SeaCreatureHpTracker.init()
        TreasureFishingTracker.init()
        FishingHookTimer.init()
        BarnFishingTimer.init()
        DeployablesTimer.init()
        BayouTracker.init()
        WaterHotspotsTracker.init()
        CrimsonIsleTracker.init()
        GalateaWaterTracker.init()
        ArchfiendDiceProfitTracker.init()
        FishingFestivalTracker.init()
        FishingProfitTracker.init()
        MagmaCoreFishingTracker.init()
        SeaCreaturesPerHourTracker.init()
        RainTimer.init()

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
