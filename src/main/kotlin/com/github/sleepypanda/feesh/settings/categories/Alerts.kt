package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Alerts : CategoryKt("Alerts") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Rare sea creatures"
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

    var alertOnPlayerDeath by boolean(true) {
        this.name = Translated("Alert when you or your party members are killed by a Mythic sea creature")
        this.description = Translated("Shows a title and plays a sound when you or your party members are killed by Thunder / Lord Jawbus / Ragnarok / Wiki Tiki / Titanoboa.")
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

    val includePriceIntoRareDropAlert by boolean(true) {
        this.name = Translated("Show dropped item price")
        this.description = Translated("Show the price of the dropped item in the alert.")
    }

    var alertOnRareDropsPriceMode by enum(PricingModeWithNpc.SELL_OFFER) {
        this.name = Translated("Rare drop price mode")
        this.description = Translated("Defines how to calculate price for the dropped item.")
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
            this.title = "${AQUA}${BOLD}Barn fishing timer"
        }
    }

    var alertOnSeaCreaturesTimerThreshold by boolean(true) {
        this.name = Translated("Alert when sea creatures are alive for 5+ minutes")
        this.description = Translated("Shows a title and plays a sound when the sea creatures nearby are alive for 5+ minutes and will despawn soon. Disabled if you have no fishing rod in your hotbar!")
    }

    var alertOnSeaCreaturesCountThreshold by boolean(true) {
        this.name = Translated("Alert when sea creatures count hits threshold")
        this.description = Translated("Shows a title and plays a sound when amount of sea creatures nearby hits the specified threshold. Useful to detect cap when barn fishing. Disabled if you have no fishing rod in your hotbar!")
    }

    var seaCreaturesCountThreshold_Hub by int(50) {
        this.name = Translated("Sea creatures count threshold - HUB")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Hub. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_CrimsonIsle by int(20) {
        this.name = Translated("Sea creatures count threshold - CRIMSON ISLE")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Crimson Isle. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_CrystalHollows by int(20) {
        this.name = Translated("Sea creatures count threshold - CRYSTAL HOLLOWS")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Crystal Hollows. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_Galatea by int(30) {
        this.name = Translated("Sea creatures count threshold - GALATEA")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Galatea. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_Default by int(50) {
        this.name = Translated("Sea creatures count threshold - Other")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in other locations. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Deployables"
        }
    }

    var alertOnDeployableExpiresSoon by boolean(true) {
        this.name = Translated("Alert when deployable item expires soon")
        this.description = Translated("Shows a title and plays a sound when your deployable item expires in 10 seconds.")
    }

    var alertOnDeployableTypes by select(DeployableTypes.TOTEM_OF_CORRUPTION, *DeployableTypes.values()) {
        this.name = Translated("Select deployables to alert on")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Pets"
        }
    }

    var alertOnPetLevelUp by boolean(true) {
        this.name = Translated("Alert when a pet reached max level")
        this.description = Translated("Shows a title and plays a sound when a pet reached max level.")
    }

    var showPetLevelUpPrice by boolean(true) {
        this.name = Translated("Show estimated price for leveling up a pet")
        this.description = Translated("Show the estimated price for leveling up a pet in the chat.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Hotspots"
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

    var alertOnFishingBagDisabled by boolean(true) {
        this.name = Translated("Alert when Fishing Bag is disabled")
        this.description = Translated("Shows a title and plays a sound when current player starts fishing with Fishing Bag disabled.\n${YELLOW}After enabling the setting, please open your fishing bag once to initialize its state!")
    }

    var alertOnNonFishingArmor by boolean(true) {
        this.name = Translated("Alert when no fishing armor equipped")
        this.description = Translated("Shows a title and plays a sound when current player is fishing in a non-fishing armor.")
    }
    
    var alertOnLootshareMessage by boolean(true) {
        this.name = Translated("Alert when 'Lootshare!' message appears in party chat")
        this.description = Translated("Shows a title and plays a sound when 'Lootshare!' message appears in party chat.")
    }

    var alertOnChumBucketAutoPickup by boolean(true) {
        this.name = Translated("Alert when a Chum / Chumcap bucket is automatically picked up")
        this.description = Translated("Shows a title and plays a sound when your Chum / Chumcap bucket is automatically picked up because you went too far away.")
    }

    var alertOnGoldenFishSpawn by boolean(true) {
        this.name = Translated("Alert when a Golden Fish has spawned")
        this.description = Translated("Shows a title and plays a sound when a Golden Fish has spawned.")
    }

    var alertOnThunderBottleCharged by boolean(true) {
        this.name = Translated("Alert when Thunder/Storm/Hurricane Bottle is charged")
        this.description = Translated("Shows a title and plays a sound when your Thunder, Storm, or Hurricane Bottle is fully charged.")
    }
    
    var alertOnSaltExpired by boolean(true) {
        this.name = Translated("Alert when a Salt has expired")
        this.description = Translated("Shows a title and plays a sound when a Salt has expired.")
    }
    
    var alertOnWormTheFishCaught by boolean(false) {
        this.name = Translated("Alert when a Worm the Fish is caught")
        this.description = Translated("Shows a title and plays a sound when a Worm the Fish is detected in the world (Dirt Rod fishing).")
    }
}
