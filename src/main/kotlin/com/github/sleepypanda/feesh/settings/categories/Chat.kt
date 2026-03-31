package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.chat.CompactCatchMessages
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import net.minecraft.client.gui.screen.option.KeybindsScreen
import net.minecraft.util.Util

enum class HotspotChatSource(val displayName: String) {
    PARTY_CHAT("Party Chat"),
    ALL_CHAT("All Chat");

    override fun toString(): String = displayName
}

object Chat : CategoryKt("Chat") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Compact catch messages"
        }
    }

    var compactSeaCreaturesMessages by boolean(false) {
        this.name = Translated("Compact sea creature catch messages")
        this.description = Translated("Shortens double hook message and catch message that says what sea creature you caught. So, instead of 'It's a Double Hook! Woot Woot! What is this creature!?' you will see 'DOUBLE HOOK! A Yeti has spawned!' in your chat.")
    }

    var compactDoubleHookMessageTemplate by strings(CompactCatchMessages.DEFAULT_DOUBLE_HOOK_TEMPLATE) {
        this.name = Translated("Double hook message template")
        this.description = Translated("${GRAY}Custom text shown when you get a double hook. Leave empty to use default.")
    }

    var compactCatchMessageTemplate by strings(CompactCatchMessages.DEFAULT_CATCH_TEMPLATE) {
        this.name = Translated("Sea creature catch message template")
        this.description = Translated("${GRAY}Custom text for the sea creature catch message. Leave empty to use default. Placeholders: ${WHITE}{article}${GRAY} — a/an (lowercase); ${WHITE}{Article}${GRAY} — A/An (capitalized); ${WHITE}{sc}${GRAY} — sea creature name (colored by default).")
    }

    init {
        button {
            title = "Colors & formatting guide"
            description = "For using custom text templates above, please explore the guide explaining color codes and formatting codes."
            text = "Click to open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Colors%20and%20formatting%20guide.md")
            }
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Rare sea creatures"
        }
    }

    var shareRareSeaCreatures by boolean(true) {
        this.name = Translated("Share rare sea creatures to the PARTY chat")
        this.description = Translated("Sends a PARTY chat message when a rare sea creature is caught by you. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var shareRareSeaCreaturesTypes by select(
        *RareSeaCreatureTypes.values().filter { it != RareSeaCreatureTypes.BANSHEE }.toTypedArray(), // Selected by default     
    ) {
        this.name = Translated("Select sea creatures to share to the PARTY chat")
    }

    var shareRareSeaCreaturesAllChat by boolean(false) {
        this.name = Translated("Share rare sea creatures location to the ALL chat")
        this.description = Translated("Sends an ALL chat message with coordinates when a rare sea creature is caught by you. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var shareRareSeaCreaturesTypesAllChat by select(RareSeaCreatureTypesAllChat.THUNDER, *RareSeaCreatureTypesAllChat.values()) {
        this.name = Translated("Select sea creatures to share location to the ALL chat")
    }

    var messageOnPlayerDeath by boolean(true) {
        this.name = Translated("Send a party chat message when killed by a Mythic sea creature")
        this.description = Translated("Sends a message to the party chat when you are killed by Thunder / Lord Jawbus / Ragnarok / Wiki Tiki / Titanoboa / Nessie. It enables the alerts for your party members so they can wait for you or laugh at you 😈")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Rare drops"
        }
    }

    var messageOnRareDrops by boolean(true) {
        this.name = Translated("Share rare drops to the PARTY chat")
        this.description = Translated("Sends a PARTY chat message when a rare item has dropped.")
    }

    var messageOnRareDropTypes by select(RareDropTypes.LUCKY_CLOVER_CORE, *RareDropTypes.values()) {
        this.name = Translated("Select rare drops to share to the PARTY chat")
    }

    var includeDropNumberIntoDropMessage by boolean(true) {
        this.name = Translated("Include drop number")
        this.description = Translated("${GRAY}Send the drop's ordinal number for the current session in the party chat message..\n${RED}Requires Fishing Profit Tracker to be enabled! ${GRAY}Drop numbers are reset when Fishing Profit Tracker is reset.")
    }

    var includeMagicFindIntoRareDropMessage by boolean(true) {
        this.name = Translated("Include Magic Find")
        this.description = Translated("Send the drop's ✯ Magic Find value as part of the party chat message.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Hotspots"
        }
    }

    var messageOnHotspotFound by boolean(true) {
        this.name = Translated("Offer sharing the found hotspots on click")
        this.description = Translated("Shows clickable chat message that offers sharing Hotspot location and its perk to ALL chat or PARTY chat. You need to be close to the hotspot in order to trigger it.")
    }

    init {
        button {
            title = "Share hotspot button"
            description = "Set a keybind in Minecraft's Controls menu to share the nearest Hotspot to PARTY chat or ALL chat on button pressed. You need to be close to the hotspot when pressing the button."
            text = "Click to open"
            onClick {
                val mc = FeeshMod.mc
                mc.send {
                    mc.setScreen(KeybindsScreen(mc.currentScreen, mc.options))
                }
            }
        }
    }

    var autoMessageOnHotspotFound by boolean(false) {
        this.name = Translated("Autoshare the found hotspots")
        this.description = Translated("Sends a chat message with Hotspot location and its perk to the selected chat. You need to be close to the hotspot in order to trigger it.")
    }

    var autoMessageOnHotspotFoundSource by enum(HotspotChatSource.PARTY_CHAT) {
        this.name = Translated("Autoshare to")
        this.description = Translated("Source chat type to autoshare the found hotspots (if autosharing enabled).")
    }
}
