package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.features.alerts.RareCatches
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.settings.categories.Chat as ChatSettings
import net.minecraft.text.Text

object CompactCatchMessages {
    fun init() {
        if (ChatSettings.compactSeaCreaturesMessages) { // TODO make it dynamic, so far it cancels messages for other handlers
            RegisterUtils.chatCancellable(Regex("Double Hook")) { _, _ ->
                ChatUtils.send("${ColorCodes.BLUE}${FormattingCodes.BOLD}DOUBLE HOOK!${FormattingCodes.RESET}")
                false
            }
    
            SeaCreatures.allSeaCreatures.forEach { sc ->
                RegisterUtils.chatCancellable(Regex(sc.pattern)) { _, _ ->
                    if (ChatSettings.compactSeaCreaturesMessages) {
                        ChatUtils.send("${ColorCodes.WHITE}You caught ${FormattingCodes.BOLD}${sc.displayName}${ColorCodes.WHITE}!")
                        return@chatCancellable false
                    }
                    true
                }
            }    
        }
    }
}