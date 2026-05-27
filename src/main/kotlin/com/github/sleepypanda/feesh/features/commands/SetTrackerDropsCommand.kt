package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.LotusAtollTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object SetTrackerDropsCommand {
    const val COMMAND_NAME = "feeshSetTrackerDrops"
    
    fun init() {
        RegisterUtils.command(COMMAND_NAME) { args ->
            if (args.size < 2) {
                ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} <dropId> <count> [lastOn]", true)
                return@command
            }
            
            val dropId = args[0]
            if (dropId.isNullOrEmpty()) {
                ChatUtils.sendLocalChat("${RED}Please specify correct drop ID.", true)
                return@command
            }

            val countStr = args[1]
            val count = countStr.toIntOrNull()
            if (count == null || count <= 0) {
                ChatUtils.sendLocalChat("${RED}Please specify correct count. Must be a positive number.", true)
                return@command
            }

            val lastOnStr = if (args.size >= 3) args.drop(2).joinToString(" ") else null
            var lastOn: Date? = null
            if (!lastOnStr.isNullOrEmpty()) {
                lastOn = parseDate(lastOnStr)
                if (lastOn == null) {
                    ChatUtils.sendLocalChat("${RED}Please specify correct Last On date in format YYYY-MM-DD hh:mm:ss, e.g. 2024-03-18 14:05:00. Can not be a future date!", true)
                    return@command
                }
            }
            
            when (dropId) {
                "TITANOBOA_SHED" -> BayouTracker.setTitanoboaSheds(count, lastOn)
                "TIKI_MASK" -> WaterHotspotsTracker.setTikiMasks(count, lastOn)
                "RADIOACTIVE_VIAL" -> CrimsonIsleTracker.setRadioactiveVials(count, lastOn)
                "PRINCE_CROWN_JEWEL" -> LotusAtollTracker.setPrincesCrownJewels(count, lastOn)
                "DYE_TREASURE" -> TreasureFishingTracker.setTreasureDyes(count, lastOn)
                else -> ChatUtils.sendLocalChat("${RED}Unknown drop ID: $dropId.", true)
            }
        }
    }

    private fun parseDate(date: String?): Date? {
        if (date == null || date.isBlank()) return null

        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val parsedDate = formatter.parse(date)

            val now = Date()
            if (parsedDate > now) return null
            return parsedDate
        } catch (e: Exception) {
            null
        }
    }
}
