# TODOs

## Reminders

- Test each release in 1.21.* and 26.x

## Alpha Torrus Canyon

You caught an Ember Shard!
 GOOD CATCH! You caught Ember Shard! (a/an missing)

You caught a Stingray Shard!
 GOOD CATCH! You caught Stingray Shard!

You caught x2 Solar Shards!
 GOOD CATCH! You caught Solar Shard!

You caught x2 Water Snake Shards!
 GOOD CATCH! You caught Water Snake Shard!

 GREAT CATCH! You caught a Giant Water Bug Shard!
 OUTSTANDING CATCH! You caught Giant Water Bug Shard x7! - not counted

Other islands - counted normally:
 GOOD CATCH! You caught a Shinyfish Shard!
 GOOD CATCH! You caught a Silentdepth Shard!
 GOOD CATCH! You caught an Abyssal Lanternfish Shard!
 GOOD CATCH! You caught a Piranha Shard!
 GREAT CATCH! You caught a Salmon Shard! - goes to both inv and box


- Add Safari to non-fishing worlds?
- All chat? Death message?
- Contests to profit tracker?
  STARLYN CONTEST REWARDS CLAIMED
  - Coupons go to the sacks
[NPC] Miria: You reached the EPIC Bracket in my contest!
[NPC] Miria: You reached the LEGENDARY Bracket in my contest!
  - Common: 10 Miria's Coupon (Rare, 15K), 20 essence
  - Uncommon: 15 30
  - 20 40
  - 25 50
  - Leg: 30 60
- Tracker for Leg/Mythic SC?
- Giant Isopod acts like a Nessie, implement immunity?
offer warp springs if not in Springs
highlight is slightly off

## Alpha Lotus Atoll

https://hypixel.net/threads/may-6th-lotus-atoll-alpha-testing.6094444/
https://hypixel.net/threads/may-14th-lotus-atoll-alpha-testing-2.6097759/

WISE! You've been granted +1 Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll! - 4 perks
WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!
WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +5♔ Trophy Chance for 30m while on the Lotus Atoll!

## 0.24.5

Newly released - https://hypixel.net/threads/hypixel-skyblock-0-24-5-assorted-qol-changes.6094244/

- Added the Personal Sea Creature Cap - 10 mobs (probably except Isle, there might be 5)
  - I need to write better "own" sea creature detection to alert before personal cap actually happens.

## Latest issues & feedback

 GOOD CATCH! You caught a Flexbone!
 GOOD CATCH! You caught a Shinyfish Shard!

- Toggle for PBs, rework trackPersonalBestFishingFestival
- Max MF PB
- Xp/h
- SMILE! Polizei111 has sprinkled some joy your way! You feel a little happier. :)
- Add All option for drops list in settings
- Share Nessie with coords to all chat?
- With the release of Minecraft version 26.2 on June 16th, we'll drop support for 1.21.11 in a few weeks.
- Personal blacklist + party sharing
- Runic sea creatures - alert or highlight
- Pickups from trade menu
- Sea Creature Tracker where it says "Total" could you change that to "Total Sea Creatures"
- Some legion counting radius logic reported
- Improve link to changelog in settings, and update announcement
- Work on various events sounds to make them more unique
- 1.21 Fishing Hook armorstand flickering
- Ragnarok immunity timer
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Settings are not saved after exiting the game, probably because user closes window using X button to exit

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.
