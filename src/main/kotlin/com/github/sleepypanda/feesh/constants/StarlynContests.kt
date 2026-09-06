package com.github.sleepypanda.feesh.constants

data class StarlynContestBracketRewards(val couponCount: Int, val forestEssenceCount: Int)

object StarlynContests {
    val AGATHA_CONTEST_BRACKET_REWARDS_MAP = mapOf(
        "COMMON"    to StarlynContestBracketRewards(10, 10),
        "UNCOMMON"  to StarlynContestBracketRewards(15, 20),
        "RARE"      to StarlynContestBracketRewards(20, 30),
        "EPIC"      to StarlynContestBracketRewards(25, 40),
        "LEGENDARY" to StarlynContestBracketRewards(30, 50),
        "MYTHIC"    to StarlynContestBracketRewards(35, 60),
        "DIVINE"    to StarlynContestBracketRewards(40, 70),
        "SPECIAL"   to StarlynContestBracketRewards(45, 80)
    )
    
    val MIRIA_CONTEST_BRACKET_REWARDS_MAP = mapOf(
        "COMMON"    to StarlynContestBracketRewards(10, 20),
        "UNCOMMON"  to StarlynContestBracketRewards(15, 30),
        "RARE"      to StarlynContestBracketRewards(20, 40),
        "EPIC"      to StarlynContestBracketRewards(25, 50),
        "LEGENDARY" to StarlynContestBracketRewards(30, 60),
        "MYTHIC"    to StarlynContestBracketRewards(35, 70),
        "DIVINE"    to StarlynContestBracketRewards(40, 80),
        "SPECIAL"   to StarlynContestBracketRewards(45, 90)
    )
}