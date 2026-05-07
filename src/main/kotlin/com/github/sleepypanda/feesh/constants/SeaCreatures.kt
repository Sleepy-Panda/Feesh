package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object SeaCreatureNames {
    // WATER
    const val WATER_HYDRA = "Water Hydra"
    const val CARROT_KING = "Carrot King"
    const val SQUID = "Squid"
    const val NIGHT_SQUID = "Night Squid"
    const val SEA_WALKER = "Sea Walker"
    const val SEA_GUARDIAN = "Sea Guardian"
    const val SEA_WITCH = "Sea Witch"
    const val SEA_ARCHER = "Sea Archer"
    const val RIDER_OF_THE_DEEP = "Rider of the Deep"
    const val CATFISH = "Catfish"
    const val SEA_LEECH = "Sea Leech"
    const val GUARDIAN_DEFENDER = "Guardian Defender"
    const val DEEP_SEA_PROTECTOR = "Deep Sea Protector"
    const val AGARIMOO = "Agarimoo"

    // FISHING FESTIVAL
    const val GREAT_WHITE_SHARK = "Great White Shark"
    const val NURSE_SHARK = "Nurse Shark"
    const val BLUE_SHARK = "Blue Shark"
    const val TIGER_SHARK = "Tiger Shark"

    // WINTER
    const val REINDRAKE = "Reindrake"
    const val YETI = "Yeti"
    const val NUTCRACKER = "Nutcracker"
    const val FROZEN_STEVE = "Frozen Steve"
    const val FROSTY = "Frosty"
    const val GRINCH = "Grinch"

    // SPOOKY
    const val PHANTOM_FISHER = "Phantom Fisher"
    const val GRIM_REAPER = "Grim Reaper"
    const val SCARECROW = "Scarecrow"
    const val NIGHTMARE = "Nightmare"
    const val WEREWOLF = "Werewolf"

    // CRIMSON ISLE
    const val FRIED_CHICKEN = "Fried Chicken"
    const val FIREPROOF_WITCH = "Fireproof Witch"
    const val MAGMA_SLUG = "Magma Slug"
    const val MOOGMA = "Moogma"
    const val LAVA_LEECH = "Lava Leech"
    const val PYROCLASTIC_WORM = "Pyroclastic Worm"
    const val LAVA_FLAME = "Lava Flame"
    const val FIRE_EEL = "Fire Eel"
    const val TAURUS = "Taurus"
    const val FIERY_SCUTTLER = "Fiery Scuttler"
    const val THUNDER = "Thunder"
    const val LORD_JAWBUS = "Lord Jawbus"
    const val PLHLEGBLAST = "Plhlegblast"
    const val RAGNAROK = "Ragnarok"

    // OASIS
    const val OASIS_RABBIT = "Oasis Rabbit"
    const val OASIS_SHEEP = "Oasis Sheep"

    // CRYSTAL HOLLOWS
    const val ABYSSAL_MINER = "Abyssal Miner"
    const val WATER_WORM = "Water Worm"
    const val POISONED_WATER_WORM = "Poisoned Water Worm"
    const val FLAMING_WORM = "Flaming Worm"
    const val LAVA_BLAZE = "Lava Blaze"
    const val LAVA_PIGMAN = "Lava Pigman"

    // ABANDONED QUARRY
    const val SMALL_MITHRIL_GRUBBER = "Small Mithril Grubber"
    const val MEDIUM_MITHRIL_GRUBBER = "Medium Mithril Grubber"
    const val LARGE_MITHRIL_GRUBBER = "Large Mithril Grubber"
    const val BLOATED_MITHRIL_GRUBBER = "Bloated Mithril Grubber"

    // BACKWATER BAYOU
    const val FROG_MAN = "Frog Man"
    const val TRASH_GOBBLER = "Trash Gobbler"
    const val DUMPSTER_DIVER = "Dumpster Diver"
    const val BANSHEE = "Banshee"
    const val SNAPPING_TURTLE = "Snapping Turtle"
    const val BAYOU_SLUDGE = "Bayou Sludge"
    const val ALLIGATOR = "Alligator"
    const val TITANOBOA = "Titanoboa"
    const val BLUE_RINGED_OCTOPUS = "Blue Ringed Octopus"
    const val WIKI_TIKI = "Wiki Tiki"

    // GALATEA
    const val NESSIE = "Nessie"
    const val THE_LOCH_EMPEROR = "The Loch Emperor"
    const val BOGGED = "Bogged"
    const val TADGANG = "Tadgang"
    const val ENT = "Ent"
    const val WETWING = "Wetwing"
    const val STRIDERSURFER = "Stridersurfer"

    // LOTUS ATOLL
    const val ATOLL_CROAKER = "Atoll Croaker"
    const val LOTUS_GUARDIAN = "Lotus Guardian"
    const val DROWNED_CAPTAIN = "Drowned Captain"
    const val GORF = "gorF"
    const val PUDDLE_JUMPER = "Puddle Jumper"
    const val FROG_PRINCE = "Frog Prince"

    // EXTRA
    const val VANQUISHER = "Vanquisher"
}

