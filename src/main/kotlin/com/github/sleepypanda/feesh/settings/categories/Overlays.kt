package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

enum class SeaCreaturesTrackerDisplayMode {
    ONLY_RARE,
    ALL
}

enum class SeaCreaturesTrackerSorting {
    CATCHES_COUNT_DESC,
    CATCHES_COUNT_ASC
}

enum class FishingHookTimerMode {
    UNTIL_REEL_IN,
    SINCE_CASTED
}

object Overlays : CategoryKt("Overlays") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Jerry's Workshop"
        }
    }

    var jerryWorkshopTrackerOverlay by boolean(false) {
        this.name = Translated("Jerry's Workshop tracker")
        this.description = Translated("Shows an overlay with Yeti / Reindrake catch statistics while in the Jerry Workshop.")
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
            this.title = "${AQUA}${BOLD}Sea creatures"
        }
    }

    var seaCreaturesTrackerOverlay by boolean(false) {
        this.name = Translated("Sea creatures tracker")
        this.description = Translated("Shows an overlay with the overview of the sea creatures caught, and different related statistics. This overlay has [Session] and [Total] view mode.\nDo ${AQUA}/feeshResetSeaCreatures${GRAY} to reset [Session], or ${AQUA}/feeshResetSeaCreaturesTotal${GRAY} to reset [Total].")
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

    var seaCreaturesTrackerSorting by enum(SeaCreaturesTrackerSorting.CATCHES_COUNT_DESC) {
        this.name = Translated("Sea creatures sorting")
        this.description = Translated("Setups sorting order for the sea creatures.")
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
        separator {
            this.title = "${AQUA}${BOLD}Treasure fishing tracker"
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
                com.github.sleepypanda.feesh.utils.ChatUtils.sendLocalChat("""
${AQUA}${BOLD}Treasure Dyes setup${RESET}

Do ${AQUA}/feeshSetTrackerDrops <ITEM_ID> <COUNT> <LAST_ON_DATE>${RESET} to initialize your drops history:
  - <ITEM_ID> is a mandatory item ID - DYE_TREASURE.
  - <COUNT> is a mandatory number of times you've dropped it.
  - <LAST_ON_DATE> is optional and, if provided, should be in YYYY-MM-DD hh:mm:ss format. Can not be in future!

Example: ${AQUA}/feeshSetTrackerDrops DYE_TREASURE 2 2025-05-30 23:59:00
                """.trimIndent(), true)
            }
        }
    }
}