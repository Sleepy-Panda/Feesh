package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Alerts : CategoryKt("Alerts") {
    init {
        separator {
            this.title = "Alerts"
        }
    }

    init {
        separator {
            this.title = "Sea creatures"
        }
    }

    var alertOnRareSeaCreatures by boolean(true) {
        this.name = Translated("Alert on rare sea creatures")
        this.description = Translated("Shows a title and plays a sound when a rare sea creature is caught by you or your party members. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var alertOnSeaCreaturesTypes by select(RareSeaCreatureTypes.CARROT_KING, *RareSeaCreatureTypes.values()) {
        this.name = Translated("Select sea creatures to alert on")
    }

    var alertOnAnyReindrake by boolean(false) {
        this.name = Translated("Alert on any Reindrake spawned in lobby")
        this.description = Translated("Shows a title and plays a sound when any Reindrake spawned in the lobby, even if it was caught not by you or your party members.")
    }

    init {
        separator {
            this.title = "Spirit Mask"
        }
    }

    var alertOnSpiritMaskUsed by boolean(true) {
        this.name = Translated("Alert on Spirit Mask used")
        this.description = Translated("Shows a title and plays a sound when your Spirit Mask's Second Wind ability is activated.")
    }

    var alertOnSpiritMaskBack by boolean(false) {
        this.name = Translated("Alert on Spirit Mask is back")
        this.description = Translated("Shows a title and plays a sound when your Spirit Mask's Second Wind ability is back after it was activated.")
    }

    init {
        separator {
            this.title = "Other"
        }
    }

    var alertOnChumBucketAutoPickup by boolean(true) {
        this.name = Translated("Alert when a Chum / Chumcap bucket is automatically picked up")
        this.description = Translated("Shows a title and plays a sound when your Chum / Chumcap bucket is automatically picked up because you went too far away.")
    }

    var alertOnPetLevelUp by boolean(true) {
        this.name = Translated("Alert when a pet reached max level")
        this.description = Translated("Shows a title and plays a sound when a pet reached max level.")
    }

    var alertOnGoldenFishSpawn by boolean(true) {
        this.name = Translated("Alert when a Golden Fish has spawned")
        this.description = Translated("Shows a title and plays a sound when a Golden Fish has spawned.")
    }
}
