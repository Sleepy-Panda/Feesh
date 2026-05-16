# 1.7.0

Released on: ???

## Features

- Enabled existing features for Lotus Atoll.
- Added new Lotus Atoll & Hotspot creatures to all existing functionalities.
- Added new drops to Fishing profit tracker.
- Enabled Rain timer/alert functionality for Bayou.
- Enabled hotspots functionality for Park.
- Added settings for alert & pchat message when a new Trophy Frog discovered.
- Added selection of Auction prices API source - you can choose between LBIN or 7-days average LBIN.
- Extended list of sea creatures you can select for alert/pchat message/highlight/HP tracker:
  - Basically now you can toggle those functionalities for any sea creature.
  - Only "rare" creatures are selected by default.
- Added alert/pchat message when you cocoon a specific sea creature.
- Added tooltips with various extra info when you hover over a line in Sea creature tracker (while in Inventory).
  - So even if you hide % or DH info in the tracker, you still can see it on hover.
- Added total DHC count and % to Sea creatures tracker (if "Show double hook statistics" is enabled).
- Added personal best tracking for double hook streak.
- Added alert after personal SC cap is reached (based on chat message).
- Changed alert on Nessie destination to appear only if you were fishing recently.
  - So basically only fishers are alerted, other players don't get notified even if they're close.
- Moved all "Last on" datetimes from the overlays into tooltip appearing on line hover (while in Inventory).
- Improved editing data in the Fishing profit tracker, for easier data migration:
  - Added commands to set count / +count / -count for items.
  - Added commands to manage elapsed time.
  - Added detailed [user guidance](https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Editing%20profit%20tracker.md).

## Bugfixes

- Fixed some baits being added to Fishing profit tracker after adding Bait Bag preview in slot #9.
- Fixed Magma Lord Necklace crafting recipe for /feeshGearCraftPrices.

# 1.6.1

Released on: 2026-05-02

## Bugfixes

- Fixed auction items prices not being loaded.
  - Migrated from Moulberry API to Elite Skyblock API for lowest BIN - https://api.eliteskyblock.com/resources/auctions/neu

# 1.6.0

Released on: 2026-05-01

## Features

- Added support for Minecraft 26.1.x (Fabric).
  - **This is experimental version, not fully tested on 26.1.1 / 26.1.2. Please let me know if you face any issues!**
- Added Night Squid to the "Highlight rare sea creatures" functionality, to easier hunt them.
- Added True Ice rare drop alert & party chat message. **Make sure to enable if you need it!**
- Added Ice Essence gains from actionbar to Fishing profit tracker.
  - This is not 100% accurate because not all total essence gains are displayed in actionbar.
- Added setting to autoshare Nessie's destination to party chat [disabled by default].
- Added Golden Bait and Treasure bait to the Fishing profit tracker, because of Seal pet's perk.
- Added Consumables tracker which shows consumed Moby-Duck's remaining time [disabled by default]. Also has expiration alert.
- Added "Mute Reindrake gifts" setting to mute loud 'totem used' sounds while picking up gifts from a Reindrake [disabled by default].
- Shortened coordinates chat message for Hotspots and Mythic creatures in Bayou, as there is only one zone on the island and no need to mention it.

## Bugfixes

- Fixed rare sea creatures not being highlighted sometimes.
- Fixed "Hotspot is gone" appearing sometimes when leaving the world after hotspot fishing.
- Fixed Fishing profit tracker not tracking sack drops when SkyHanni Sack hider is enabled.
- Fixed rare drop-based functionalities not working when other mod changes rare drop message format.
- Fixed Legendary Squid catch alert not working when SH pet rarity message modification enabled.

## Other

- Big rework related with adding 26.1 support - moved to official mappings instead of yarn. It means that every functionality was touched in order to migrate it. Please let me know if something is broken but was working before!
- Refactored how mod subscribes to various chat messages.

# 1.5.0

Released on: 2026-04-16

## Features

- Added Galatea water tracker which shows The Loch Emperor/Nessie catch statistics in Galatea [disabled by default].
- Show Nessie's immunity flag in Sea creature HP overlay, while Nessie is running away.
- Added Nessie destination alert which announces when a Nessie decides into which cave to swim.
- Added setting to mute Jade Dragon [disabled by default].
- Added Flash I drop alert and party chat message. **Make sure to enable if you need it!**
- Show player name who summoned a Reindrake, in the "Alert on ANY Reindrake" functionality.
- Added setting to limit the amount of lines shown in Sea creatures tracker, to avoid tracker being too long.
- Added +/-/x buttons when hovering a line in Sea creatures tracker (while in inventory), to add/substract/delete sea creatures from the view.
- Added Magma Core fishing tracker which shows Lava Pigman/Lava Blaze catch stats & Magma Core drop profits, while in Crystal Hollows [disabled by default].
- Added /feeshTerryShopPrices command which shows profits for selling Terry shop items compared with selling Hunk of Ice / Hunk of Blue Ice as is.

## Bugfixes

