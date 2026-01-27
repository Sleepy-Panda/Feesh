package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
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

object Overlays : CategoryKt("Overlays") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Common"
        }

        button {
            title = "Pause all trackers keybind"
            description = "Set a keybind in Minecraft's Controls menu to pause all active trackers on button pressed (so the timers stop). Default is PAUSE.\nExecutes ${AQUA}/feeshPauseAllTrackers"
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

    init {
        separator {
            this.title = "${AQUA}${BOLD}Barn fishing timer"
        }
    }

    var barnFishingTimerOverlay by boolean(false) {
        this.name = Translated("Barn fishing timer overlay")
        this.description = Translated("Shows an overlay with the count of sea creatures nearby and how long they have been alive. Mostly useful for barn fishing. Hidden if you have no fishing rod in your hotbar or if you are wearing Hunter armor!\nUse /feeshResetBarnFishingTimer to reset.")
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
Shows an overlay with the overview of the sea creatures caught, and different related statistics. This overlay has [Session] and [Total] view mode.
To reset [Session]: ${AQUA}/feeshResetSeaCreatures
To reset [Total]: ${AQUA}/feeshResetSeaCreaturesTotal
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
        this.description = Translated("Shows an overlay with the sea creatures per hour, and total sea creatures caught per session. Not persistent - resets on MC restart.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Jerry's Workshop"
        }
    }

    var jerryWorkshopTrackerOverlay by boolean(false) {
        this.name = Translated("Jerry's Workshop tracker")
        this.description = Translated("""
Shows an overlay with Yeti / Reindrake catch statistics while in the Jerry Workshop.
To reset: ${AQUA}/feeshResetJerryWorkshop
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
Shows an overlay with Titanoboa (when fishing in Backwater Bayou) and Wiki Tiki (when in Water Hotspots) catch statistics. Also has Titanoboa Shed and Tiki Mask drop statistics.
To reset: ${AQUA}/feeshResetWaterHotspotsAndBayou
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
                ChatUtils.sendLocalChat("""
${WHITE}${BOLD}Titanoboa Sheds / Tiki Masks setup${RESET}

Do ${AQUA}/feeshSetTrackerDrops <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:
  - <ITEM_ID> is a mandatory item ID - TITANOBOA_SHED or TIKI_MASK.
  - <COUNT> is a mandatory number of times you've dropped it.
  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!

Examples:
${AQUA}/feeshSetTrackerDrops TITANOBOA_SHED 5 2025-05-30 23:59:00${RESET}
${AQUA}/feeshSetTrackerDrops TIKI_MASK 5 2025-05-30 23:59:00${RESET}
                """.trimIndent(), true)
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
Shows an overlay with Fiery Scuttler & Ragnarok (when fishing in hotspot), Plhlegblast (when in Plhlegblast Pool), Thunder & Lord Jawbus catch statistics. Also has Radioactive Vial drop statistics.
To reset: ${AQUA}/feeshResetCrimsonIsle
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
                ChatUtils.sendLocalChat("""
${WHITE}${BOLD}Radioactive Vials setup${RESET}

Do ${AQUA}/feeshSetTrackerDrops <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:
  - <ITEM_ID> is a mandatory item ID - RADIOACTIVE_VIAL.
  - <COUNT> is a mandatory number of times you've dropped it.
  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!

Example:
${AQUA}/feeshSetTrackerDrops RADIOACTIVE_VIAL 2 2025-05-30 23:59:00
                """.trimIndent(), true)
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
        this.description = Translated("Shows an overlay with the overview of the treasure fishing catches, and different related statistics.")
    }

    init {
        button {
            title = "Set Treasure Dyes count"
            description = "Explains in your chat how to setup Treasure Dyes count and last drop date."
            text = "Click for help"
            onClick {
                ChatUtils.sendLocalChat("""
${WHITE}${BOLD}Treasure Dyes setup${RESET}

Do ${AQUA}/feeshSetTrackerDrops <ITEM_ID> <COUNT> [LAST_ON_DATE]${RESET} to initialize your drops history:
  - <ITEM_ID> is a mandatory item ID - DYE_TREASURE.
  - <COUNT> is a mandatory number of times you've dropped it.
  - [LAST_ON_DATE] is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!

Example:
${AQUA}/feeshSetTrackerDrops DYE_TREASURE 2 2025-05-30 23:59:00
                """.trimIndent(), true)
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
        this.description = Translated("Shows an overlay with your Archfiend Dice / High Class Archfiend Dice profits. This overlay has [Session] and [Total] view mode.")
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
Shows an overlay with your profits you gained while fishing. This overlay has [Session] and [Total] view mode.
To count items added to your sacks, make sure to enable ${YELLOW}Skyblock Settings -> Personal -> Chat Feedback -> Sack Notifications
To reset [Session]: ${AQUA}/feeshResetFishingProfit
To reset [Total]: ${AQUA}/feeshResetFishingProfitTotal
        """.trimIndent())
    }

    var fishingProfitTrackerPriceMode by enum(PricingModeWithNpc.SELL_OFFER) {
        this.name = Translated("Price mode")
        this.description = Translated("How to calculate prices for the dropped items in the Fishing profit tracker.")
    }
    
    var calculateProfitInCrimsonEssence by boolean(false) {
        this.name = Translated("Show profits in Crimson Essence when applicable")
        this.description = Translated("Calculate price in Crimson Essence for salvageable crimson fishing items e.g. Slug Boots, Moogma Leggings, Flaming Chestplate, Blade of the Volcano, Staff of the Volcano.")
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

    var resetFishingProfitTrackerOnGameClosed by boolean(false) {
        this.name = Translated("Autoreset [Session] on closing game")
        this.description = Translated("Automatically reset the fishing profit tracker [Session] when you close Minecraft.")
    }
}