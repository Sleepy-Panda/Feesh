package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.PricingMode
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Commands : CategoryKt("Commands") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Pets level up prices"
        }

        button {
            title = "Pets level up prices"
            description = "Calculates the profits for leveling up the fishing pets from level 1 to level 100, and displays the results in the chat. Executes ${AQUA}/feeshPetLevelUpPrices"
            text = "Click to execute"
            onClick {
                ChatUtils.command("feeshPetLevelUpPrices")
            }
        }

        separator {
            this.title = "${AQUA}${BOLD}Gear craft prices"
        }

        button {
            title = "Gear craft prices"
            description = "Calculates the profits for crafting different gear pieces from fishing drops, and displays the results in the chat. Executes ${AQUA}/feeshGearCraftPrices"
            text = "Click to execute"
            onClick {
                ChatUtils.command("feeshGearCraftPrices")
            }
        }
    }

    var gearCraftPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Gear craft prices mode")
        this.description = Translated("Defines how to calculate price for base fishing drops which can be sold to Bazaar or used to craft gear.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Spider's Den rain schedule"
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

