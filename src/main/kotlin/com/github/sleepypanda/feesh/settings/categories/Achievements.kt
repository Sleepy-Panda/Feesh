package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.features.commands.AchievementsCommand
import com.github.sleepypanda.feesh.features.commands.PersonalBestsCommand
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Achievements : CategoryKt("Achievements and PB") {
    override val description: TranslatableValue
        get() = Literal("Track fishing achievements and personal bests while playing.")

    init {
        separator {
            this.title = "${AQUA}${BOLD}Achievements"
        }
    }

    var achievementsEnabled by boolean(true) {
        this.name = Translated("Track achievements")
        this.description = Translated("Enables achievement tracking and announcements.")
    }

    init {
        button {
            title = "View achievements"
            description = "Displays your Feesh achievements in the chat. Executes ${WHITE}/${AchievementsCommand.COMMAND_NAME}"
            text = "Click to execute"
            onClick {
                ChatUtils.command(AchievementsCommand.COMMAND_NAME)
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
