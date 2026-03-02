package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.features.commands.FearMongererShopPrices
import com.github.sleepypanda.feesh.features.commands.GearCraftPricesCommand
import com.github.sleepypanda.feesh.features.commands.PersonalBestCommand
import com.github.sleepypanda.feesh.features.commands.PetLevelUpPricesCommand
import com.github.sleepypanda.feesh.features.commands.SpiderDenRainScheduleCommand
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
            description = "Calculates the profits for leveling up the fishing pets from level 1 to level 100, and displays the results in the chat. Executes ${WHITE}/${PetLevelUpPricesCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(PetLevelUpPricesCommand.COMMAND_NAME)
            }
        }

        separator {
            this.title = "${AQUA}${BOLD}Gear craft prices"
        }

        button {
            title = "Gear craft prices"
            description = "Calculates the profits for crafting different gear pieces from fishing drops, and displays the results in the chat. Executes ${WHITE}/${GearCraftPricesCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(GearCraftPricesCommand.COMMAND_NAME)
            }
        }
    }

    var gearCraftPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Gear craft prices mode")
        this.description = Translated("Defines how to calculate price for base fishing drops which can be sold to Bazaar or used to craft gear.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Fear Mongerer shop prices"
        }

        button {
            title = "Fear Mongerer shop prices"
            description = "Calculates the profits for selling items from Fear Mongerer shop compared to selling Green/Purple candies, and displays the results in the chat. Executes ${WHITE}/${FearMongererShopPrices.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(FearMongererShopPrices.COMMAND_NAME)
            }
        }
    }

    var fearMongererShopPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Fear Mongerer shop prices mode")
        this.description = Translated("Defines how to calculate price for candies and shop items which can be sold to Bazaar.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Spider's Den rain schedule"
        }

        button {
            title = "Spider's Den rain schedule"
            description = "Displays the nearest Spider's Den Rain / Thunderstorm events in the chat. Executes ${WHITE}/${SpiderDenRainScheduleCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(SpiderDenRainScheduleCommand.COMMAND_NAME)
            }
        }

        separator {
            this.title = "${AQUA}${BOLD}Personal Best"
        }

        button {
            title = "Personal Best"
            description = "Displays your personal best records in the chat. Executes ${WHITE}/${PersonalBestCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(PersonalBestCommand.COMMAND_NAME)
            }
        }
    }
}

