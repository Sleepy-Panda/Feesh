package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Commands : CategoryKt("Commands") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Commands"
        }

        button {
            title = "Pets level up profits"
            description = "Calculates the profits for leveling up the fishing pets from level 1 to level 100, and displays the results in the chat. Executes ${AQUA}/feeshPetLevelUpPrices"
            text = "Click to execute"
            onClick {
                ChatUtils.command("feeshPetLevelUpPrices")
            }
        }

        button {
            title = "Gear craft prices"
            description = "Calculates the profits for crafting different gear pieces from fishing drops, and displays the results in the chat. Executes ${AQUA}/feeshGearCraftPrices"
            text = "Click to execute"
            onClick {
                ChatUtils.command("feeshGearCraftPrices")
            }
        }

        button {
            title = "Spider's Den rain schedule"
            description = "Displays the nearest Spider's Den Rain / Thunderstorm events in the chat. Executes ${AQUA}/feeshSpiderDenRainSchedule"
            text = "Click to execute"
            onClick {
                ChatUtils.command("feeshSpiderDenRainSchedule")
            }
        }
    }
}

