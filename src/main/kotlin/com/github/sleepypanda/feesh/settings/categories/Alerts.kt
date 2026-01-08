package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Alerts : CategoryKt("Alerts") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures"
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
            this.title = "${AQUA}${BOLD}Rare drops"
        }
    }

    var alertOnRareDrops by boolean(true) {
        this.name = Translated("Alert on rare drops")
        this.description = Translated("Shows a title and plays a sound when a rare item has dropped by you or your party members.")
    }

    var alertOnRareDropTypes by select(RareDropTypes.LUCKY_CLOVER_CORE, *RareDropTypes.values()) {
        this.name = Translated("Select rare drops to alert on")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Spirit Mask"
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
            this.title = "${AQUA}${BOLD}Hotspot"
        }
    }

    var alertOnHotspotGone by boolean(true) {
        this.name = Translated("Alert when the hotspot is gone")
        this.description = Translated("Shows a title and plays a sound when the hotspot you recently fished in, is gone.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Other"
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

    var alertOnThunderBottleCharged by boolean(true) {
        this.name = Translated("Alert when Thunder/Storm/Hurricane Bottle is charged")
        this.description = Translated("Shows a title and plays a sound when your Thunder, Storm, or Hurricane Bottle is fully charged.")
    }

    var alertOnNonFishingArmor by boolean(true) {
        this.name = Translated("Alert when no fishing armor equipped")
        this.description = Translated("Shows a title and plays a sound when current player is fishing in a non-fishing armor.")
    }
}