object SeaCreatureMessages {
    // WATER
    const val WATER_HYDRA_MESSAGE = "^The Water Hydra has come to test your strength\\.$"
    const val CARROT_KING_MESSAGE = "^Is this even a fish\\? It\\'s the Carrot King\\!$"
    const val SQUID_MESSAGE = "^A Squid appeared\\.$"
    const val NIGHT_SQUID_MESSAGE = "^Pitch darkness reveals a Night Squid\\.$"
    const val SEA_WALKER_MESSAGE = "^You caught a Sea Walker\\.$"
    const val SEA_GUARDIAN_MESSAGE = "^You stumbled upon a Sea Guardian\\.$"
    const val SEA_WITCH_MESSAGE = "^It looks like you\\'ve disrupted the Sea Witch\\'s brewing session\\. Watch out, she\\'s furious\\!$"
    const val SEA_ARCHER_MESSAGE = "^You reeled in a Sea Archer\\.$"
    const val RIDER_OF_THE_DEEP_MESSAGE = "^The Rider of the Deep has emerged\\.$"
    const val CATFISH_MESSAGE = "^Huh\\? A Catfish\\!$"
    const val SEA_LEECH_MESSAGE = "^Gross\\! A Sea Leech\\!$"
    const val GUARDIAN_DEFENDER_MESSAGE = "^You\\'ve discovered a Guardian Defender of the sea\\.$"
    const val DEEP_SEA_PROTECTOR_MESSAGE = "^You have awoken the Deep Sea Protector, prepare for a battle\\!$"
    const val AGARIMOO_MESSAGE = "^Your Chumcap Bucket trembles, it\\'s an Agarimoo\\.$"

    // FISHING FESTIVAL
    const val GREAT_WHITE_SHARK_MESSAGE = "^Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood\\!$"
    const val NURSE_SHARK_MESSAGE = "^A tiny fin emerges from the water, you\\'ve caught a Nurse Shark\\.$"
    const val BLUE_SHARK_MESSAGE = "^You spot a fin as blue as the water it came from, it\\'s a Blue Shark\\.$"
    const val TIGER_SHARK_MESSAGE = "^A striped beast bounds from the depths, the wild Tiger Shark\\!$"

    // WINTER
    const val YETI_MESSAGE = "^What is this creature\\!\\?$"
    const val REINDRAKE_MESSAGE = "^A Reindrake forms from the depths\\.$"
    const val NUTCRACKER_MESSAGE = "^You found a forgotten Nutcracker laying beneath the ice\\.$"
    const val FROZEN_STEVE_MESSAGE = "^Frozen Steve fell into the pond long ago, never to resurface\\.\\.\\.until now\\!$"
    const val FROSTY_MESSAGE = "^It\\'s a snowman\\! He looks harmless\\.$"
    const val GRINCH_MESSAGE = "^The Grinch stole Jerry\\'s Gifts\\.\\.\\.get them back\\!$"

