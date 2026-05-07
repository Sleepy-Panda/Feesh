package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object PuddleJumperMovementAlert {

    private val JUMPING_PATTERN = Regex("^\\[NPC\\] Puddle Jumper: Let's get ready for a ride!$")
    private val CAUGHT_PATTERN = Regex("^\\[NPC\\] Puddle Jumper: Wow! You caught me!$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPuddleJumperMovement) return

        if (JUMPING_PATTERN.matches(event.unformattedText)) {
            CommonUtils.showTitle("", "${LEGENDARY}Puddle Jumper ${YELLOW}jumps!")
            SoundUtils.playSound()
            return
        }

        if (CAUGHT_PATTERN.matches(event.unformattedText)) {
            CommonUtils.showTitle("", "${LEGENDARY}Puddle Jumper ${GREEN}caught!")
            SoundUtils.playSound()
            return
        }
    }
}