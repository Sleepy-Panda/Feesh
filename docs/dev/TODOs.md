# TODOs

## Reminders

- Test each release in 26.x

## Fishing outpost revamp

https://hypixel.net/threads/november-18-sven-packmaster-tier-5-fishing-outpost-revamp-alpha-testing.6172091/#-%E2%96%BA-fishing-outpost-revamp

## 0.24.5

Newly released - https://hypixel.net/threads/hypixel-skyblock-0-24-5-assorted-qol-changes.6094244/

- Added the Personal Sea Creature Cap - 10 mobs (probably except Isle, there might be 5)
  - I need to write better "own" sea creature detection to alert before personal cap actually happens.

## Latest issues & feedback

- Both compacted and original loot arrives as sacks message and counts twice.
- Display bait price as craft costs (optionally - selection between sell offer, insta sell, craft cost)
- Pchat commands: since <drop>
- NPC sell prices change https://hypixel.net/threads/october-29-accessory-bag-revamp-economy-changes.6165308/
- Bait refund arrives to sack and gets counted (temporary disabled bait tracking).
- Singed Powder is forced to be tracked from RARE DROP message, being a precedent (items get hidden in Sacks message).
- Scrollable overlays
- Send a message like this "BAD JUNK CATCH! You caught a Salmon!" when catching a non-treasure while treasure/junk fishing
- For level 100 pets, would be cool to change their item name to be aligned with Level 1 pets. This also causes Lvl 100 pets being not easily found ny name via /get command or party command item search.
- Drake sound not muted when using Sound Controller mod
- integrate medal clipping for rare drops / dyes? have seen it in sbo and its pretty neat  https://medal.tv/developer/auto-clipping#api-reference
- Default party drop on-screen alert ragebaits some people :(
- Hotspot nametag hider + overlay with hotspot perk
- Hotspot radius, hotspot text laying on the water
- Custom msg in catch message which can be shown in title (e.g. I KILL OWN)
- Editing the format of feesh titles (summoned creature name, dropped rare item, price of an item, name of the player who did it etc) to change their order, color, duration, location(on title or subtitle) or to add some custom texts in it and so on.
- Mod name is copied as [ Feesh]
- Spooky features
- Toggle for PBs, rework trackPersonalBestFishingFestival
- Max MF PB
- Xp/h
- Share Nessie with coords to all chat?
- Personal blacklist + party sharing
- Runic sea creatures - alert or highlight
- Pickups from trade menu
- Sea Creature Tracker where it says "Total" could you change that to "Total Sea Creatures"
- Some legion counting radius logic reported
- Work on various events sounds to make them more unique
- Ragnarok immunity timer
- Manual "set tracker drops" command does not reset "sc since last" for that drop.
- Fished coins to add via the command.
- Autoupdates

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
