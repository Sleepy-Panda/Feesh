package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.features.commands.FearMongererShopPricesCommand
import com.github.sleepypanda.feesh.features.commands.GearCraftPricesCommand
import com.github.sleepypanda.feesh.features.commands.JunkerJoelShopPricesCommand
import com.github.sleepypanda.feesh.features.commands.PersonalBestsCommand
import com.github.sleepypanda.feesh.features.commands.PetLevelUpPricesCommand
import com.github.sleepypanda.feesh.features.commands.SpiderDenRainScheduleCommand
import com.github.sleepypanda.feesh.features.commands.TerryShopPricesCommand
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
            description = "Calculates the profits for selling items from Fear Mongerer shop compared to selling Green/Purple candies, and displays the results in the chat. Executes ${WHITE}/${FearMongererShopPricesCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(FearMongererShopPricesCommand.COMMAND_NAME)
            }
        }
    }

    var fearMongererShopPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Fear Mongerer shop prices mode")
        this.description = Translated("Defines how to calculate price for candies and shop items which can be sold to Bazaar.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Junker Joel shop prices"
        }

        button {
            title = "Junker Joel shop prices"
            description = "Calculates the profits for selling items from Junker Joel shop compared to selling Rusty Coins, Busted Belt Buckles, Old Leather Boots, and displays the results in the chat. Executes ${WHITE}/${JunkerJoelShopPricesCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(JunkerJoelShopPricesCommand.COMMAND_NAME)
            }
        }
    }

    var junkerJoelShopPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Junker Joel shop prices mode")
        this.description = Translated("Defines how to calculate price for base items and shop items (Bazaar sell offer vs insta-sell).")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Terry shop prices"
        }

        button {
            title = "Terry shop prices"
            description = "Calculates the profits for selling items from Terry shop compared to selling Hunk of Ice / Hunk of Blue Ice, and displays the results in the chat. Executes ${WHITE}/${TerryShopPricesCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(TerryShopPricesCommand.COMMAND_NAME)
            }
        }
    }

    var terryShopPricesPriceMode by enum(PricingMode.SELL_OFFER) {
        this.name = Translated("Terry shop prices mode")
        this.description = Translated("Defines how to calculate price for Hunk of Ice, Hunk of Blue Ice and Terry shop items (Bazaar sell offer vs insta-sell).")
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
            description = "Displays your personal best records in the chat. Executes ${WHITE}/${PersonalBestsCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(PersonalBestsCommand.COMMAND_NAME)
            }
        }
    }
}

