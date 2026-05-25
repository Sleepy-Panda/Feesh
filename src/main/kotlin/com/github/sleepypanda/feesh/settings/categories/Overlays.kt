package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.github.sleepypanda.feesh.features.commands.PauseAllTrackersCommand
import com.github.sleepypanda.feesh.features.commands.SetTrackerDropsCommand
import com.github.sleepypanda.feesh.features.overlays.ArchfiendDiceProfitTracker
import com.github.sleepypanda.feesh.features.overlays.BarnFishingTimer
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.FishingFestivalTracker
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesPerHourTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.features.overlays.GalateaWaterTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreatureHpTracker
import com.github.sleepypanda.feesh.settings.models.HpTrackableSeaCreatureTypes
import com.github.sleepypanda.feesh.utils.gui.MoveGuis
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen
import net.minecraft.Util
import java.awt.Color

enum class SeaCreaturesTrackerDisplayMode(val displayName: String) {
    ONLY_RARE("Only rare"),
    ALL("All");

    override fun toString(): String = displayName
}

enum class SeaCreaturesTrackerSorting(val displayName: String) {
    CATCHES_COUNT_DESC("Catches count (DESC)"),
    CATCHES_COUNT_ASC("Catches count (ASC)"),
    RARITY_DESC("Rarity (DESC)"),
    RARITY_ASC("Rarity (ASC)");

    override fun toString(): String = displayName
}

enum class FishingHookTimerMode(val displayName: String) {
    UNTIL_REEL_IN("Until reel in"),
    SINCE_CASTED("Since casted");

    override fun toString(): String = displayName
}

enum class NearbyEntitiesCounterTypes(val displayName: String) {
    LEGION("Legion"),
    BOBBING_TIME("Bobbin' Time"),
    CHUMCAP_BUCKETS("Chumcap Buckets");

    override fun toString(): String = displayName
}

enum class CrimsonIsleTrashGearDropsPriceMode(val displayName: String) {
    NORMAL("Normal AH price"),
    ESSENCE("Crimson Essence"),
    NPC_PRICE("NPC price");

    override fun toString(): String = displayName
}

object Overlays : CategoryKt("Overlays") {
    private fun getCustomStyleDescription(overlayName: String): String {
        return "Whether to apply custom style from \"Custom overlays style\" category to $overlayName. When disabled, the overlay is drawn as a text without those decorations."
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Common"
        }

        button {
            title = "Move GUIs"
            description = "Allows to move and resize all GUIs enabled in the Overlays settings section. Executes ${WHITE}/${MoveGuis.COMMAND_NAME}"
            text = "Click to move"
            onClick {
                MoveGuis.moveAllGuis()
            }
        }

