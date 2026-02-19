# 1.2.0

Released on: ???

## Features

- Added setting to hide other players' fishing hooks.
- Added Dwarven Lanterns to the deployables tracker and alerts (disabled by default).

## Bugfixes

# 1.1.0

Released on: 2026-02-16

## Features

- Added Fishing festival tracker and related alerts
- Added Personal Best tracking for fishing festivals (total sharks, Great White Sharks caught)
- Show latest available version from Modrinth in the settings
- Added version checker which announces latest available version from Modrinth after joining the game
- Added price displaying settings for Rare Drop alert (Own / Own and party / Off)
- Added settings for Rare Catch / Rare Drop alerts to toggle alerts from different sources (Own / Own and party)
- Added setting for Legion & Bobbin' Time overlay to toggle which lines to show
- More precise % in Sea creatures tracker
- Added setting to count SC catches without double hook in Sea creatures per hour tracker
- Added setting for Banshee alert / party chat message (disabled by default) - requested for Ironman grind
- Added chat announcement for some Bayou drops when picked up by profit tracker
- Added data backups on game close - saved into MC folder/config/feesh/backup

## Bugfixes

- Added guidance and log warnings for custom sounds filename - MC rejects to play sounds when file name is not [a-z0-9-_]
- Track drops in Fishing profit tracker while being in Inventory and some other "safe" GUIs
- Do not track items taken from pet item swapping GUI in Fishing profit tracker
- Allow increase/decrease/delete max level pets in Fishing profit tracker
- Get rid of ,0 when formatting prices
- Fixed error in logs after generating sounds.json for custom sounds resource pack
- Made bigger hotspot detection radius
- Fixed (I hope) fake "hotspot is gone" alert when hotspot is still active
- Most important bugfix ever (thanks to gegerik for reporting)

## Special thanks

Big thanks to the people who helped with testing and feedback for 1.0.0-beta! Everyone who DM'd me or created issues, I really appreciate your help.

Take the fish:

    o   o
                  /^^^^^7
    '  '     ,oO))))))))Oo,
           ,'))))))))))))))), /{
      '  ,'o  ))))))))))))))))={
         >    ))))))))))))))))={
         `,   ))))))\ \)))))))={
           ',))))))))\/)))))' \{
             '*O))))))))O*'

# 1.0.0-beta

Released on: 2026-02-10

## Features

### General

- **Sound modes** — Meme (**customizable**), Normal (default MC sounds), or Off.

### Alerts

- **Rare sea creatures** — Shows a title and plays a sound when a rare sea creature is caught by you or your party members.
- **Rare drops** — Shows a title and plays a sound when a rare item drops. Supports price display and customizable drop types.
- **Any Reindrake** — Alerts when any Reindrake spawns in the lobby, even if caught by others. Offers option to warp to island spawn on click.
- **Spirit Mask** — Alerts when Spirit Mask's Second Wind is activated and when it's back.
- **Barn fishing timer** — Alerts when sea creatures are alive for 5+ minutes or when their count hits threshold (configurable per location).
- **Deployables** — Alerts when a deployable item expires in 10 seconds.
- **Pet level up** — Alerts when a pet reaches max level. Can show estimated leveling price in chat.
- **Hotspot found or gone** — Alerts when a hotspot is found or is gone.
- **Fishing Bag disabled** — Alerts when you start fishing with Fishing Bag disabled.
- **No fishing armor** — Alerts when fishing without fishing armor equipped.
- **Lootshare** — Alerts when "Lootshare!" message appears in party chat.
- **Chum bucket auto-pickup** — Alerts when Chum/Chumcap bucket is automatically picked up.
- **Golden Fish** — Alerts when a Golden Fish has spawned.
- **Thunder/Storm/Hurricane Bottle charged** — Alerts when bottle is fully charged.
- **Salt expired** — Alerts when a Salt has expired.
- **Worm the Fish** — Alerts when Worm the Fish is detected (Dirt Rod fishing).
- **Fishing Festival** — Alerts when the Fishing Festival ends, and posts amounts of sharks caught in chat. Also has option to track personal best per festival.

### Overlays

> You can change position, scale and alignment (Left, Center, Right) for each overlay in **/feeshMoveAllGuis**

- **Fishing profit tracker** - Shows how many coins you earned in total and per hour, with Session/Total modes.
- **Legion & Bobbin' Time tracker** — Shows players and fishing hooks count within 30 blocks.
- **Barn fishing timer** — Shows count of sea creatures nearby and how long they've been alive.
- **Deployables timer** — Shows remaining time of deployable items placed nearby.
- **Sea creatures HP** — Shows HP of nearby rare sea creatures in lootshare range with immunity indicator.
- **Sea creatures tracker** — Overview of caught sea creatures with Session/Total modes, percentages, and double hook stats.
- **Fishing hook timer** — Displays hook timer and fish arrival indicator. Requires Skyblock Fishing Timer enabled.
- **Sea creatures per hour tracker** — Shows sea creatures per hour and total caught per session.
- **Fishing Festival tracker** — Shows Great White, Tiger, Blue and Nurse shark counts caught during the Fishing Festival.
- **Jerry's Workshop tracker** — Yeti/Reindrake catch statistics in Jerry Workshop.
- **Water hotspots & Bayou tracker** — Titanoboa, Wiki Tiki, and Titanoboa Shed, Tiki Mask drop statistics for Backwater Bayou and Water Hotspots.
- **Crimson Isle tracker** — Fiery Scuttler, Ragnarok, Plhlegblast, Thunder, Lord Jawbus catch and Radioactive Vial drop stats.
- **Treasure fishing tracker** — Good/Great/Outstanding treasure catches and Treasure Dye drop statistics.
- **Archfiend Dice profit tracker** — Archfiend Dice / High Class Archfiend Dice profit overlay with Session/Total modes.

### Chat

- **Compact sea creature messages** — Shortens double hook and catch messages in your chat.
- **Share rare sea creatures** — Sends to PARTY chat when you catch a rare sea creature.
- **Share rare sea creatures location - ALL chat** — Sends to ALL chat with coordinates when you catch selected rare creatures.
- **Player death message** — Sends party chat when killed by Mythic sea creature (Thunder, Lord Jawbus, etc.).
- **Share rare drops** — Sends to PARTY chat when rare item drops.
- **Hotspot sharing** — Clickable message to share found hotspot location to PARTY or ALL chat. Optional autoshare.
- **Lootshare message** — Sends to PARTY chat when it's time to lootshare. Available via Keybind.

### Commands

- **/feesh** — Opens mod settings.
- **/feeshMoveAllGuis** — Move and resize all enabled overlay GUIs.
- **/feeshPauseAllTrackers** — Pause all active trackers which have a timer (also available as Keybind).
- **/feeshPersonalBest** — Displays all your personal best records tracked by the mod.
- **/feeshPetLevelUpPrices** — Calculates profits for leveling fishing pets from 1 to 100.
- **/feeshGearCraftPrices** — Calculates profits for crafting gear from fishing drops.
- **/feeshSpiderDenRainSchedule** — Displays nearest Spider's Den Rain/Thunderstorm events.
- **/feeshSetTrackerDrops** — Initialize drop history (Titanoboa Shed, Tiki Mask, Radioactive Vial, Treasure Dye).