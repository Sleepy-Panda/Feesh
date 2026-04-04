package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

// This should be aligned with Rare Sea Creatures names using the following logic:
// Carrot King -> CARROT_KING
enum class RareSeaCreatureTypes(val displayName: String) {
    CARROT_KING("Carrot King"),
    WATER_HYDRA("Water Hydra"),
    GREAT_WHITE_SHARK("Great White Shark"),
    NUTCRACKER("Nutcracker"),
    YETI("Yeti"),
    REINDRAKE("Reindrake"),
    PHANTOM_FISHER("Phantom Fisher"),
    GRIM_REAPER("Grim Reaper"),
    FIERY_SCUTTLER("Fiery Scuttler"),
    THUNDER("Thunder"),
    LORD_JAWBUS("Lord Jawbus"),
    PLHLEGBLAST("Plhlegblast"),
    RAGNAROK("Ragnarok"),
    VANQUISHER("Vanquisher"),
    ABYSSAL_MINER("Abyssal Miner"),
    ALLIGATOR("Alligator"),
    BLUE_RINGED_OCTOPUS("Blue Ringed Octopus"),
    BANSHEE("Banshee"),
    WIKI_TIKI("Wiki Tiki"),
    TITANOBOA("Titanoboa"),
    THE_LOCH_EMPEROR("The Loch Emperor"),
    NESSIE("Nessie");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}

enum class RareSeaCreatureTypesAllChat(val displayName: String) {
    THUNDER("Thunder"),
    LORD_JAWBUS("Lord Jawbus"),
    RAGNAROK("Ragnarok"),
    VANQUISHER("Vanquisher"),
    WIKI_TIKI("Wiki Tiki"),
    TITANOBOA("Titanoboa");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}