        button {
            title = "Pause all trackers keybind"
            description = "Set a keybind in Minecraft's Controls menu to pause all active trackers on button pressed (so the timers stop). Default is PAUSE.\nExecutes ${WHITE}/${PauseAllTrackersCommand.COMMAND_NAME}"
            text = "Click to open"
            onClick {
                val mc = FeeshMod.mc
                mc.schedule {
                    val currentScreen = mc.screen ?: return@schedule
                    mc.setScreen(KeyBindsScreen(currentScreen, mc.options))
                }
            }
        }
    }

    var trackersAutoPauseSeconds by int(180) {
        this.name = Translated("Idle seconds to auto-pause all trackers")
        this.description = Translated("Pauses elapsed timers on various widgets when you stop fishing for this long (in seconds).\n${YELLOW}Make sure to choose enough time to kill any fishing mob and get loot from it, so it gets counted by the trackers!")
        this.range = 30..300
        this.slider = true
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Custom overlays style"
            this.description = "Customize the style of the overlays. You can enable/disable applying this style for each overlay individually."
        }
    }

    var overlaysBackground by boolean(false) {
        this.name = Translated("Overlays background")
        this.description = Translated("Draw a background (gradient or single color) behind the overlays for better readability.")
    }

    var overlaysBackgroundColor1 by color(Color(0, 0, 0, 70).rgb) {
        this.name = Translated("Overlays background color #1")
        this.description = Translated("Select background color with opacity. It is used as top color for vertical gradient.")
        this.allowAlpha = true
    }

    var overlaysBackgroundColor2 by color(Color(0, 0, 0, 70).rgb) {
        this.name = Translated("Overlays background color #2")
        this.description = Translated("Select background color with opacity. It is used as bottom color for vertical gradient. Use same color as above to fill the background with a single color.")
        this.allowAlpha = true
    }

    var overlaysBorder by boolean(false) {
        this.name = Translated("Overlays border")
        this.description = Translated("Draws a border around the overlays.")
    }

    var overlaysBorderColor by color(Color(255, 255, 255, 255).rgb) {
        this.name = Translated("Overlays border color")
        this.description = Translated("Select border color with opacity.")
        this.allowAlpha = true
    }

    var overlaysBorderWidth by int(1) {
        this.name = Translated("Overlays border width")
        this.description = Translated("Select border width.")
        this.range = 1..5
        this.slider = true
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Nearby entities"
        }
    }

    var nearbyEntitiesCounterOverlay by boolean(false) {
        this.name = Translated("Nearby entities counter")
        this.description = Translated("""
Shows an overlay with various counters for nearby entities that give you fishing buffs:
- Legion - amount of players within 30 blocks (excluding you).
- Bobbin' Time - amount of fishing hooks within 30 blocks (including your own hook).
- Chumcap Buckets - amount of Chumcap Buckets within 30 blocks (including your own bucket). Chum Buckets are not counted.

Hidden if you have no fishing rod in your hotbar!""".trimIndent())
    }

    var nearbyEntitiesCounterTypes by select(NearbyEntitiesCounterTypes.LEGION, NearbyEntitiesCounterTypes.BOBBING_TIME) {
        this.name = Translated("Nearby entities counter types to display")
        this.searchTerms = NearbyEntitiesCounterTypes.values().map { it.displayName }.toList()
    }

    var nearbyEntitiesCounterCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Nearby entities counter"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Barn fishing timer"
        }
    }

    var barnFishingTimerOverlay by boolean(false) {
        this.name = Translated("Barn fishing timer")
        this.description = Translated("Shows an overlay with the count of sea creatures nearby and how long they have been alive. Mostly useful for barn fishing. Hidden if you have no fishing rod in your hotbar or if you are wearing Hunter armor!\nTo reset: ${WHITE}/${BarnFishingTimer.RESET_COMMAND}")
    }

    init {
        button {
            title = "Reset barn fishing timer keybind"
            description = "Set a keybind in Minecraft's Controls menu to reset the barn fishing timer."
            text = "Click to open"
            onClick {
                val mc = FeeshMod.mc
                mc.schedule {
                    val currentScreen = mc.screen ?: return@schedule
                    mc.setScreen(KeyBindsScreen(currentScreen, mc.options))
                }
            }
        }
    }

    var barnFishingTimerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Barn fishing timer"))
    }
      
    init {
        separator {
            this.title = "${AQUA}${BOLD}Deployables"
        }
    }

    var deployablesTimerOverlay by boolean(false) {
        this.name = Translated("Deployables timer")
        this.description = Translated("Shows an overlay with the remaining time of your deployable items placed nearby.")
    }

    var deployablesOverlayTypes by select(DeployableTypes.TOTEM_OF_CORRUPTION, *DeployableTypes.values()) {
        this.name = Translated("Select deployables to show in overlay")
        this.searchTerms = DeployableTypes.values().map { it.displayName }.toList()
    }

    var deployablesTimerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Deployables timer"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Consumables"
        }
    }

    var consumablesTimerOverlay by boolean(false) {
        this.name = Translated("Consumables timer")
        this.description = Translated("Shows an overlay with the remaining time of active Moby-Duck.")
    }

    var consumablesTimerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Consumables timer"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures HP"
        }
    }

    var seaCreaturesHpOverlay by boolean(false) {
        this.name = Translated("Sea creatures HP")
        this.description = Translated("Shows an overlay with the HP of nearby sea creatures when they're in lootshare range.\nDisplays ~5 seconds immunity indicator for damage reduction period that some sea creature types have. Not 100% precise!")
    }

    var seaCreaturesHpTrackedList by ObservableEntry(select(
            *HpTrackableSeaCreatureTypes.values().filter { it.isEnabledByDefault }.toTypedArray(),
        ) {
            this.name = Translated("Select sea creatures")
            this.description = Translated("Which sea creatures to track and show in the HP overlay.")
            this.searchTerms = HpTrackableSeaCreatureTypes.values().map { it.displayName }.toList()
    }) { prev, new ->
        if (!prev.contentEquals(new)) {
            SeaCreatureHpTracker.updateEnabledMobTypes()
        }
    }

    var seaCreaturesHpOverlayMaxCount by int(7) {
        this.name = Translated("Maximum entries count")
        this.description = Translated("Show maximum N sea creatures nearby (to limit overlay size). Sea creatures with lower HP come first.")
        this.range = 1..20
        this.slider = true
    }

    var seaCreaturesHpCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Sea creatures HP"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures caught"
        }
    }

    var seaCreaturesTrackerOverlay by boolean(false) {
        this.name = Translated("Sea creatures tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with the overview of the sea creatures caught, and different related statistics. This overlay has [Session] and [Total] view mode.
${GRAY}To view details: Hover over a line while in Inventory screen.
${GRAY}To reset [Session]: ${WHITE}/${SeaCreaturesTracker.RESET_SESSION}
${GRAY}To reset [Total]: ${WHITE}/${SeaCreaturesTracker.RESET_TOTAL}
""".trimIndent())
    }

    var seaCreaturesTrackerMode by enum(SeaCreaturesTrackerDisplayMode.ALL) {
        this.name = Translated("Sea creatures tracker display mode")
        this.description = Translated("Setups whether to hide regular sea creatures in the overlay, showing just rare ones. All sea creatures are tracked regardless this setting.")
    }

    var countCocoonedSeaCreatures by boolean(true) {
        this.name = Translated("Count cocooned sea creatures")
        this.description = Translated("Include sea creatures cocooned by your Bloodshot reforge in the Sea creatures tracker, as SB treats them as your own.")
    }

    var showSeaCreaturesPercentage by boolean(true) {
        this.name = Translated("Show percentage")
        this.description = Translated("Show percentage for each sea creature out of total amount of sea creatures. If disabled, the statistics will be still visible in the tooltip.")
    }

    var showSeaCreaturesDoubleHookStatistics by boolean(true) {
        this.name = Translated("Show double hook statistics")
        this.description = Translated("Show statistics for each sea creature how often it was double hooked (shown as 'DH' in the overlay). If disabled, the statistics will be still visible in the tooltip.")
    }

    var showCocoonedStatistics by boolean(false) {
        this.name = Translated("Show cocooned statistics")
        this.description = Translated("Show statistics for each sea creature how often it was cocooned by your Bloodshot reforge (shown as 'BS' in the overlay). If disabled, the statistics will be still visible in the tooltip.")
    }

    var seaCreaturesTrackerSorting by enum(SeaCreaturesTrackerSorting.RARITY_DESC) {
        this.name = Translated("Sea creatures sorting")
        this.description = Translated("Setups sorting order for the sea creatures list.")
    }

    var seaCreaturesTrackerShowTop by int(50) {
        this.name = Translated("Maximum lines count")
        this.description = Translated("Show top N lines for sea creatures in Session/Total views. Remaining entries will be grouped under 'Other sea creatures'.")
        this.range = 1..100
        this.slider = true
    }

    var resetSeaCreaturesTrackerSessionOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the Sea creatures tracker [Session] when you close Minecraft.")
    }

    init {
        button {
            title = "Editing Sea creatures tracker guide"
            description = "Opens a guide on how to adjust sea creature counts and statistics in Sea creatures tracker [Session] and [Total]."
            text = "Click to open"
            onClick {
                Util.getPlatform().openUri("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Editing%20sea%20creatures%20tracker.md")
            }
        }
    }

    var seaCreaturesTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Sea creatures tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Fishing Hook"
        }
    }

    var fishingHookTimerOverlay by boolean(false) {
        this.name = Translated("Fishing hook timer")
        this.description = Translated("Displays the timer of your fishing hook, as well as the sign when a fish arrived and can be reeled in. For this to work, please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Fishing Timer")
    }

    var fishingHookTimerMode by enum(FishingHookTimerMode.UNTIL_REEL_IN) {
        this.name = Translated("Fishing hook timer mode")
        this.description = Translated("'Until reel in' shows countdown while fish is swimming towards the fishing hook. 'Since casted' shows the timer while the fishing hook is casted.")
    }

    var fishingHookFishArrivedTemplate by string("§c§l!!!") {
        this.name = Translated("Custom fish arrived template")
        this.description = Translated("Replace default !!! with your custom text when a fish arrived to your hook. Leave empty to use default.")
    }

    var fishingHookFishTimerTemplate by string("§e§l{timer}") {
        this.name = Translated("Custom timer format")
        this.description = Translated("Replace default with your custom timer text. Use {timer} to insert timer seconds into the template. Leave empty to use default.")
    }

    init {
        button {
            title = "Colors & formatting guide"
            description = "For settings above with custom text templates, please explore color codes and formatting codes."
            text = "Click to open"
            onClick {
                Util.getPlatform().openUri("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Colors%20and%20formatting%20guide.md")
            }
        }
    }

    var fishingHookTimerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Fishing hook timer"))
    }
    
    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures per hour"
        }
    }

    var seaCreaturesPerHourTrackerOverlay by boolean(false) {
        this.name = Translated("Sea creatures per hour tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with the sea creatures caught per hour, and total sea creatures caught per session. Not persistent - resets on MC restart.
${GRAY}To reset: ${WHITE}/${SeaCreaturesPerHourTracker.RESET_COMMAND}
${GRAY}To pause: ${WHITE}/${SeaCreaturesPerHourTracker.PAUSE_COMMAND}
""".trimIndent())
    }

    var seaCreaturesPerHourCountDoubleHookAsTwo by boolean(true) {
        this.name = Translated("Count double hook as 2")
        this.description = Translated("When enabled, a double hook catch counts as 2 sea creatures. When disabled, it counts as 1.")
    }

    var seaCreaturesPerHourTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Sea creatures per hour tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Rain, Thunder, Blizzard"
        }
    }

    var rainTimerOverlay by boolean(false) {
        this.name = Translated("Rain/Thunder/Blizzard timer")
        this.description = Translated("${GRAY}Shows an overlay with the active/upcoming Rain/Thunder/Blizzard timer in The Park, Spider's Den, Lotus Atoll, Backwater Bayou, and Jerry's Workshop. Please enable ${YELLOW}TabList settings -> General Info widget -> Show Rain / Show Blizzard")
    }

    var rainTimerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Rain/Thunder/Blizzard timer"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Fishing Festival"
        }
    }

    var fishingFestivalTrackerOverlay by boolean(false) {
        this.name = Translated("Fishing Festival tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with sharks caught during the Fishing Festival. Not persistent - resets on MC restart.
${GRAY}To reset: ${WHITE}/${FishingFestivalTracker.RESET_COMMAND}
""".trimIndent())
    }

    var fishingFestivalTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Fishing Festival tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Jerry's Workshop"
        }
    }

    var jerryWorkshopTrackerOverlay by boolean(false) {
        this.name = Translated("Jerry's Workshop tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with Yeti / Reindrake catch statistics while in the Jerry Workshop.
${GRAY}To reset: ${WHITE}/${JerryWorkshopTracker.RESET_COMMAND}
""".trimIndent())
    }

    var resetJerryWorkshopTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Jerry Workshop tracker when you close Minecraft.")
    }

    var jerryWorkshopTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Jerry's Workshop tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Bayou tracker"
        }
    }

    var bayouTrackerOverlay by boolean(false) {
        this.name = Translated("Bayou tracker")
        this.description = Translated("""
${GRAY}Shows Titanoboa catch statistics and Titanoboa Shed drop statistics while fishing in Backwater Bayou.
${GRAY}To reset: ${WHITE}/${BayouTracker.RESET_COMMAND}
        """.trimIndent())
    }

    var resetBayouTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Bayou tracker when you close Minecraft.")
    }

    init {
        button {
            title = "Set Titanoboa Sheds count"
            description = "Explains in your chat how to init Titanoboa Sheds count and last drop date for the Bayou tracker."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${AQUA}${BOLD}Titanoboa Sheds setup${RESET}", true)
                ChatUtils.sendLocalChat("${YELLOW}Command: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME}${GOLD} <ITEM_ID> <COUNT> [LAST_ON_DATE]")
                ChatUtils.sendLocalChat("${DARK_AQUA}<ITEM_ID>: ${GRAY}must be TITANOBOA_SHED")
                ChatUtils.sendLocalChat("${DARK_AQUA}<COUNT>: ${GRAY}how many drops you got")
                ChatUtils.sendLocalChat("${DARK_AQUA}[LAST_ON_DATE]: ${GRAY}optional, YYYY-MM-DD hh:mm:ss, cannot be future")
                ChatUtils.sendLocalChat("${GREEN}Example: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} TITANOBOA_SHED 5 2025-05-30 23:59:00")
            }
        }
    }

    var bayouTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Bayou tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Water Hotspots tracker"
        }
    }

    var waterHotspotsTrackerOverlay by boolean(false) {
        this.name = Translated("Water Hotspots tracker")
        this.description = Translated("""
${GRAY}Shows Wiki Tiki catch statistics and Tiki Mask drop statistics while fishing in a Water Hotspot.
${GRAY}To reset: ${WHITE}/${WaterHotspotsTracker.RESET_COMMAND}
        """.trimIndent())
    }

    var resetWaterHotspotsTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Water Hotspots tracker when you close Minecraft.")
    }

    init {
        button {
            title = "Set Tiki Masks count"
            description = "Explains in your chat how to init Tiki Masks count and last drop date for the Water Hotspots tracker."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${AQUA}${BOLD}Tiki Masks setup${RESET}", true)
                ChatUtils.sendLocalChat("${YELLOW}Command: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME}${GOLD} <ITEM_ID> <COUNT> [LAST_ON_DATE]")
                ChatUtils.sendLocalChat("${DARK_AQUA}<ITEM_ID>: ${GRAY}must be TIKI_MASK")
                ChatUtils.sendLocalChat("${DARK_AQUA}<COUNT>: ${GRAY}how many drops you got")
                ChatUtils.sendLocalChat("${DARK_AQUA}[LAST_ON_DATE]: ${GRAY}optional, YYYY-MM-DD hh:mm:ss, cannot be future")
                ChatUtils.sendLocalChat("${GREEN}Example: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} TIKI_MASK 5 2025-05-30 23:59:00")
            }
        }
    }

    var waterHotspotsTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Water Hotspots tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Crimson Isle tracker"
        }
    }

    var crimsonIsleTrackerOverlay by boolean(false) {
        this.name = Translated("Crimson Isle tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with Fiery Scuttler & Ragnarok (when fishing in hotspot), Plhlegblast (when in Plhlegblast Pool), Thunder & Lord Jawbus catch statistics. Also has Radioactive Vial drop statistics.
${GRAY}To reset: ${WHITE}/${CrimsonIsleTracker.RESET_COMMAND}
        """.trimIndent())
    }

    var resetCrimsonIsleTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Crimson Isle tracker when you close Minecraft.")
    }

    init {
        button {
            title = "Set Radioactive Vials count"
            description = "Explains in your chat how to init Radioactive Vials count and last drop date."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${AQUA}${BOLD}Radioactive Vials setup${RESET}", true)
                ChatUtils.sendLocalChat("${YELLOW}Command: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME}${GOLD} <ITEM_ID> <COUNT> [LAST_ON_DATE]")
                ChatUtils.sendLocalChat("${DARK_AQUA}<ITEM_ID>: ${GRAY}must be RADIOACTIVE_VIAL")
                ChatUtils.sendLocalChat("${DARK_AQUA}<COUNT>: ${GRAY}how many drops you got")
                ChatUtils.sendLocalChat("${DARK_AQUA}[LAST_ON_DATE]: ${GRAY}optional, YYYY-MM-DD hh:mm:ss, cannot be future")
                ChatUtils.sendLocalChat("${GREEN}Example: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} RADIOACTIVE_VIAL 2 2025-05-30 23:59:00")
            }
        }
    }

    var crimsonIsleTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Crimson Isle tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Galatea water tracker"
        }
    }

    var galateaWaterTrackerOverlay by boolean(false) {
        this.name = Translated("Galatea water tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with The Loch Emperor and Nessie catch statistics while fishing in Galatea's water.
${GRAY}To reset: ${WHITE}/${GalateaWaterTracker.RESET_COMMAND}
        """.trimIndent())
    }

    var resetGalateaWaterTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Galatea water tracker when you close Minecraft.")
    }

    var galateaWaterTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Galatea water tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Treasure fishing"
        }
    }

    var treasureFishingTrackerOverlay by boolean(false) {
        this.name = Translated("Treasure fishing tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with Good/Great/Outstanding treasure catches and Treasure Dye drop statistics. This overlay has [Session] and [Total] view mode.
${GRAY}Reset session: ${WHITE}/${TreasureFishingTracker.RESET_SESSION_COMMAND}
${GRAY}Reset total: ${WHITE}/${TreasureFishingTracker.RESET_TOTAL_COMMAND}
        """.trimIndent())
    }

    var resetTreasureFishingTrackerSessionOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the Treasure fishing tracker [Session] when you close Minecraft.")
    }

    init {
        button {
            title = "Set Treasure Dyes count"
            description = "Explains in your chat how to setup Treasure Dyes count and last drop date."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${AQUA}${BOLD}Treasure Dyes setup${RESET}", true)
                ChatUtils.sendLocalChat("${YELLOW}Command: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME}${GOLD} <ITEM_ID> <COUNT> [LAST_ON_DATE]")
                ChatUtils.sendLocalChat("${DARK_AQUA}<ITEM_ID>: ${GRAY}must be DYE_TREASURE")
                ChatUtils.sendLocalChat("${DARK_AQUA}<COUNT>: ${GRAY}how many drops you got")
                ChatUtils.sendLocalChat("${DARK_AQUA}[LAST_ON_DATE]: ${GRAY}optional, YYYY-MM-DD hh:mm:ss, cannot be future")
                ChatUtils.sendLocalChat("${GREEN}Example: ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} DYE_TREASURE 2 2025-05-30 23:59:00")
            }
        }
    }

    var treasureFishingTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Treasure fishing tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Magma Core fishing"
        }
    }

    var magmaCoreFishingTrackerOverlay by boolean(false) {
        this.name = Translated("Magma Core fishing tracker")
        this.description = Translated("""
${GRAY}Shows an overlay for Magma Core fishing, with Lava Pigman/Lava Blaze catch stats and Magma Core drop profits (total and per hour), while in Crystal Hollows. This overlay has [Session] and [Total] view mode.
${GRAY}To reset [Session]: ${WHITE}/${MagmaCoreFishingTracker.RESET_COMMAND}
${GRAY}To reset [Total]: ${WHITE}/${MagmaCoreFishingTracker.RESET_TOTAL_COMMAND}
${GRAY}To pause: ${WHITE}/${MagmaCoreFishingTracker.PAUSE_COMMAND}
        """.trimIndent())
    }

    var magmaCoreFishingTrackerPriceMode by ObservableEntry(
        enum(PricingModeWithNpc.SELL_OFFER) {
            this.name = Translated("Price mode")
            this.description = Translated("How to calculate prices for Magma Core in the tracker.")
        }
    ) { prev, new ->
        if (prev != new) {
            MagmaCoreFishingTracker.refreshGui()
        }
    }

    var resetMagmaCoreFishingTrackerSessionOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the Magma Core fishing tracker [Session] when you close Minecraft.")
    }

    var magmaCoreFishingTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Magma Core fishing tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Archfiend Dice profit"
        }
    }

    var archfiendDiceProfitTrackerOverlay by boolean(false) {
        this.name = Translated("Archfiend Dice profit tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with your Archfiend Dice / High Class Archfiend Dice profits. This overlay has [Session] and [Total] view mode.
${GRAY}To reset [Session]: ${WHITE}/${ArchfiendDiceProfitTracker.RESET_COMMAND}
${GRAY}To reset [Total]: ${WHITE}/${ArchfiendDiceProfitTracker.RESET_TOTAL_COMMAND}
""".trimIndent())
    }

    var resetArchfiendDiceProfitTrackerSessionOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the Archfiend Dice profit tracker [Session] when you close Minecraft.")
    }

    var archfiendDiceProfitTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Archfiend Dice profit tracker"))
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Fishing profit"
        }
    }

    var fishingProfitTrackerOverlay by boolean(false) {
        this.name = Translated("Fishing profit tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with your profits you gained while fishing. This overlay has [Session] and [Total] view mode.
${GRAY}To count items added to your sacks, make sure to enable ${YELLOW}Skyblock Settings -> Personal -> Chat Feedback -> Sack Notifications
${GRAY}To reset [Session]: ${WHITE}/${FishingProfitTracker.RESET_COMMAND}
${GRAY}To reset [Total]: ${WHITE}/${FishingProfitTracker.RESET_TOTAL_COMMAND}
${GRAY}To pause: ${WHITE}/${FishingProfitTracker.PAUSE_COMMAND}
        """.trimIndent())
    }

    var fishingProfitTrackerPriceMode by ObservableEntry(
        enum(PricingModeWithNpc.SELL_OFFER) {
            this.name = Translated("Price mode")
            this.description = Translated("How to calculate prices for the dropped items in the Fishing profit tracker.")
        }
    ) { prev, new ->
        if (prev != new) {
            FishingProfitTracker.refreshTotalItemsProfits()
        }
    }
    
    var priceModeForCrimsonIsleTrashGearDrops by ObservableEntry(
        enum(CrimsonIsleTrashGearDropsPriceMode.NPC_PRICE) {
            this.name = Translated("Price mode for Crimson Isle trash gear drops")
            this.description = Translated("How to calculate prices for Crimson Isle fishing drops: Slug Boots, Moogma Leggings, Flaming Chestplate, Blade of the Volcano, Staff of the Volcano.")
        }
    ) { prev, new ->
        if (prev != new) {
            FishingProfitTracker.refreshTotalItemsProfits()
        }
    }

    var fishingProfitTrackerHideCheaperThan by int(1_000_000) {
        this.name = Translated("Hide cheap items [Session]")
        this.description = Translated("Items which are cheaper than the specified threshold in coins will be hidden in the fishing profit tracker [Session]. They will be grouped under 'Cheap items' section. Set to 0 to show all items.")
    }

    var fishingProfitTrackerHideCheaperThanTotal by int(1_000_000) {
        this.name = Translated("Hide cheap items [Total]")
        this.description = Translated("Items which are cheaper than the specified threshold in coins will be hidden in the fishing profit tracker [Total]. They will be grouped under 'Cheap items' section. Set to 0 to show all items.")
    }

    var fishingProfitTrackerShowTop by int(15) {
        this.name = Translated("Maximum lines count")
        this.description = Translated("Show top N lines for the most expensive items. Other cheaper items will be grouped under 'Cheap items' section. This works on top of 'Hide cheap items' setting.")
        this.range = 1..50
        this.slider = true
    }

    var shouldAnnounceRareDropsWhenPickup by boolean(true) {
        this.name = Translated("Announce rare drops")
        this.description = Translated("Send RARE DROP! message to player's chat when a rare item is added to the fishing profit tracker (for relatively rare items that have no RARE DROP! message from Hypixel by default).")
    }

    var shouldHideTimerInTotal by boolean(false) {
        this.name = Translated("Hide timer and coins/h in [Total] view")
        this.description = Translated("Hide timer and coins/h in the fishing profit tracker [Total] view. Useful if you want to add past drops to the tracker but do not know the elapsed time.")
    }

    var resetFishingProfitTrackerOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the fishing profit tracker [Session] when you close Minecraft.")
    }
 
    init {
        button {
            title = "Editing Fishing profit tracker guide"
            description = "Opens a guide on how to adjust item counts and elapsed time in the Fishing profit tracker [Session] and [Total]."
            text = "Click to open"
            onClick {
                Util.getPlatform().openUri("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Editing%20profit%20tracker.md")
            }
        }
    }

    var fishingProfitTrackerCustomStyle by boolean(true) {
        this.name = Translated("Apply custom style")
        this.description = Translated(getCustomStyleDescription("Fishing profit tracker"))
    }
}