    // SPOOKY
    const val PHANTOM_FISHER_MESSAGE = "^The spirit of a long lost Phantom Fisher has come to haunt you\\.$"
    const val GRIM_REAPER_MESSAGE = "^This can\\'t be\\! The manifestation of death himself\\!$"
    const val SCARECROW_MESSAGE = "^Phew\\! It\\'s only a Scarecrow\\.$"
    const val NIGHTMARE_MESSAGE = "^You hear trotting from beneath the waves, you caught a Nightmare\\.$"
    const val WEREWOLF_MESSAGE = "^It must be a full moon, a Werewolf appears\\.$"

    // CRIMSON ISLE
    const val FRIED_CHICKEN_MESSAGE = "^Smells of burning\\. Must be a Fried Chicken\\.$"
    const val FIREPROOF_WITCH_MESSAGE = "^Trouble\\'s brewing, it\\'s a Fireproof Witch\\!$"
    const val MAGMA_SLUG_MESSAGE = "^From beneath the lava appears a Magma Slug\\.$"
    const val MOOGMA_MESSAGE = "^You hear a faint Moo from the lava\\.\\.\\. A Moogma appears\\.$"
    const val LAVA_LEECH_MESSAGE = "^A small but fearsome Lava Leech emerges\\.$"
    const val PYROCLASTIC_WORM_MESSAGE = "^You feel the heat radiating as a Pyroclastic Worm surfaces\\.$"
    const val LAVA_FLAME_MESSAGE = "^A Lava Flame flies out from beneath the lava\\.$"
    const val FIRE_EEL_MESSAGE = "^A Fire Eel slithers out from the depths\\.$"
    const val TAURUS_MESSAGE = "^Taurus and his steed emerge\\.$"
    const val FIERY_SCUTTLER_MESSAGE = "^A Fiery Scuttler inconspicuously waddles up to you, friends in tow\\.$"
    const val THUNDER_MESSAGE = "^You hear a massive rumble as Thunder emerges\\.$"
    const val LORD_JAWBUS_MESSAGE = "^You have angered a legendary creature\\.\\.\\. Lord Jawbus has arrived\\.$"
    const val PLHLEGBLAST_MESSAGE = "^WOAH\\! A Plhlegblast appeared\\.$"
    const val RAGNAROK_MESSAGE = "^The sky darkens and the air thickens\\. The end times are upon us: Ragnarok is here\\.$"
    const val VANQUISHER_MESSAGE = "^A Vanquisher is spawning nearby\\!$"

    // OASIS
    const val OASIS_RABBIT_MESSAGE = "^An Oasis Rabbit appears from the water\\.$"
    const val OASIS_SHEEP_MESSAGE = "^An Oasis Sheep appears from the water\\.$"

    // CRYSTAL HOLLOWS
    const val ABYSSAL_MINER_MESSAGE = "^An Abyssal Miner breaks out of the water\\!$"
    const val WATER_WORM_MESSAGE = "^A Water Worm surfaces\\!$"
    const val POISONED_WATER_WORM_MESSAGE = "^A Poisoned Water Worm surfaces\\!$"
    const val FLAMING_WORM_MESSAGE = "^A Flaming Worm surfaces from the depths\\!$"
    const val LAVA_BLAZE_MESSAGE = "^A Lava Blaze has surfaced from the depths\\!$"
    const val LAVA_PIGMAN_MESSAGE = "^A Lava Pigman arose from the depths\\!$"

    // ABANDONED QUARRY
    const val ANY_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a $"
    const val SMALL_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Mithril Grubber\\.$"
    const val MEDIUM_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Medium Mithril Grubber\\.$"
    const val LARGE_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Large Mithril Grubber\\.$"
    const val BLOATED_MITHRIL_GRUBBER_MESSAGE = "^A leech of the mines surfaces\\.\\.\\. you\\'ve caught a Bloated Mithril Grubber\\.$"