class SeaCreatures {
    companion object {
        const val TYPE_CRIMSON_ISLE_LAVA = "CRIMSON_ISLE_LAVA"
        const val TYPE_GALATEA_LAVA = "GALATEA_LAVA"

        // WATER SEA CREATURES
        val WATER_HYDRA_MESSAGE = "^The Water Hydra has come to test your strength\\.$"
        val CARROT_KING_MESSAGE = "^Is this even a fish\\? It\\'s the Carrot King\\!$"
        val SQUID_MESSAGE = "^A Squid appeared\\.$"
        val NIGHT_SQUID_MESSAGE = "^Pitch darkness reveals a Night Squid\\.$"
        val SEA_WALKER_MESSAGE = "^You caught a Sea Walker\\.$"
        val SEA_GUARDIAN_MESSAGE = "^You stumbled upon a Sea Guardian\\.$"
        val SEA_WITCH_MESSAGE = "^It looks like you\\'ve disrupted the Sea Witch\\'s brewing session\\. Watch out, she\\'s furious\\!$"
        val SEA_ARCHER_MESSAGE = "^You reeled in a Sea Archer\\.$"
        val RIDER_OF_THE_DEEP_MESSAGE = "^The Rider of the Deep has emerged\\.$"
        val CATFISH_MESSAGE = "^Huh\\? A Catfish\\!$"
        val SEA_LEECH_MESSAGE = "^Gross\\! A Sea Leech\\!$"
        val GUARDIAN_DEFENDER_MESSAGE = "^You\\'ve discovered a Guardian Defender of the sea\\.$"
        val DEEP_SEA_PROTECTOR_MESSAGE = "^You have awoken the Deep Sea Protector, prepare for a battle\\!$"
        val AGARIMOO_MESSAGE = "^Your Chumcap Bucket trembles, it\\'s an Agarimoo\\.$"

        // FISHING FESTIVAL SEA CREATURES
        val GREAT_WHITE_SHARK_MESSAGE = "^Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood\\!$"
        val NURSE_SHARK_MESSAGE = "^A tiny fin emerges from the water, you\\'ve caught a Nurse Shark\\.$"
        val BLUE_SHARK_MESSAGE = "^You spot a fin as blue as the water it came from, it\\'s a Blue Shark\\.$"
        val TIGER_SHARK_MESSAGE = "^A striped beast bounds from the depths, the wild Tiger Shark\\!$"

        // WINTER SEA CREATURES
        val YETI_MESSAGE = "^What is this creature\\!\\?$"
        val REINDRAKE_MESSAGE = "^A Reindrake forms from the depths\\.$"
        val NUTCRACKER_MESSAGE = "^You found a forgotten Nutcracker laying beneath the ice\\.$"
        val FROZEN_STEVE_MESSAGE = "^Frozen Steve fell into the pond long ago, never to resurface\\.\\.\\.until now\\!$"
        val FROSTY_MESSAGE = "^It\\'s a snowman\\! He looks harmless\\.$"
        val GRINCH_MESSAGE = "^The Grinch stole Jerry\\'s Gifts\\.\\.\\.get them back\\!$"

        // SPOOKY SEA CREATURES
        val PHANTOM_FISHER_MESSAGE = "^The spirit of a long lost Phantom Fisher has come to haunt you\\.$"
        val GRIM_REAPER_MESSAGE = "^This can\\'t be\\! The manifestation of death himself\\!$"
        val SCARECROW_MESSAGE = "^Phew\\! It\\'s only a Scarecrow\\.$"
        val NIGHTMARE_MESSAGE = "^You hear trotting from beneath the waves, you caught a Nightmare\\.$"
        val WEREWOLF_MESSAGE = "^It must be a full moon, a Werewolf appears\\.$"

        // CRIMSON ISLE SEA CREATURES
        val FRIED_CHICKEN_MESSAGE = "^Smells of burning\\. Must be a Fried Chicken\\.$"
        val FIREPROOF_WITCH_MESSAGE = "^Trouble\\'s brewing, it\\'s a Fireproof Witch\\!$"
        val MAGMA_SLUG_MESSAGE = "^From beneath the lava appears a Magma Slug\\.$"
        val MOOGMA_MESSAGE = "^You hear a faint Moo from the lava\\.\\.\\. A Moogma appears\\.$"
        val LAVA_LEECH_MESSAGE = "^A small but fearsome Lava Leech emerges\\.$"
        val PYROCLASTIC_WORM_MESSAGE = "^You feel the heat radiating as a Pyroclastic Worm surfaces\\.$"
        val LAVA_FLAME_MESSAGE = "^A Lava Flame flies out from beneath the lava\\.$"
        val FIRE_EEL_MESSAGE = "^A Fire Eel slithers out from the depths\\.$"
        val TAURUS_MESSAGE = "^Taurus and his steed emerge\\.$"
        val FIERY_SCUTTLER_MESSAGE = "^A Fiery Scuttler inconspicuously waddles up to you, friends in tow\\.$"
        val THUNDER_MESSAGE = "^You hear a massive rumble as Thunder emerges\\.$"
        val LORD_JAWBUS_MESSAGE = "^You have angered a legendary creature\\.\\.\\. Lord Jawbus has arrived\\.$"
        val PLHLEGBLAST_MESSAGE = "^WOAH\\! A Plhlegblast appeared\\.$"
        val RAGNAROK_MESSAGE = "^The sky darkens and the air thickens\\. The end times are upon us: Ragnarok is here\\.$"
        val VANQUISHER_MESSAGE = "^A Vanquisher is spawning nearby\\!$"

        // OASIS SEA CREATURES
        val OASIS_RABBIT_MESSAGE = "^An Oasis Rabbit appears from the water\\.$"
        val OASIS_SHEEP_MESSAGE = "^An Oasis Sheep appears from the water\\.$"

        // CRYSTAL HOLLOWS SEA CREATURES
        val ABYSSAL_MINER_MESSAGE = "^An Abyssal Miner breaks out of the water\\!$"
        val WATER_WORM_MESSAGE = "^A Water Worm surfaces\\!$"
        val POISONED_WATER_WORM_MESSAGE = "^A Poisoned Water Worm surfaces\\!$"
        val FLAMING_WORM_MESSAGE = "^A Flaming Worm surfaces from the depths\\!$"
        val LAVA_BLAZE_MESSAGE = "^A Lava Blaze has surfaced from the depths\\!$"
        val LAVA_PIGMAN_MESSAGE = "^A Lava Pigman arose from the depths\\!$"

        // ABANDONED QUARRY SEA CREATURES
        val ANY_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a $"
        val SMALL_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Mithril Grubber\\.$"
        val MEDIUM_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Medium Mithril Grubber\\.$"
        val LARGE_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Large Mithril Grubber\\.$"
        val BLOATED_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Bloated Mithril Grubber\\.$"

        // BACKWATER BAYOU SEA CREATURES
        val FROG_MAN_MESSAGE = "^Is it a frog\\? Is it a man\\? Well, yes, sorta, IT\\'S FROG MAN\\!\\!\\!\\!\\!\\!$"
        val TRASH_GOBBLER_MESSAGE = "^The Trash Gobbler is hungry for you\\!$"
        val DUMPSTER_DIVER_MESSAGE = "^A Dumpster Diver has emerged from the swamp\\!$"
        val BANSHEE_MESSAGE = "^The desolate wail of a Banshee breaks the silence\\.$"
        val SNAPPING_TURTLE_MESSAGE = "^A Snapping Turtle is coming your way, and it\\'s ANGRY\\!$"
        val BAYOU_SLUDGE_MESSAGE = "^A swampy mass of slime emerges, the Bayou Sludge\\!$"
        val ALLIGATOR_MESSAGE = "^A long snout breaks the surface of the water\\. It\\'s an Alligator\\!$"
        val TITANOBOA_MESSAGE = "^A massive Titanoboa surfaces\\. Its body stretches as far as the eye can see\\.$"
        val BLUE_RINGED_OCTOPUS_MESSAGE = "^A garish set of tentacles arise\\. It\\'s a Blue Ringed Octopus\\!$"
        val WIKI_TIKI_MESSAGE = "^The water bubbles and froths\\. A massive form emerges- you have disturbed the Wiki Tiki\\! You shall pay the price\\.$"

        // GALATEA SEA CREATURES
        val NESSIE_MESSAGE = "^You\\'ve caused a disturbance in the loch\\. Could it be\\.\\.\\. Nessie\\?$"
        val THE_LOCH_EMPEROR_MESSAGE = "^The Loch Emperor arises from the depths\\.$"
        val TADGANG_MESSAGE = "^A gang of Liltads\\!$"
        val ENT_MESSAGE = "^You\\'ve hooked an Ent, as ancient as the forest itself\\.$"
        val WETWING_MESSAGE = "^Look\\! A Wetwing emerges\\!$"
        val STRIDERSURFER_MESSAGE = "^You caught a Stridersurfer\\.$"
        val BOGGED_MESSAGE = "^You\\'ve hooked a Bogged\\!$"

        data class SeaCreatureInfo(val name: String, val rarityColorCode: String, val pattern: Regex, val isRare: Boolean, val types: List<String> = emptyList()) {
            val displayName: String get() = rarityColorCode + name
            val boldDisplayName: String get() = rarityColorCode + BOLD + name
        }

        val allSeaCreatures = listOf(
            SeaCreatureInfo(RareSeaCreatureTypes.WATER_HYDRA.displayName, LEGENDARY.code, Regex(WATER_HYDRA_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.CARROT_KING.displayName, RARE.code, Regex(CARROT_KING_MESSAGE), true),
            SeaCreatureInfo("Squid", COMMON.code, Regex(SQUID_MESSAGE), false),
            SeaCreatureInfo("Night Squid", COMMON.code, Regex(NIGHT_SQUID_MESSAGE), false),
            SeaCreatureInfo("Sea Walker", COMMON.code, Regex(SEA_WALKER_MESSAGE), false),
            SeaCreatureInfo("Sea Guardian", COMMON.code, Regex(SEA_GUARDIAN_MESSAGE), false),
            SeaCreatureInfo("Sea Witch", UNCOMMON.code, Regex(SEA_WITCH_MESSAGE), false),
            SeaCreatureInfo("Sea Archer", UNCOMMON.code, Regex(SEA_ARCHER_MESSAGE), false),
            SeaCreatureInfo("Rider of the Deep", UNCOMMON.code, Regex(RIDER_OF_THE_DEEP_MESSAGE), false),
            SeaCreatureInfo("Catfish", RARE.code, Regex(CATFISH_MESSAGE), false),
            SeaCreatureInfo("Sea Leech", RARE.code, Regex(SEA_LEECH_MESSAGE), false),
            SeaCreatureInfo("Guardian Defender", EPIC.code, Regex(GUARDIAN_DEFENDER_MESSAGE), false),
            SeaCreatureInfo("Deep Sea Protector", EPIC.code, Regex(DEEP_SEA_PROTECTOR_MESSAGE), false),
            SeaCreatureInfo("Agarimoo", RARE.code, Regex(AGARIMOO_MESSAGE), false),

            SeaCreatureInfo(RareSeaCreatureTypes.GREAT_WHITE_SHARK.displayName, LEGENDARY.code, Regex(GREAT_WHITE_SHARK_MESSAGE), true),
            SeaCreatureInfo("Nurse Shark", UNCOMMON.code, Regex(NURSE_SHARK_MESSAGE), false),
            SeaCreatureInfo("Blue Shark", RARE.code, Regex(BLUE_SHARK_MESSAGE), false),
            SeaCreatureInfo("Tiger Shark", EPIC.code, Regex(TIGER_SHARK_MESSAGE), false),

            SeaCreatureInfo(RareSeaCreatureTypes.REINDRAKE.displayName, MYTHIC.code, Regex(REINDRAKE_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.YETI.displayName, LEGENDARY.code, Regex(YETI_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.NUTCRACKER.displayName, EPIC.code, Regex(NUTCRACKER_MESSAGE), true),
            SeaCreatureInfo("Frozen Steve", COMMON.code, Regex(FROZEN_STEVE_MESSAGE), false),
            SeaCreatureInfo("Frosty", UNCOMMON.code, Regex(FROSTY_MESSAGE), false),
            SeaCreatureInfo("Grinch", RARE.code, Regex(GRINCH_MESSAGE), false),

            SeaCreatureInfo(RareSeaCreatureTypes.PHANTOM_FISHER.displayName, LEGENDARY.code, Regex(PHANTOM_FISHER_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.GRIM_REAPER.displayName, LEGENDARY.code, Regex(GRIM_REAPER_MESSAGE), true),
            SeaCreatureInfo("Scarecrow", COMMON.code, Regex(SCARECROW_MESSAGE), false),
            SeaCreatureInfo("Nightmare", RARE.code, Regex(NIGHTMARE_MESSAGE), false),
            SeaCreatureInfo("Werewolf", EPIC.code, Regex(WEREWOLF_MESSAGE), false),

            SeaCreatureInfo("Fried Chicken", COMMON.code, Regex(FRIED_CHICKEN_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Fireproof Witch", RARE.code, Regex(FIREPROOF_WITCH_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Magma Slug", UNCOMMON.code, Regex(MAGMA_SLUG_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Moogma", UNCOMMON.code, Regex(MOOGMA_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Lava Leech", RARE.code, Regex(LAVA_LEECH_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Pyroclastic Worm", RARE.code, Regex(PYROCLASTIC_WORM_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Lava Flame", RARE.code, Regex(LAVA_FLAME_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Fire Eel", RARE.code, Regex(FIRE_EEL_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Taurus", EPIC.code, Regex(TAURUS_MESSAGE), false, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo("Fiery Scuttler", LEGENDARY.code, Regex(FIERY_SCUTTLER_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo(RareSeaCreatureTypes.THUNDER.displayName, MYTHIC.code, Regex(THUNDER_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo(RareSeaCreatureTypes.LORD_JAWBUS.displayName, MYTHIC.code, Regex(LORD_JAWBUS_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo(RareSeaCreatureTypes.PLHLEGBLAST.displayName, MYTHIC.code, Regex(PLHLEGBLAST_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo(RareSeaCreatureTypes.RAGNAROK.displayName, MYTHIC.code, Regex(RAGNAROK_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),
            SeaCreatureInfo(RareSeaCreatureTypes.VANQUISHER.displayName, EPIC.code, Regex(VANQUISHER_MESSAGE), true, listOf(TYPE_CRIMSON_ISLE_LAVA)),

            SeaCreatureInfo("Oasis Rabbit", UNCOMMON.code, Regex(OASIS_RABBIT_MESSAGE), false),
            SeaCreatureInfo("Oasis Sheep", UNCOMMON.code, Regex(OASIS_SHEEP_MESSAGE), false),

            SeaCreatureInfo(RareSeaCreatureTypes.ABYSSAL_MINER.displayName, LEGENDARY.code, Regex(ABYSSAL_MINER_MESSAGE), true),
            SeaCreatureInfo("Water Worm", RARE.code, Regex(WATER_WORM_MESSAGE), false),
            SeaCreatureInfo("Poisoned Water Worm", RARE.code, Regex(POISONED_WATER_WORM_MESSAGE), false),
            SeaCreatureInfo("Flaming Worm", RARE.code, Regex(FLAMING_WORM_MESSAGE), false),
            SeaCreatureInfo("Lava Blaze", EPIC.code, Regex(LAVA_BLAZE_MESSAGE), false),
            SeaCreatureInfo("Lava Pigman", EPIC.code, Regex(LAVA_PIGMAN_MESSAGE), false),

            SeaCreatureInfo("Small Mithril Grubber", UNCOMMON.code, Regex(SMALL_MITHRIL_GRUBBER_MESSAGE), false),
            SeaCreatureInfo("Medium Mithril Grubber", UNCOMMON.code, Regex(MEDIUM_MITHRIL_GRUBBER_MESSAGE), false),
            SeaCreatureInfo("Large Mithril Grubber", UNCOMMON.code, Regex(LARGE_MITHRIL_GRUBBER_MESSAGE), false),
            SeaCreatureInfo("Bloated Mithril Grubber", UNCOMMON.code, Regex(BLOATED_MITHRIL_GRUBBER_MESSAGE), false),

            SeaCreatureInfo("Frog Man", COMMON.code, Regex(FROG_MAN_MESSAGE), false),
            SeaCreatureInfo("Trash Gobbler", COMMON.code, Regex(TRASH_GOBBLER_MESSAGE), false),
            SeaCreatureInfo("Dumpster Diver", UNCOMMON.code, Regex(DUMPSTER_DIVER_MESSAGE), false),
            SeaCreatureInfo(RareSeaCreatureTypes.BANSHEE.displayName, RARE.code, Regex(BANSHEE_MESSAGE), false),
            SeaCreatureInfo("Snapping Turtle", RARE.code, Regex(SNAPPING_TURTLE_MESSAGE), false),
            SeaCreatureInfo("Bayou Sludge", RARE.code, Regex(BAYOU_SLUDGE_MESSAGE), false),
            SeaCreatureInfo(RareSeaCreatureTypes.ALLIGATOR.displayName, LEGENDARY.code, Regex(ALLIGATOR_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.BLUE_RINGED_OCTOPUS.displayName, LEGENDARY.code, Regex(BLUE_RINGED_OCTOPUS_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.WIKI_TIKI.displayName, MYTHIC.code, Regex(WIKI_TIKI_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.TITANOBOA.displayName, MYTHIC.code, Regex(TITANOBOA_MESSAGE), true),

            SeaCreatureInfo(RareSeaCreatureTypes.NESSIE.displayName, MYTHIC.code, Regex(NESSIE_MESSAGE), true),
            SeaCreatureInfo(RareSeaCreatureTypes.THE_LOCH_EMPEROR.displayName, LEGENDARY.code, Regex(THE_LOCH_EMPEROR_MESSAGE), true),
            SeaCreatureInfo("Bogged", COMMON.code, Regex(BOGGED_MESSAGE), false),
            SeaCreatureInfo("Tadgang", UNCOMMON.code, Regex(TADGANG_MESSAGE), false),
            SeaCreatureInfo("Ent", UNCOMMON.code, Regex(ENT_MESSAGE), false),
            SeaCreatureInfo("Wetwing", RARE.code, Regex(WETWING_MESSAGE), false),
            SeaCreatureInfo("Stridersurfer", RARE.code, Regex(STRIDERSURFER_MESSAGE), false, listOf(TYPE_GALATEA_LAVA)),
        )

        val rareSeaCreatures = allSeaCreatures.filter { it.isRare }

        // Some sea creatures are not rare, but we want to alert on them for some specific reasons, e.g. bestiary or ironman grind.
        val seaCreaturesWithAlert = allSeaCreatures.filter { it.isRare || it.name == RareSeaCreatureTypes.BANSHEE.displayName }

        @JvmStatic
        fun getTitle(seaCreatureName: String, isDoubleHook: Boolean): String {
            val info = allSeaCreatures.find { it.name == seaCreatureName } ?: return ""
            val dh = if (isDoubleHook) " ${RESET}${RED}${BOLD}X2${RESET}" else ""
            val baseTitle = "${info.boldDisplayName}${RESET}${dh}${RESET}"
            return if (info.rarityColorCode == MYTHIC.code) "${GOLD}${OBFUSCATED}x${RESET} ${baseTitle} ${GOLD}${OBFUSCATED}x${RESET}" 
            else "${baseTitle}"
        }
    }
}