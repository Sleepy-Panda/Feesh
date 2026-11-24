package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.settings.models.AlertableSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.ModVersionConstants
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.features.alerts.RareCatchAlert
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.enums.WeatherEventTypes
import com.github.sleepypanda.feesh.utils.openUriCompat
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
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
        *AlertableSeaCreatureTypes.entries.filter { it.isAlertEnabledByDefault }.toTypedArray(), // Selected by default
    ) {
        this.name = Translated("Select sea creatures to be alerted on")
        this.searchTerms = AlertableSeaCreatureTypes.entries.map { it.displayName }.toList()
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
        this.name = Translated("Alert when you or your party members are killed by a fishing boss")
        this.description = Translated("Shows a title and plays a sound when you or your party members are killed by a fishing boss.")
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

    var alertOnRareDropTypes by select(RareDropTypes.ALL, *RareDropTypes.entries.toTypedArray()) {
        this.name = Translated("Select rare drops to be alerted on")
        this.description = Translated("ALL is equivalent of selecting all items in the list below - you can select it once, to enable all existing and future items in the list.")
        this.searchTerms = RareDropTypes.entries.map { it.displayName }.toList()
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
            this.title = "${AQUA}${BOLD}Sea creatures alert customization"
        }
    }

    private val seaCreatureCatchPlaceholders =
        "${WHITE}{playerName}${GRAY} - colored player name; ${WHITE}{scName}${GRAY} - sea creature name, ${WHITE}{scRarityColor}${GRAY} - sea creature's rarity color."

    private val seaCreatureCocoonPlaceholders =
        "${WHITE}{playerName}${GRAY} - colored player name; ${WHITE}{scName}${GRAY} - sea creature name, ${WHITE}{scRarityColor}${GRAY} - sea creature's rarity color."

    val rareCatchOwnAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE,
        placeholders = seaCreatureCatchPlaceholders,
    )) {
        this.name = Translated("Sea creature alert - Own")
        this.description = Translated("Title and subtitle for your own sea creature alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    val rareCatchOwnDoubleHookAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE_DOUBLE_HOOK,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE_DOUBLE_HOOK,
        placeholders = seaCreatureCatchPlaceholders,
    )) {
        this.name = Translated("Double hooked sea creature alert - Own")
        this.description = Translated("Title and subtitle for your own double hooked sea creature alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    val rareCatchPartyAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE,
        placeholders = seaCreatureCatchPlaceholders,
    )) {
        this.name = Translated("Sea creature alert - Party")
        this.description = Translated("Title and subtitle for a party member's sea creature alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    val rareCatchPartyDoubleHookAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE_DOUBLE_HOOK,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE_DOUBLE_HOOK,
        placeholders = seaCreatureCatchPlaceholders,
    )) {
        this.name = Translated("Double hooked sea creature alert - Party")
        this.description = Translated("Title and subtitle for a party member's double hooked sea creature alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    val rareCatchOwnCocoonAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE_COCOON,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE_COCOON,
        placeholders = seaCreatureCocoonPlaceholders,
    )) {
        this.name = Translated("Sea creature cocooned alert - Own")
        this.description = Translated("Title and subtitle for your own sea creature cocooned alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    val rareCatchPartyCocoonAlertTemplate by obj(SeaCreatureAlertTemplateSettings(
        title = RareCatchAlert.DEFAULT_TITLE_COCOON,
        subtitle = RareCatchAlert.DEFAULT_SUBTITLE_COCOON,
        placeholders = seaCreatureCocoonPlaceholders,
    )) {
        this.name = Translated("Sea creature cocooned alert - Party")
        this.description = Translated("Title and subtitle for a party member's sea creature cocooned alert.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    var rareCatchAlertDurationTicks by int(RareCatchAlert.DEFAULT_DURATION_TICKS) {
        this.name = Translated("Duration (ticks)")
        this.description = Translated("How long the sea creature title stays on screen. 20 ticks = 1 second.")
        this.range = 1..200
        this.slider = true
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_19_0)
    }

    init {
        button {
            title = "Colors & formatting guide"
            description = "For custom text templates above, please explore the guide explaining color codes and formatting codes."
            text = "Click to open"
            onClick {
                openUriCompat("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Colors%20and%20formatting%20guide.md")
            }
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Nessie"
        }
    }

    var alertOnNessieDestination by boolean(true) {
        this.name = Translated("Alert when Nessie has chosen its destination")
        this.description = Translated("Shows a title and sends a local chat message when Nessie is swimming to the Driptoad Delve or Jade Dragon cave. Tracks Nessies only if you fished recently!")
    }

    var autoShareNessieDestination by boolean(false) {
        this.name = Translated("Autoshare to party chat")
        this.description = Translated("Shares Nessie's chosen destination to PARTY chat automatically.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Puddle Jumper"
        }
    }

    var alertOnPuddleJumperTimer by boolean(false) {
        this.name = Translated("Alert on Puddle Jumper timer")
        this.description = Translated("Shows a title and sends a local chat message when your Puddle Jumper is about to arrive to its destination.\nCan be used to swap your gear before Puddle Jumper finishes its jumping and gives the loot.")
    }

    var puddleJumperTimerSeconds by int(40) {
        this.name = Translated("Puddle Jumper timer (seconds)")
        this.description = Translated("Seconds elapsed after catching a Puddle Jumper to show the alert. Ignored if the Puddle Jumper timer alert is disabled.\n Usually, Puddle Jumper randomly takes 43-55 seconds to finish its jumping and give the loot, so adjust the timer to be alerted before it reaches the destination.")
        this.range = 30..50
        this.slider = true
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Trophy"
        }
    }

    var alertOnTrophyFrogDiscovered by boolean(true) {
        this.name = Translated("Alert on new Trophy Frog discovered")
        this.description = Translated("Shows a title and plays a sound when you discover a new Trophy Frog on Lotus Atoll.")
    }

    var alertOnTrophyFishDiscovered by boolean(true) {
        this.name = Translated("Alert on new Trophy Fish discovered")
        this.description = Translated("Shows a title and plays a sound when you discover a new Trophy Fish on Crimson Isle.")
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

    var alertOnSeaCreaturesCountThreshold by boolean(false) {
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
        this.name = Translated("Sea creatures count threshold - MOONGLADE MARSH")
        this.description = Translated("Count of sea creatures nearby required to see the alert when you are in the Moonglade Marsh. It does not check if those are own or other people's sea creatures. Ignored if the sea creatures count alert is disabled.")
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
        this.name = Translated("Alert when deployable expires soon")
        this.description = Translated("Shows a title and plays a sound when own deployable is about to expire.")
    }

    var alertOnDeployableTypes by select(DeployableTypes.TOTEM_OF_CORRUPTION, *DeployableTypes.entries.toTypedArray()) {
        this.name = Translated("Select deployables to be alerted on")
        this.searchTerms = DeployableTypes.entries.map { it.displayName }.toList()
    }

    var deployableExpiresSoonSeconds by int(10) {
        this.name = Translated("Seconds before expiration [long-living deployables]")
        this.description = Translated("How many seconds should remain until deployable expiration to see the alert. This setting is applicable to long-living deployables (3 min or 5 min).")
        this.range = 1..60
        this.slider = true
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_11_0)
    }

    var shortLivingDeployableExpiresSoonSeconds by int(5) {
        this.name = Translated("Seconds before expiration [short-living deployables]")
        this.description = Translated("How many seconds should remain until deployable expiration to see the alert. This setting is applicable to short-living deployables (30s or 1 min).")
        this.range = 1..30
        this.slider = true
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_11_0)
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

    var alertOnHotspotSeen by boolean(false) {
        this.name = Translated("Alert when a Hotspot is spawned")
        this.description = Translated("Shows a title and plays a sound when a new hotspot nametag appeared, and draws a temporary waypoint for it.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_18_0)
    }

    var alertOnHotspotGone by boolean(true) {
        this.name = Translated("Alert when the hotspot is gone")
        this.description = Translated("Shows a title and plays a sound when the hotspot you recently fished in, is gone.")
    }

    var alertOnWormholeGone by boolean(true) {
        this.name = Translated("Alert when the wormhole is gone")
        this.description = Translated("Shows a title and plays a sound when the wormhole you recently fished in, is gone.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Weather"
        }
    }
 
    var alertOnWeatherStartingSoon by boolean(false) {
        this.name = Translated("Alert when weather event starts soon")
        this.description = Translated("${GRAY}Shows a title and plays a sound when a weather event starts soon. Please enable ${YELLOW}TabList settings -> General Info widget -> Show Weather")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_15_0)
    }

    var alertOnWeatherEndingSoon by boolean(false) {
        this.name = Translated("Alert when weather event ends soon")
        this.description = Translated("${GRAY}Shows a title and plays a sound when active weather event ends soon. Please enable ${YELLOW}TabList settings -> General Info widget -> Show Weather")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_14_0)
    }

    var alertOnWeatherEventTypes by select(*WeatherEventTypes.entries.toTypedArray()) {
        this.name = Translated("Select weather types to be alerted on")
        this.searchTerms = WeatherEventTypes.entries.map { it.displayName }.toList() + ModVersionConstants.VERSION_1_15_0
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Day/Night"
        }
    }
    
    var alertOnDayNight by boolean(false) {
        this.name = Translated("Alert when Day/Night started")
        this.description = Translated("Shows a title and plays a sound when Skyblock day starts (6:00am) or Skyblock night starts (7:00pm). Can be used as a reminder to swap Light/Dark bait.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_14_0)
    }

    var alertOnDayNightOnlyWhenFishing by boolean(false) {
        this.name = Translated("Alert when Day/Night started only when fishing")
        this.description = Translated("Alerts only when you are fishing. If disabled, alerts you in every location regardless of whether you are fishing or not.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_14_0)
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
            this.title = "${AQUA}${BOLD}Bait"
        }
    }
    
    var alertOnFishingBagDisabled by boolean(true) {
        this.name = Translated("Alert when Fishing Bag/Bait Sack is disabled")
        this.description = Translated("Shows a title and plays a sound when current player starts fishing with Fishing Bait Sack disabled.\n${YELLOW}After enabling the setting, please open your fishing bait sack once to initialize its state!")
    }

    var alertOnBaitChanged by boolean(true) {
        this.name = Translated("Alert when bait is changed")
        this.description = Translated("Shows a title and plays a sound when bait is changed while fishing.")
    }

    var alertOnBaitRunningOut by boolean(true) {
        this.name = Translated("Alert when bait is running out")
        this.description = Translated("Shows a title and plays a sound when almost no bait is remaining while fishing.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Other"
        }
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
    
    var alertOnOutOfAir by boolean(true) {
        this.name = Translated("Alert when out of air soon")
        this.description = Translated("Shows a title and plays a sound when you are low on air and will start drowning soon. Applicable for Moonglade Marsh and Torrhus Canyon.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_11_0)
    }

    var alertOnWormTheFishCaught by boolean(false) {
        this.name = Translated("Alert when a Worm the Fish is caught")
        this.description = Translated("Shows a title and plays a sound when a Worm the Fish is detected in the world (Dirt Rod fishing).")
    }
}

class SeaCreatureAlertTemplateSettings(
    title: String,
    subtitle: String,
    placeholders: String,
) : ObjectKt() {
    var titleTemplate by strings(title) {
        this.name = Translated("Alert title")
        this.description = Translated("${GRAY}Reset or leave empty to use default. Placeholders: $placeholders")
    }

    var subtitleTemplate by strings(subtitle) {
        this.name = Translated("Alert subtitle")
        this.description = Translated("${GRAY}Reset or leave empty to use default. Placeholders: $placeholders")
    }
}