    // BACKWATER BAYOU
    const val FROG_MAN_MESSAGE = "^Is it a frog\\? Is it a man\\? Well, yes, sorta, IT\\'S FROG MAN\\!\\!\\!\\!\\!\\!$"
    const val TRASH_GOBBLER_MESSAGE = "^The Trash Gobbler is hungry for you\\!$"
    const val DUMPSTER_DIVER_MESSAGE = "^A Dumpster Diver has emerged from the swamp\\!$"
    const val BANSHEE_MESSAGE = "^The desolate wail of a Banshee breaks the silence\\.$"
    const val SNAPPING_TURTLE_MESSAGE = "^A Snapping Turtle is coming your way, and it\\'s ANGRY\\!$"
    const val BAYOU_SLUDGE_MESSAGE = "^A swampy mass of slime emerges, the Bayou Sludge\\!$"
    const val ALLIGATOR_MESSAGE = "^A long snout breaks the surface of the water\\. It\\'s an Alligator\\!$"
    const val TITANOBOA_MESSAGE = "^A massive Titanoboa surfaces\\. Its body stretches as far as the eye can see\\.$"
    const val BLUE_RINGED_OCTOPUS_MESSAGE = "^A garish set of tentacles arise\\. It\\'s a Blue Ringed Octopus\\!$"
    const val WIKI_TIKI_MESSAGE = "^The water bubbles and froths\\. A massive form emerges- you have disturbed the Wiki Tiki\\! You shall pay the price\\.$"

    // GALATEA
    const val NESSIE_MESSAGE = "^You\\'ve caused a disturbance in the loch\\. Could it be\\.\\.\\. Nessie\\?$"
    const val THE_LOCH_EMPEROR_MESSAGE = "^The Loch Emperor arises from the depths\\.$"
    const val TADGANG_MESSAGE = "^A gang of Liltads\\!$"
    const val ENT_MESSAGE = "^You\\'ve hooked an Ent, as ancient as the forest itself\\.$"
    const val WETWING_MESSAGE = "^Look\\! A Wetwing emerges\\!$"
    const val STRIDERSURFER_MESSAGE = "^You caught a Stridersurfer\\.$"
    const val BOGGED_MESSAGE = "^You\\'ve hooked a Bogged\\!$"

    // LOTUS ATOLL
    const val ATOLL_CROAKER_MESSAGE = "^An inquisitive Atoll Croaker takes the bait!$"
    const val LOTUS_GUARDIAN_MESSAGE = "^A Lotus Guardian emerges, ready to protect the Atoll.$"
    const val DROWNED_CAPTAIN_MESSAGE = "^A Drowned Captain takes hold of your bobber!$"
    const val GORF_MESSAGE = "^What even is that\\?! A\\.\\.\\. gorF\\?$"
    const val PUDDLE_JUMPER_MESSAGE = "^A Puddle Jumper is preparing for liftoff—cast your rod into it and hold on tight!$"
    const val FROG_PRINCE_MESSAGE = "^Bow down before the Frog Prince\\.\\.\\. or pay the hefty price!$"
}

class SeaCreatures {
    data class SeaCreatureInfo(
        val name: String,
        val rarityColorCode: String,
        val pattern: Regex,
        val isRare: Boolean,
        val canBeDoubleHooked: Boolean = true,
        val types: List<String> = emptyList(),
        val worlds: List<String> = emptyList()
    ) {
        val displayName: String get() = rarityColorCode + name
        val boldDisplayName: String get() = rarityColorCode + BOLD + name
    }

