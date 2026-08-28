# TODOs

## Reminders

- Test each release in 26.x

## Torrus Canyon

- Giant Isopod acts like a Nessie, implement immunity?
- Giant Isopod highlight looks weird

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

- Drake sound not muted when using Sound Controller mod
- integrate medal clipping for rare drops / dyes? have seen it in sbo and its pretty neat  https://medal.tv/developer/auto-clipping#api-reference
- lf treasure streak (maybe good, great , outstanding streak but maybe to much)
- Flipping items via bz + supercraft + sell still gets to the tracker
- Default party drop on-screen alert ragebaits some people :(
- Hotspot nametag hider + overlay with hotspot perk
- Custom msg in catch message which can be shown in title (e.g. I KILL OWN)
- Editing the format of feesh titles (summoned creature name, dropped rare item, price of an item, name of the player who did it etc) to change their order, color, duration, location(on title or subtitle) or to add some custom texts in it and so on.
- Mod name is copied as [ Feesh]
- Issues with setting RNG meter before first dye in Treasure tracker (0 as drops count)
- Toggle for PBs, rework trackPersonalBestFishingFestival
- Max MF PB
- Xp/h
- SMILE! Polizei111 has sprinkled some joy your way! You feel a little happier. :)
- Share Nessie with coords to all chat?
- Personal blacklist + party sharing
- Runic sea creatures - alert or highlight
- Pickups from trade menu
- Sea Creature Tracker where it says "Total" could you change that to "Total Sea Creatures"
- Some legion counting radius logic reported
- Improve link to changelog in settings, and update announcement
- Work on various events sounds to make them more unique
- Ragnarok immunity timer
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Autoupdates

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.
