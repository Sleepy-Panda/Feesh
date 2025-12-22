package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.features.alerts.RareCatches
import com.github.sleepypanda.feesh.utils.Common
import com.github.sleepypanda.feesh.utils.Register
import com.github.sleepypanda.feesh.utils.Player
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.utils.Chat
import net.minecraft.text.Text

object CompactCatchMessages {
    private data class SeaCreatureAlert(val name: String, val displayName: String, val pattern: String)

    private val alerts = listOf(
            SeaCreatureAlert("Water Hydra", "${ColorCodes.LEGENDARY.code}Water Hydra", SeaCreatures.WATER_HYDRA_MESSAGE),
            SeaCreatureAlert("The Loch Emperor", "${ColorCodes.LEGENDARY.code}The Loch Emperor", SeaCreatures.THE_LOCH_EMPEROR_MESSAGE),
            SeaCreatureAlert("Carrot King", "${ColorCodes.RARE.code}Carrot King", SeaCreatures.CARROT_KING_MESSAGE),
            SeaCreatureAlert("Squid", "${ColorCodes.COMMON.code}Squid", SeaCreatures.SQUID_MESSAGE),
            SeaCreatureAlert("Night Squid", "${ColorCodes.COMMON.code}Night Squid", SeaCreatures.NIGHT_SQUID_MESSAGE),
            SeaCreatureAlert("Sea Walker", "${ColorCodes.COMMON.code}Sea Walker", SeaCreatures.SEA_WALKER_MESSAGE),
            SeaCreatureAlert("Sea Guardian", "${ColorCodes.COMMON.code}Sea Guardian", SeaCreatures.SEA_GUARDIAN_MESSAGE),
            SeaCreatureAlert("Sea Witch", "${ColorCodes.UNCOMMON.code}Sea Witch", SeaCreatures.SEA_WITCH_MESSAGE),
            SeaCreatureAlert("Sea Archer", "${ColorCodes.UNCOMMON.code}Sea Archer", SeaCreatures.SEA_ARCHER_MESSAGE),
            SeaCreatureAlert("Rider of the Deep", "${ColorCodes.UNCOMMON.code}Rider of the Deep", SeaCreatures.RIDER_OF_THE_DEEP_MESSAGE),
            SeaCreatureAlert("Catfish", "${ColorCodes.RARE.code}Catfish", SeaCreatures.CATFISH_MESSAGE),
            SeaCreatureAlert("Sea Leech", "${ColorCodes.RARE.code}Sea Leech", SeaCreatures.SEA_LEECH_MESSAGE),
            SeaCreatureAlert("Guardian Defender", "${ColorCodes.EPIC.code}Guardian Defender", SeaCreatures.GUARDIAN_DEFENDER_MESSAGE),
            SeaCreatureAlert("Deep Sea Protector", "${ColorCodes.EPIC.code}Deep Sea Protector", SeaCreatures.DEEP_SEA_PROTECTOR_MESSAGE),
            SeaCreatureAlert("Agarimoo", "${ColorCodes.RARE.code}Agarimoo", SeaCreatures.AGARIMOO_MESSAGE),

            SeaCreatureAlert("Great White Shark", "${ColorCodes.LEGENDARY.code}Great White Shark", SeaCreatures.GREAT_WHITE_SHARK_MESSAGE),
            SeaCreatureAlert("Nurse Shark", "${ColorCodes.UNCOMMON.code}Nurse Shark", SeaCreatures.NURSE_SHARK_MESSAGE),
            SeaCreatureAlert("Blue Shark", "${ColorCodes.RARE.code}Blue Shark", SeaCreatures.BLUE_SHARK_MESSAGE),
            SeaCreatureAlert("Tiger Shark", "${ColorCodes.EPIC.code}Tiger Shark", SeaCreatures.TIGER_SHARK_MESSAGE),

            SeaCreatureAlert("Yeti", "${ColorCodes.LEGENDARY.code}Yeti", SeaCreatures.YETI_MESSAGE),
            SeaCreatureAlert("Reindrake", "${ColorCodes.MYTHIC.code}Reindrake", SeaCreatures.REINDRAKE_MESSAGE),
            SeaCreatureAlert("Nutcracker", "${ColorCodes.RARE.code}Nutcracker", SeaCreatures.NUTCRACKER_MESSAGE),
            SeaCreatureAlert("Frozen Steve", "${ColorCodes.COMMON.code}Frozen Steve", SeaCreatures.FROZEN_STEVE_MESSAGE),
            SeaCreatureAlert("Frosty", "${ColorCodes.COMMON.code}Frosty", SeaCreatures.FROSTY_MESSAGE),
            SeaCreatureAlert("Grinch", "${ColorCodes.UNCOMMON.code}Grinch", SeaCreatures.GRINCH_MESSAGE),

            SeaCreatureAlert("Phantom Fisher", "${ColorCodes.LEGENDARY.code}Phantom Fisher", SeaCreatures.PHANTOM_FISHER_MESSAGE),
            SeaCreatureAlert("Grim Reaper", "${ColorCodes.LEGENDARY.code}Grim Reaper", SeaCreatures.GRIM_REAPER_MESSAGE),
            SeaCreatureAlert("Scarecrow", "${ColorCodes.COMMON.code}Scarecrow", SeaCreatures.SCARECROW_MESSAGE),
            SeaCreatureAlert("Nightmare", "${ColorCodes.RARE.code}Nightmare", SeaCreatures.NIGHTMARE_MESSAGE),
            SeaCreatureAlert("Werewolf", "${ColorCodes.EPIC.code}Werewolf", SeaCreatures.WEREWOLF_MESSAGE),

            SeaCreatureAlert("Fried Chicken", "${ColorCodes.COMMON.code}Fried Chicken", SeaCreatures.FRIED_CHICKEN_MESSAGE),
            SeaCreatureAlert("Fireproof Witch", "${ColorCodes.RARE.code}Fireproof Witch", SeaCreatures.FIREPROOF_WITCH_MESSAGE),
            SeaCreatureAlert("Magma Slug", "${ColorCodes.UNCOMMON.code}Magma Slug", SeaCreatures.MAGMA_SLUG_MESSAGE),
            SeaCreatureAlert("Moogma", "${ColorCodes.UNCOMMON.code}Moogma", SeaCreatures.MOOGMA_MESSAGE),
            SeaCreatureAlert("Lava Leech", "${ColorCodes.RARE.code}Lava Leech", SeaCreatures.LAVA_LEECH_MESSAGE),
            SeaCreatureAlert("Pyroclastic Worm", "${ColorCodes.RARE.code}Pyroclastic Worm", SeaCreatures.PYROCLASTIC_WORM_MESSAGE),
            SeaCreatureAlert("Lava Flame", "${ColorCodes.RARE.code}Lava Flame", SeaCreatures.LAVA_FLAME_MESSAGE),
            SeaCreatureAlert("Fire Eel", "${ColorCodes.RARE.code}Fire Eel", SeaCreatures.FIRE_EEL_MESSAGE),
            SeaCreatureAlert("Taurus", "${ColorCodes.EPIC.code}Taurus", SeaCreatures.TAURUS_MESSAGE),
            SeaCreatureAlert("Fiery Scuttler", "${ColorCodes.LEGENDARY.code}Fiery Scuttler", SeaCreatures.FIERY_SCUTTLER_MESSAGE),
            SeaCreatureAlert("Thunder", "${ColorCodes.MYTHIC.code}Thunder", SeaCreatures.THUNDER_MESSAGE),
            SeaCreatureAlert("Lord Jawbus", "${ColorCodes.MYTHIC.code}Lord Jawbus", SeaCreatures.LORD_JAWBUS_MESSAGE),
            SeaCreatureAlert("Plhlegblast", "${ColorCodes.MYTHIC.code}Plhlegblast", SeaCreatures.PLHLEGBLAST_MESSAGE),
            SeaCreatureAlert("Ragnarok", "${ColorCodes.MYTHIC.code}Ragnarok", SeaCreatures.RAGNAROK_MESSAGE),

            SeaCreatureAlert("Oasis Rabbit", "${ColorCodes.UNCOMMON.code}Oasis Rabbit", SeaCreatures.OASIS_RABBIT_MESSAGE),
            SeaCreatureAlert("Oasis Sheep", "${ColorCodes.UNCOMMON.code}Oasis Sheep", SeaCreatures.OASIS_SHEEP_MESSAGE),

            SeaCreatureAlert("Abyssal Miner", "${ColorCodes.LEGENDARY.code}Abyssal Miner", SeaCreatures.ABYSSAL_MINER_MESSAGE),
            SeaCreatureAlert("Water Worm", "${ColorCodes.RARE.code}Water Worm", SeaCreatures.WATER_WORM_MESSAGE),
            SeaCreatureAlert("Poisoned Water Worm", "${ColorCodes.RARE.code}Poisoned Water Worm", SeaCreatures.POISONED_WATER_WORM_MESSAGE),
            SeaCreatureAlert("Flaming Worm", "${ColorCodes.RARE.code}Flaming Worm", SeaCreatures.FLAMING_WORM_MESSAGE),
            SeaCreatureAlert("Lava Blaze", "${ColorCodes.EPIC.code}Lava Blaze", SeaCreatures.LAVA_BLAZE_MESSAGE),
            SeaCreatureAlert("Lava Pigman", "${ColorCodes.EPIC.code}Lava Pigman", SeaCreatures.LAVA_PIGMAN_MESSAGE),

            SeaCreatureAlert("Small Mithril Grubber", "${ColorCodes.UNCOMMON.code}Small Mithril Grubber", SeaCreatures.SMALL_MITHRIL_GRUBBER_MESSAGE),
            SeaCreatureAlert("Medium Mithril Grubber", "${ColorCodes.UNCOMMON.code}Medium Mithril Grubber", SeaCreatures.MEDIUM_MITHRIL_GRUBBER_MESSAGE),
            SeaCreatureAlert("Large Mithril Grubber", "${ColorCodes.UNCOMMON.code}Large Mithril Grubber", SeaCreatures.LARGE_MITHRIL_GRUBBER_MESSAGE),
            SeaCreatureAlert("Bloated Mithril Grubber", "${ColorCodes.UNCOMMON.code}Bloated Mithril Grubber", SeaCreatures.BLOATED_MITHRIL_GRUBBER_MESSAGE),
            SeaCreatureAlert("Mithril Grubber", "${ColorCodes.UNCOMMON.code}Mithril Grubber", SeaCreatures.ANY_MITHRIL_GRUBBER_MESSAGE),

            SeaCreatureAlert("Frog Man", "${ColorCodes.COMMON.code}Frog Man", SeaCreatures.FROG_MAN_MESSAGE),
            SeaCreatureAlert("Trash Gobbler", "${ColorCodes.COMMON.code}Trash Gobbler", SeaCreatures.TRASH_GOBBLER_MESSAGE),
            SeaCreatureAlert("Dumpster Diver", "${ColorCodes.UNCOMMON.code}Dumpster Diver", SeaCreatures.DUMPSTER_DIVER_MESSAGE),
            SeaCreatureAlert("Banshee", "${ColorCodes.RARE.code}Banshee", SeaCreatures.BANSHEE_MESSAGE),
            SeaCreatureAlert("Snapping Turtle", "${ColorCodes.RARE.code}Snapping Turtle", SeaCreatures.SNAPPING_TURTLE_MESSAGE),
            SeaCreatureAlert("Bayou Sludge", "${ColorCodes.RARE.code}Bayou Sludge", SeaCreatures.BAYOU_SLUDGE_MESSAGE),
            SeaCreatureAlert("Alligator", "${ColorCodes.LEGENDARY.code}Alligator", SeaCreatures.ALLIGATOR_MESSAGE),
            SeaCreatureAlert("Blue Ringed Octopus", "${ColorCodes.LEGENDARY.code}Blue Ringed Octopus", SeaCreatures.BLUE_RINGED_OCTOPUS_MESSAGE),
            SeaCreatureAlert("Wiki Tiki", "${ColorCodes.MYTHIC.code}Wiki Tiki", SeaCreatures.WIKI_TIKI_MESSAGE),
            SeaCreatureAlert("Titanoboa", "${ColorCodes.MYTHIC.code}Titanoboa", SeaCreatures.TITANOBOA_MESSAGE),

            SeaCreatureAlert("Bogged", "${ColorCodes.COMMON.code}Bogged", SeaCreatures.BOGGED_MESSAGE),
            SeaCreatureAlert("Tadgang", "${ColorCodes.UNCOMMON.code}Tadgang", SeaCreatures.TADGANG_MESSAGE),
            SeaCreatureAlert("Ent", "${ColorCodes.UNCOMMON.code}Ent", SeaCreatures.ENT_MESSAGE),
            SeaCreatureAlert("Wetwing", "${ColorCodes.RARE.code}Wetwing", SeaCreatures.WETWING_MESSAGE),
            SeaCreatureAlert("Stridersurfer", "${ColorCodes.RARE.code}Stridersurfer", SeaCreatures.STRIDERSURFER_MESSAGE),
        )

    fun init() {
        Register.chatCancellable(Regex("Double Hook")) { _, _ ->
            Chat.send("${ColorCodes.DARK_BLUE.code}${FormattingCodes.BOLD.code}DOUBLE HOOK!${FormattingCodes.RESET.code}")
            true
        }

        alerts.forEach { alert ->
            Register.chatCancellable(Regex(alert.pattern)) { _, _ ->
                Chat.send("${ColorCodes.WHITE.code}You caught ${alert.displayName}${ColorCodes.WHITE.code}!")
                true
            }
        }
    }
}