- Track Agatha Contest results even if Fishing profit tracker is paused, while it's visible.
- Fixed /feeshPlayTestSound not playing when sound mode is Off.
- Fixed own death sound not played for alert on killed by a Mythic sea creature.
- Fixed too wide background for overlays with inline buttons.
- Fixed deployable expiration alert playing twice sometimes.
- Fixed items counting in Fishing profit tracker after /gfs.
- Fixed deployables overlay not appearing when using Bubblegum on pet.

## Other

- Made Normal sound mode default for new mod users.
- Show selected price mode in the Fishing profit tracker towards total profit.
- Made sea creature names and drop names searchable in settings, to easier identify selectlists where they can be toggled.
- Refactored way of detecting new entities spawned in the world.
- Refactored data saving logic to debounce file writes. This accumulates data changes and saves them once over time, to avoid writing to the file too often.
- Refactored way how keybinds are registered. Hopefully this will fix periodic keybind reset issues.

## Special thanks

To the [Casters discord](https://discord.gg/79E7Rhv8) and personally `jessbre` for supporting the mod!

Take the fish:

```
 _________         .    .
(..       \_    ,  |\  /|
 \       O  \  /|  \ \/ /
  \______    \/ |   \  / 
     vvvv\    \ |   /  |
     \^^^^  ==   \_/   |
      `\_   ===    \.  |
      / /\_   \ /      |
      |/   \_  \|      /
            \________/
```

# 1.4.0

Released on: 2026-03-31

## Features

- Added features for Jerry Island QoL SB update:
  - Added Nessie catch alert and chat message. **Make sure to enable if you need it!**
  - Added Vibrant Coral drop alert and chat message. **Make sure to enable if you need it!**
  - Added Nessie to various existing functionalities for rare sea creatures.
  - Enabled hotspots functionalities on Jerry's Workshop.
  - Added Blizzard timer and alert for Jerry's Workshop (added to existing Rain/Thunder functionality).
  - Actualized loot table and sea creatures rarities.
  - Removed functionalities related to Epic/Legendary Baby Yeti because there is no such drop anymore.
  - Some bugfixes caused by this patch.
- Added "Custom overlays style" settings section which lets you toggle gradient/single color background and border for the overlays. This style is toggleable for each overlay.
- Moved Water Hotspots & Bayou overlay into 2 separate overlays, because Water Hotspots might become more popular outside of Bayou. **Please re-enable in settings and adjust position if you used it!**
- Renamed Legion & Bobbin' Time tracker and its settings to Nearby entities counter, to fit new purpose of showing various entity counters. **Please re-enable in settings and adjust position if you used it!**
- Added Chumcap buckets to Nearby entities counter [disabled by default].
- Track Agatha's Contest rewards in the Fishing profit tracker. This includes Forest Essence and Agatha's coupons based on rank earned during contest.
- Added "Items" settings section with the following functionalities [all disabled by default]:
  - Highlight slots with trash enchanted books. Book names are configurable in settings. You can use it to quickly find books to throw away or insta sell.
  - Highlight Kat GUI slot with red color if you try to give her Epic Megalodon. _For those who sometimes puts pets into Kat instead of George by mistake c:_
  - Render percentage of Thunder / Storm / Hurricane Bottle charge progress in the item slot.
  - Render percentage of Moby-Duck evolving progress in the item slot.
  - Render recomb upgrade flag (R) for auto-recombobulated fishing drops in the item slot.

## Bugfixes

- Added Hilt of True Ice and Mob the Fish drops missing in the Fishing profit tracker.

# 1.3.0

Released on: 2026-03-10

## Features

- Added rare sea creatures highlighter (disabled by default). It applies glowing border to the rare sea creatures, Jawbus Followers and Wiki Tiki Laster Totems. Outline color depends on mob rarity. Not visible through walls!
  - Shoutout to **alpha-r** for contribiting into this feature!
- Added legendary Seal pet to `/feeshPetLevelUpPrices`.
- Added immunity timer to the Sea creatures HP overlay.
- Added `/feeshFearMongererShopPrices` command to see profitable items from Fear Mongerer NPC shop.
- Added `/feeshJunkerJoelShopPrices` command to see profitable items from Junker Joel NPC shop.

## Bugfixes

- Excluded Vanquisher from being tracked in Sc/h tracker and Barn fishing timer.
- Fixed Squid alert not working when Skyhanni's pet rarity chat setting enabled.
- Fixed (I hope) the issue when own Dwarven Lantern / Umberella was not shown in the overlay sometimes after placing.
- Fixed alert on `Lootshare!` pchat message not working.

## Special thanks

- alpha-r, for being the first contributor to this project :)

# 1.2.0

Released on: 2026-02-25

## Features

- Added support for 1.21.11.
- Added setting to hide other players' fishing hooks (disabled by default).
- Added Dwarven Lanterns to the deployables tracker and alerts (disabled by default).
- Added Rain/Thunderstorm timer overlay for Birch Park/Spider Den, and alert when Rain/Thunderstorm ends soon (disabled by default).
- Added settings to customize compact catch messages written to your chat when you catch any sea creature. You can type your own text for double hook and catch message.
- Added setting to hide other players near your bobber when a fishing rod is casted (disabled by default).

## Bugfixes

- Made Banshee alert / chat message not enabled by default for new mod users.

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