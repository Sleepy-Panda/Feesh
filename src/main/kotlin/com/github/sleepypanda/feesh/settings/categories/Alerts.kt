package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.settings.models.AlertableSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

enum class AlertSource(val displayName: String) {
    OWN_AND_PARTY("Own and party"),
    OWN("Own");

    override fun toString(): String = displayName
}

enum class RareDropPriceScope(val displayName: String) {
    OWN("Own"),
    OWN_AND_PARTY("Own and party"),
    OFF("Off");

    override fun toString(): String = displayName
}

object Alerts : CategoryKt("Alerts") {
    override val description: TranslatableValue
        get() = Literal(
            "Personal alerts that appear on your screen, in local chat, or play a sound."
        )

    init {
        separator {
            this.title = "${AQUA}${BOLD}Sea creatures"
        }
    }

    var alertOnRareSeaCreatures by boolean(true) {
        this.name = Translated("Alert on sea creatures")
        this.description = Translated("Shows a title and plays a sound when a specific sea creature is caught by you or your party members. Sound can be customized for each creature from the list. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var alertOnSeaCreaturesList by select(
        *AlertableSeaCreatureTypes.values().filter { it.isEnabledByDefault }.toTypedArray(), // Selected by default
    ) {
        this.name = Translated("Select sea creatures to be alerted on")
        this.searchTerms = AlertableSeaCreatureTypes.values().map { it.displayName }.toList()
    }

    var alertOnSeaCreaturesIncludeCocooned by boolean(true) {
        this.name = Translated("Alert on cocooned sea creature")
        this.description = Translated("Also alerts when selected sea creatures are cocooned by you or your party members.")
    }

    var alertOnRareSeaCreaturesSource by enum(AlertSource.OWN_AND_PARTY) {
        this.name = Translated("Alert source")
        this.description = Translated("\"Own and party\" = your catches and party members' catches; \"Own\" = only your catches.")
    }

    var alertOnAnyReindrake by boolean(false) {
        this.name = Translated("Alert on any Reindrake spawned in lobby")
        this.description = Translated("Shows a title and plays a sound when any Reindrake spawned in the lobby, even if it was caught not by you or your party members.")
    }

    var alertOnPlayerDeath by boolean(true) {
        this.name = Translated("Alert when you or your party members are killed by a Mythic sea creature")
        this.description = Translated("Shows a title and plays a sound when you or your party members are killed by Thunder / Lord Jawbus / Ragnarok / Wiki Tiki / Titanoboa / Nessie.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Rare drops"
        }
    }

    var alertOnRareDrops by boolean(true) {
        this.name = Translated("Alert on rare drops")
        this.description = Translated("Shows a title and plays a sound when a rare item has dropped by you or your party members. Sound can be customized for each item from the list.")
    }

    var alertOnRareDropTypes by select(RareDropTypes.LUCKY_CLOVER_CORE, *RareDropTypes.values()) {
        this.name = Translated("Select rare drops to be alerted on")
        this.searchTerms = RareDropTypes.values().map { it.displayName }.toList()
    }

    var alertOnRareDropsSource by enum(AlertSource.OWN_AND_PARTY) {
        this.name = Translated("Alert source")
        this.description = Translated("\"Own and party\" = your drops and party members' drops; \"Own\" = only your drops.")
    }

    var rareDropAlertShowPriceFor by enum(RareDropPriceScope.OWN_AND_PARTY) {
        this.name = Translated("Show dropped item price in the title")
        this.description = Translated("Show the price of the dropped item in the alert. \"Own\" = only for your drops; \"Own and party\" = for your drops and party members' drops; \"Off\" = don't show price.")
    }

    var alertOnRareDropsPriceMode by enum(PricingModeWithNpc.SELL_OFFER) {
        this.name = Translated("Rare drop price mode")
        this.description = Translated("Defines how to calculate price for the dropped item.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Nessie"
        }
    }

    var alertOnNessieDestination by boolean(true) {
        this.name = Translated("Alert when Nessie has chosen its destination")
        this.description = Translated("Shows a title and sends a local chat message when Nessie is swimming to the Driptoad Delve or Jade Dragon cave.")
    }

    var autoShareNessieDestination by boolean(false) {
        this.name = Translated("Autoshare to party chat")
        this.description = Translated("Shares Nessie's chosen destination to PARTY chat automatically.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Trophy Frogs"
        }
    }

    var alertOnTrophyFrogDiscovered by boolean(true) {
        this.name = Translated("Alert on new Trophy Frog discovered")
        this.description = Translated("Shows a title and plays a sound when you discover a new Trophy Frog on Lotus Atoll.")
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

    var alertOnSeaCreaturesPersonalCap by boolean(true) {
        this.name = Translated("Alert after personal sea creatures count cap")
        this.description = Translated("Shows a title and plays a sound when the personal sea creatures count cap is reached, and Skyblock says \"There is not enough space for another Sea Creature!\"")
    }

    var alertOnSeaCreaturesTimerThreshold by boolean(true) {
        this.name = Translated("Alert when own/others' sea creatures are alive for 5+ minutes")
        this.description = Translated("Shows a title and plays a sound when the sea creatures nearby are alive for 5+ minutes and will despawn soon. It does not check if those are own or other people's sea creatures. Disabled if you have no fishing rod in your hotbar!")
    }

    var alertOnSeaCreaturesCountThreshold by boolean(true) {
        this.name = Translated("Alert when own/others' sea creatures count hits threshold")
        this.description = Translated("Shows a title and plays a sound when amount of sea creatures nearby hits the specified threshold. It does not check if those are own or other people's sea creatures. Disabled if you have no fishing rod in your hotbar!")
    }

    var seaCreaturesCountThreshold_Hub by int(50) {
        this.name = Translated("Sea creatures count threshold - HUB")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Hub. It does not check if those are own or other people's sea creatures.Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_CrimsonIsle by int(20) {
        this.name = Translated("Sea creatures count threshold - CRIMSON ISLE")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Crimson Isle. It does not check if those are own or other people's sea creatures. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_CrystalHollows by int(20) {
        this.name = Translated("Sea creatures count threshold - CRYSTAL HOLLOWS")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Crystal Hollows. It does not check if those are own or other people's sea creatures. Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_Galatea by int(30) {
        this.name = Translated("Sea creatures count threshold - GALATEA")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Galatea. It does not check if those are own or other people's sea creatures.Ignored if the sea creatures count alert is disabled.")
        this.range = 5..60
        this.slider = true
    }

    var seaCreaturesCountThreshold_Default by int(50) {
        this.name = Translated("Sea creatures count threshold - Other")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in other locations. It does not check if those are own or other people's sea creatures. Ignored if the sea creatures count alert is disabled.")
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
        this.name = Translated("Select deployables to be alerted on")
        this.searchTerms = DeployableTypes.values().map { it.displayName }.toList()
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Consumables"
        }
    }

    var alertOnConsumableExpiresSoon by boolean(true) {
        this.name = Translated("Alert when a Moby-Duck expires soon")
        this.description = Translated("Shows a title and plays a sound when a Moby-Duck expires in 10 seconds.")
    }
        
    var alertOnSaltExpired by boolean(true) {
        this.name = Translated("Alert when a Salt has expired")
        this.description = Translated("Shows a title and plays a sound when a Salt has expired.")
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
            this.title = "${AQUA}${BOLD}Rain, Thunder, Blizzard"
        }
    }

    var alertOnRainEndingSoon by boolean(false) {
        this.name = Translated("Alert when Rain/Thunder/Blizzard ends soon")
        this.description = Translated("${GRAY}Shows a title and plays a sound when active Rain/Thunder/Blizzard ends soon. It's applicable to The Park, Spider's Den, Lotus Atoll, Backwater Bayou, and Jerry's Workshop. Please enable ${YELLOW}TabList settings -> General Info widget -> Show Rain / Show Blizzard")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Fishing Festival"
        }
    }

    var alertOnFishingFestivalEnded by boolean(true) {
        this.name = Translated("Alert on Fishing Festival ended")
        this.description = Translated("Shows a title and sends shark counts to chat when the Fishing Festival ends. Requires the overlay or tracking to be active during the festival.")
    }

    var trackPersonalBestFishingFestival by boolean(true) {
        this.name = Translated("Track personal best")
        this.description = Translated("Track your personal best for total sharks caught, and Great White Sharks caught during the Fishing Festival.")
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
    
    var alertOnWormTheFishCaught by boolean(false) {
        this.name = Translated("Alert when a Worm the Fish is caught")
        this.description = Translated("Shows a title and plays a sound when a Worm the Fish is detected in the world (Dirt Rod fishing).")
    }
}