    companion object {
        const val TYPE_CRIMSON_ISLE_LAVA = "CRIMSON_ISLE_LAVA"
        const val TYPE_GALATEA_LAVA = "GALATEA_LAVA"
        const val TYPE_MAGMA_FIELDS = "MAGMA_FIELDS"

        val allSeaCreatures = listOf(
            SeaCreatureInfo(
                SeaCreatureNames.WATER_HYDRA,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.WATER_HYDRA_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.CARROT_KING,
                RARE.code,
                Regex(SeaCreatureMessages.CARROT_KING_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SQUID,
                COMMON.code,
                Regex(SeaCreatureMessages.SQUID_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.NIGHT_SQUID,
                COMMON.code,
                Regex(SeaCreatureMessages.NIGHT_SQUID_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SEA_WALKER,
                COMMON.code,
                Regex(SeaCreatureMessages.SEA_WALKER_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SEA_GUARDIAN,
                COMMON.code,
                Regex(SeaCreatureMessages.SEA_GUARDIAN_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SEA_WITCH,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.SEA_WITCH_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SEA_ARCHER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.SEA_ARCHER_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.RIDER_OF_THE_DEEP,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.RIDER_OF_THE_DEEP_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.CATFISH,
                RARE.code,
                Regex(SeaCreatureMessages.CATFISH_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SEA_LEECH,
                RARE.code,
                Regex(SeaCreatureMessages.SEA_LEECH_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.GUARDIAN_DEFENDER,
                EPIC.code,
                Regex(SeaCreatureMessages.GUARDIAN_DEFENDER_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.DEEP_SEA_PROTECTOR,
                EPIC.code,
                Regex(SeaCreatureMessages.DEEP_SEA_PROTECTOR_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.AGARIMOO,
                RARE.code,
                Regex(SeaCreatureMessages.AGARIMOO_MESSAGE),
                false,
            ),

            SeaCreatureInfo(
                SeaCreatureNames.GREAT_WHITE_SHARK,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.GREAT_WHITE_SHARK_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.NURSE_SHARK,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.NURSE_SHARK_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BLUE_SHARK,
                RARE.code,
                Regex(SeaCreatureMessages.BLUE_SHARK_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.TIGER_SHARK,
                EPIC.code,
                Regex(SeaCreatureMessages.TIGER_SHARK_MESSAGE),
                false,
            ),

            SeaCreatureInfo(
                SeaCreatureNames.REINDRAKE,
                MYTHIC.code,
                Regex(SeaCreatureMessages.REINDRAKE_MESSAGE),
                true,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.YETI,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.YETI_MESSAGE),
                true,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.NUTCRACKER,
                EPIC.code,
                Regex(SeaCreatureMessages.NUTCRACKER_MESSAGE),
                true,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FROZEN_STEVE,
                COMMON.code,
                Regex(SeaCreatureMessages.FROZEN_STEVE_MESSAGE),
                false,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FROSTY,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.FROSTY_MESSAGE),
                false,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.GRINCH,
                RARE.code,
                Regex(SeaCreatureMessages.GRINCH_MESSAGE),
                false,
                worlds = listOf(WorldUtils.JERRY_WORKSHOP),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.PHANTOM_FISHER,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.PHANTOM_FISHER_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.GRIM_REAPER,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.GRIM_REAPER_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SCARECROW,
                COMMON.code,
                Regex(SeaCreatureMessages.SCARECROW_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.NIGHTMARE,
                RARE.code,
                Regex(SeaCreatureMessages.NIGHTMARE_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.WEREWOLF,
                EPIC.code,
                Regex(SeaCreatureMessages.WEREWOLF_MESSAGE),
                false,
            ),

            SeaCreatureInfo(
                SeaCreatureNames.FRIED_CHICKEN,
                COMMON.code,
                Regex(SeaCreatureMessages.FRIED_CHICKEN_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FIREPROOF_WITCH,
                RARE.code,
                Regex(SeaCreatureMessages.FIREPROOF_WITCH_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.MAGMA_SLUG,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.MAGMA_SLUG_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.MOOGMA,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.MOOGMA_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LAVA_LEECH,
                RARE.code,
                Regex(SeaCreatureMessages.LAVA_LEECH_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.PYROCLASTIC_WORM,
                RARE.code,
                Regex(SeaCreatureMessages.PYROCLASTIC_WORM_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LAVA_FLAME,
                RARE.code,
                Regex(SeaCreatureMessages.LAVA_FLAME_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FIRE_EEL,
                RARE.code,
                Regex(SeaCreatureMessages.FIRE_EEL_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.TAURUS,
                EPIC.code,
                Regex(SeaCreatureMessages.TAURUS_MESSAGE),
                false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FIERY_SCUTTLER,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.FIERY_SCUTTLER_MESSAGE),
                true,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.THUNDER,
                MYTHIC.code,
                Regex(SeaCreatureMessages.THUNDER_MESSAGE),
                true,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LORD_JAWBUS,
                MYTHIC.code,
                Regex(SeaCreatureMessages.LORD_JAWBUS_MESSAGE),
                true,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.PLHLEGBLAST,
                MYTHIC.code,
                Regex(SeaCreatureMessages.PLHLEGBLAST_MESSAGE),
                true,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.RAGNAROK,
                MYTHIC.code,
                Regex(SeaCreatureMessages.RAGNAROK_MESSAGE),
                true,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.VANQUISHER,
                EPIC.code,
                Regex(SeaCreatureMessages.VANQUISHER_MESSAGE),
                true,
                canBeDoubleHooked = false,
                types = listOf(TYPE_CRIMSON_ISLE_LAVA),
                worlds = listOf(WorldUtils.CRIMSON_ISLE),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.OASIS_RABBIT,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.OASIS_RABBIT_MESSAGE),
                false,
                worlds = listOf(WorldUtils.FARMING_ISLANDS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.OASIS_SHEEP,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.OASIS_SHEEP_MESSAGE),
                false,
                worlds = listOf(WorldUtils.FARMING_ISLANDS),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.ABYSSAL_MINER,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.ABYSSAL_MINER_MESSAGE),
                true,
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.WATER_WORM,
                RARE.code,
                Regex(SeaCreatureMessages.WATER_WORM_MESSAGE),
                false,
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.POISONED_WATER_WORM,
                RARE.code,
                Regex(SeaCreatureMessages.POISONED_WATER_WORM_MESSAGE),
                false,
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FLAMING_WORM,
                RARE.code,
                Regex(SeaCreatureMessages.FLAMING_WORM_MESSAGE),
                false,
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LAVA_BLAZE,
                EPIC.code,
                Regex(SeaCreatureMessages.LAVA_BLAZE_MESSAGE),
                false,
                types = listOf(TYPE_MAGMA_FIELDS),
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LAVA_PIGMAN,
                EPIC.code,
                Regex(SeaCreatureMessages.LAVA_PIGMAN_MESSAGE),
                false,
                types = listOf(TYPE_MAGMA_FIELDS),
                worlds = listOf(WorldUtils.CRYSTAL_HOLLOWS),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.SMALL_MITHRIL_GRUBBER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.SMALL_MITHRIL_GRUBBER_MESSAGE),
                false,
                worlds = listOf(WorldUtils.DWARVEN_MINES),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.MEDIUM_MITHRIL_GRUBBER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.MEDIUM_MITHRIL_GRUBBER_MESSAGE),
                false,
                worlds = listOf(WorldUtils.DWARVEN_MINES),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LARGE_MITHRIL_GRUBBER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.LARGE_MITHRIL_GRUBBER_MESSAGE),
                false,
                worlds = listOf(WorldUtils.DWARVEN_MINES),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BLOATED_MITHRIL_GRUBBER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.BLOATED_MITHRIL_GRUBBER_MESSAGE),
                false,
                worlds = listOf(WorldUtils.DWARVEN_MINES),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.FROG_MAN,
                COMMON.code,
                Regex(SeaCreatureMessages.FROG_MAN_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.TRASH_GOBBLER,
                COMMON.code,
                Regex(SeaCreatureMessages.TRASH_GOBBLER_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.DUMPSTER_DIVER,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.DUMPSTER_DIVER_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BANSHEE,
                RARE.code,
                Regex(SeaCreatureMessages.BANSHEE_MESSAGE),
                false,
                worlds = listOf(WorldUtils.BACKWATER_BAYOU),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.SNAPPING_TURTLE,
                RARE.code,
                Regex(SeaCreatureMessages.SNAPPING_TURTLE_MESSAGE),
                false,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BAYOU_SLUDGE,
                RARE.code,
                Regex(SeaCreatureMessages.BAYOU_SLUDGE_MESSAGE),
                false,
                worlds = listOf(WorldUtils.BACKWATER_BAYOU),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.ALLIGATOR,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.ALLIGATOR_MESSAGE),
                true,
                worlds = listOf(WorldUtils.BACKWATER_BAYOU),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BLUE_RINGED_OCTOPUS,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.BLUE_RINGED_OCTOPUS_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.WIKI_TIKI,
                MYTHIC.code,
                Regex(SeaCreatureMessages.WIKI_TIKI_MESSAGE),
                true,
            ),
            SeaCreatureInfo(
                SeaCreatureNames.TITANOBOA,
                MYTHIC.code,
                Regex(SeaCreatureMessages.TITANOBOA_MESSAGE),
                true,
                worlds = listOf(WorldUtils.BACKWATER_BAYOU),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.NESSIE,
                MYTHIC.code,
                Regex(SeaCreatureMessages.NESSIE_MESSAGE),
                true,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.THE_LOCH_EMPEROR,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.THE_LOCH_EMPEROR_MESSAGE),
                true,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.BOGGED,
                COMMON.code,
                Regex(SeaCreatureMessages.BOGGED_MESSAGE),
                false,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.TADGANG,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.TADGANG_MESSAGE),
                false,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.ENT,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.ENT_MESSAGE),
                false,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.WETWING,
                RARE.code,
                Regex(SeaCreatureMessages.WETWING_MESSAGE),
                false,
                worlds = listOf(WorldUtils.GALATEA),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.STRIDERSURFER,
                RARE.code,
                Regex(SeaCreatureMessages.STRIDERSURFER_MESSAGE),
                false,
                types = listOf(TYPE_GALATEA_LAVA),
                worlds = listOf(WorldUtils.GALATEA),
            ),

            SeaCreatureInfo(
                SeaCreatureNames.ATOLL_CROAKER,
                COMMON.code,
                Regex(SeaCreatureMessages.ATOLL_CROAKER_MESSAGE),
                false,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.LOTUS_GUARDIAN,
                UNCOMMON.code,
                Regex(SeaCreatureMessages.LOTUS_GUARDIAN_MESSAGE),
                false,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.GORF,
                RARE.code,
                Regex(SeaCreatureMessages.GORF_MESSAGE),
                false,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.DROWNED_CAPTAIN,
                EPIC.code,
                Regex(SeaCreatureMessages.DROWNED_CAPTAIN_MESSAGE),
                false,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.PUDDLE_JUMPER,
                LEGENDARY.code,
                Regex(SeaCreatureMessages.PUDDLE_JUMPER_MESSAGE),
                true,
                canBeDoubleHooked = false,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
            SeaCreatureInfo(
                SeaCreatureNames.FROG_PRINCE,
                MYTHIC.code,
                Regex(SeaCreatureMessages.FROG_PRINCE_MESSAGE),
                true,
                worlds = listOf(WorldUtils.LOTUS_ATOLL),
            ),
        )

        @JvmStatic
        fun getTitle(seaCreatureName: String, isDoubleHook: Boolean): String {
            val info = allSeaCreatures.find { it.name == seaCreatureName } ?: return ""
            val dh = if (isDoubleHook) " ${RESET}${RED}${BOLD}X2${RESET}" else ""
            val baseTitle = "${info.boldDisplayName}${RESET}${dh}${RESET}"
            return if (info.rarityColorCode == MYTHIC.code) "${GOLD}${OBFUSCATED}x${RESET} $baseTitle ${GOLD}${OBFUSCATED}x${RESET}"
            else baseTitle
        }
    }
}