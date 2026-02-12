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
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesPerHourTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsAndBayouTracker
import com.github.sleepypanda.feesh.utils.gui.MoveGuis
import net.minecraft.util.Util
import net.minecraft.client.gui.screen.option.KeybindsScreen

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

enum class LegionBobbingTimeTrackerMode(val displayName: String) {
    BOTH("Both"),
    LEGION_ONLY("Legion only"),
    BOBBING_TIME_ONLY("Bobbin' Time only");

    override fun toString(): String = displayName
}

object Overlays : CategoryKt("Overlays") {
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
                mc.send {
                    mc.setScreen(KeybindsScreen(mc.currentScreen, mc.options))
                }
            }
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Legion & Bobbin' Time"
        }
    }

    var legionBobbingTimeTrackerOverlay by boolean(false) {
        this.name = Translated("Legion & Bobbin' Time tracker")
        this.description = Translated("Shows an overlay with the amount of players within 30 blocks (excluding you), and amount of fishing hooks within 30 blocks (including your own hook). Hidden if you have no fishing rod in your hotbar!")
    }

    var legionBobbingTimeTrackerMode by enum(LegionBobbingTimeTrackerMode.BOTH) {
        this.name = Translated("Legion & Bobbin' Time display mode")
        this.description = Translated("Choose which lines to show: both, Legion only, or Bobbin' Time only.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Barn fishing timer"
        }
    }

    var barnFishingTimerOverlay by boolean(false) {
        this.name = Translated("Barn fishing timer overlay")
        this.description = Translated("Shows an overlay with the count of sea creatures nearby and how long they have been alive. Mostly useful for barn fishing. Hidden if you have no fishing rod in your hotbar or if you are wearing Hunter armor!\nTo reset: ${WHITE}/${BarnFishingTimer.RESET_COMMAND}")
    }

    init {
        button {
            title = "Reset barn fishing timer keybind"
            description = "Set a keybind in Minecraft's Controls menu to reset the barn fishing timer."
            text = "Click to open"
            onClick {
                val mc = FeeshMod.mc
                mc.send {
                    mc.setScreen(KeybindsScreen(mc.currentScreen, mc.options))
                }
            }
        }
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
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures HP"
        }
    }

    var seaCreaturesHpOverlay by boolean(false) {
        this.name = Translated("Sea creatures HP")
        this.description = Translated("Shows an overlay with the HP of nearby rare sea creatures when they're in lootshare range. Displays ~5 seconds immunity indicator for damage reduction period that some sea creature types have. Not 100% precise!")
    }

    var seaCreaturesHpOverlayMaxCount by int(7) {
        this.name = Translated("Maximum entries count")
        this.description = Translated("Show maximum N sea creatures nearby (to limit overlay size). Sea creatures with lower HP come first.")
        this.range = 1..20
        this.slider = true
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures"
        }
    }

    var seaCreaturesTrackerOverlay by boolean(false) {
        this.name = Translated("Sea creatures tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with the overview of the sea creatures caught, and different related statistics. This overlay has [Session] and [Total] view mode.
${GRAY}To reset [Session]: ${WHITE}/${SeaCreaturesTracker.RESET_SESSION}
${GRAY}To reset [Total]: ${WHITE}/${SeaCreaturesTracker.RESET_TOTAL}
""".trimIndent())
    }

    var seaCreaturesTrackerMode by enum(SeaCreaturesTrackerDisplayMode.ALL) {
        this.name = Translated("Sea creatures tracker display mode")
        this.description = Translated("Setups whether to hide regular sea creatures in the overlay, showing just rare ones. All sea creatures are tracked regardless this setting.")
    }

    var showSeaCreaturesPercentage by boolean(true) {
        this.name = Translated("Show sea creatures percentage")
        this.description = Translated("Show statistics with a percentage for each sea creature. It is not shown when in the \"Only rare sea creatures\" mode.")
    }

    var showSeaCreaturesDoubleHookStatistics by boolean(true) {
        this.name = Translated("Show double hook statistics")
        this.description = Translated("Show statistics how often the sea creatures were double hooked.")
    }

    var seaCreaturesTrackerSorting by enum(SeaCreaturesTrackerSorting.RARITY_DESC) {
        this.name = Translated("Sea creatures sorting")
        this.description = Translated("Setups sorting order for the sea creatures.")
    }

    var resetSeaCreaturesTrackerSessionOnGameClosed by boolean(true) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the Sea creatures tracker [Session] when you close Minecraft.")
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
                Util.getOperatingSystem().open("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Colors%20and%20formatting%20guide.md")
            }
        }
    }
    
    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures per hour"
        }
    }

    var seaCreaturesPerHourTrackerOverlay by boolean(false) {
        this.name = Translated("Sea creatures per hour tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with the sea creatures per hour, and total sea creatures caught per session. Not persistent - resets on MC restart.
${GRAY}To reset: ${WHITE}/${SeaCreaturesPerHourTracker.RESET_COMMAND}
${GRAY}To pause: ${WHITE}/${SeaCreaturesPerHourTracker.PAUSE_COMMAND}
""".trimIndent())
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

    init {
        separator {
            this.title = "${AQUA}${BOLD}Water hotspots & Bayou tracker"
        }
    }

    var waterHotspotsAndBayouTrackerOverlay by boolean(false) {
        this.name = Translated("Water hotspots & Bayou tracker")
        this.description = Translated("""
${GRAY}Shows an overlay with Titanoboa (when fishing in Backwater Bayou) and Wiki Tiki (when in Water Hotspots) catch statistics. Also has Titanoboa Shed and Tiki Mask drop statistics.
${GRAY}To reset: ${WHITE}/${WaterHotspotsAndBayouTracker.RESET_COMMAND}
        """.trimIndent())
    }

    var resetWaterHotspotsAndBayouTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset on closing game")
        this.description = Translated("Automatically reset the Water hotspots & Bayou tracker when you close Minecraft.")
    }

    init {
        button {
            title = "Set Titanoboa Sheds / Tiki Masks count"
            description = "Explains in your chat how to init Titanoboa Sheds / Tiki Masks count and last drop date."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${WHITE}${BOLD}Titanoboa Sheds / Tiki Masks setup${RESET}", true)
                ChatUtils.sendLocalChat("\nDo ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:")
                ChatUtils.sendLocalChat("  - <ITEM_ID> is a mandatory item ID - TITANOBOA_SHED or TIKI_MASK.")
                ChatUtils.sendLocalChat("  - <COUNT> is a mandatory number of times you've dropped it.")
                ChatUtils.sendLocalChat("  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!")
                ChatUtils.sendLocalChat("\nExamples:")
                ChatUtils.sendLocalChat("/${SetTrackerDropsCommand.COMMAND_NAME} TITANOBOA_SHED 5 2025-05-30 23:59:00${RESET}")
                ChatUtils.sendLocalChat("/${SetTrackerDropsCommand.COMMAND_NAME} TIKI_MASK 5 2025-05-30 23:59:00${RESET}")
            }
        }
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
                ChatUtils.sendLocalChat("${WHITE}${BOLD}Radioactive Vials setup${RESET}", true)
                ChatUtils.sendLocalChat("\nDo ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:")
                ChatUtils.sendLocalChat("  - <ITEM_ID> is a mandatory item ID - RADIOACTIVE_VIAL.")
                ChatUtils.sendLocalChat("  - <COUNT> is a mandatory number of times you've dropped it.")
                ChatUtils.sendLocalChat("  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!")
                ChatUtils.sendLocalChat("\nExample:")
                ChatUtils.sendLocalChat("/${SetTrackerDropsCommand.COMMAND_NAME} RADIOACTIVE_VIAL 2 2025-05-30 23:59:00")
            }
        }
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
                ChatUtils.sendLocalChat("${WHITE}${BOLD}Treasure Dyes setup${RESET}", true)
                ChatUtils.sendLocalChat("\nDo ${WHITE}/${SetTrackerDropsCommand.COMMAND_NAME} <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:")
                ChatUtils.sendLocalChat("  - <ITEM_ID> is a mandatory item ID - DYE_TREASURE.")
                ChatUtils.sendLocalChat("  - <COUNT> is a mandatory number of times you've dropped it.")
                ChatUtils.sendLocalChat("  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!")
                ChatUtils.sendLocalChat("\nExample:")
                ChatUtils.sendLocalChat("/${SetTrackerDropsCommand.COMMAND_NAME} DYE_TREASURE 2 2025-05-30 23:59:00")
            }
        }
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
    
    var calculateProfitInCrimsonEssence by ObservableEntry(
        boolean(false) {
            this.name = Translated("Show profits in Crimson Essence when applicable")
            this.description = Translated("Calculate price in Crimson Essence for salvageable crimson fishing items e.g. Slug Boots, Moogma Leggings, Flaming Chestplate, Blade of the Volcano, Staff of the Volcano.")
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
            title = "Fishing profit tracker commands"
            description = "Explains in your chat how to use manual commands to adjust items count in the Fishing profit tracker [Session] and [Total]."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("${WHITE}${BOLD}Fishing profit tracker commands${RESET}", true)
                ChatUtils.sendLocalChat("\nUse these commands if you want to manually fix or import drops into the tracker:")
                ChatUtils.sendLocalChat("  - ${WHITE}/${FishingProfitTracker.SET_ITEM_COUNT_COMMAND} <ITEM_ID> <COUNT>${RESET} - sets item count in [Session].")
                ChatUtils.sendLocalChat("  - ${WHITE}/${FishingProfitTracker.SET_ITEM_COUNT_TOTAL_COMMAND} <ITEM_ID> <COUNT>${RESET} - sets item count in [Total].")
                ChatUtils.sendLocalChat("  - ${WHITE}/${FishingProfitTracker.DELETE_ITEM_COMMAND} <ITEM_ID>${RESET} - deletes item from [Session].")
                ChatUtils.sendLocalChat("  - ${WHITE}/${FishingProfitTracker.DELETE_ITEM_TOTAL_COMMAND} <ITEM_ID>${RESET} - deletes item from [Total].")
                ChatUtils.sendLocalChat("\n${GRAY}<ITEM_ID>${RESET} - ID of the fishing drop (for example MAGMA_FISH, SILVER_MAGMAFISH, BABY_YETI;4, etc.).")
                ChatUtils.sendLocalChat("${GRAY}<COUNT>${RESET} - positive integer with desired total amount of this item in the tracker.")
            }
        }
    }
}