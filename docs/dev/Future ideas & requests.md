# Future ideas and players' requests

## Overlays general

- Pause timers in 30 seconds or 1 minute of inactivity, but still track drops while tracker is visible.
  - To cover the case when you don't fish and kill bosses, or someone kills your SC while you are AFK with paused tracker.
- When there is no fishing rod in hand, some trackers can be hidden (optional setting).
- Pause button pauses all widget's timers so you dont have to click each widget's button individually. Same thing that keybind does.

## Barn fishing timer

- Detect personal cap before it actually happened. Currently there is cap 10 SC.
- Change overlay/alerting so it can rely on own creatures
- Split entities count as 1 (e.g. 3 Baby Magma Slugs give 1 to cap). How to detect ownership?

## Sea creature HP tracker

- Add custom Ragnarok immunity timer - when it goes below 50% hp (e.g. 62.4). Max HP is 125 mil, or 250 on Derpy. It takes around 12 seconds, but if server is laggy it takes unpredictable time.
- Add custom immunity logic for Puddle Jumper.
- Add owner's name for sea creatures (might be more tricky for cocooned).

## SC tracker

- Track cocooned stuff. How to detect who is the owner for the mob cocooned?

## Fishing profit tracker

- Track Blizzard in Bottle, Moby Duck, bought rain, consumed bait (exclude from profits).
- Add levelled Magma Necklaces as some people level them for profits. Vanquished Magma Necklace upgraded to +10☠! Vanquished Magma Necklace upgraded to +1☠!
- Propose compacting or selling items like raw fish going to the inventory (full sack).
- Use drop # in the chat message based on current profit tracker.
- Option to render icons instead of item names
- Track scavenged coins in Fishing Profit Tracker
- Track pet level progress in coins while it's not maxed
- Find out price calculation for Kuudra Keys
- Develop calculation for Level 100 pets on Ironman. So far I'm lazy to store NPC price for each existing pet in the module
- Separate tracker modes for different areas of fishing, or ability to load named session. E.g. to separate Jerry, Spooky, Crimson, Treasure, etc
- Rewrite way to ignore compacted loot in profit tracker
- [Bug] Some items are tracked by Fishing Profit Tracker when dropped, but drop was prevented by SB settings (basically it drops and picks up again).
- [Bug] Trading with other players adds items to the profit trackers.

## Trophy

Some highlight / icon rendering to detect missing trophies/rarities in Odger and Trophy Frogs menu.

Sample messages:

RIBBIT! [MVP+] _etaF caught their first DIAMOND Common Frog!
♔ TROPHY FROG! You caught a Common Frog BRONZE!
♔ TROPHY FROG! You caught a Puddle Jumper SILVER!

## Deployables

- Overflux and its variations

## Consumables

Buffs from Lotus Atoll cave donations:
WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll!
WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!

## Party commands

- Chat settings - Party commands. Add individual settings to let people explore Fishing profit tracker, Sc/h, Sea creatures, etc.
- Commands should have a cooldown so people do not spam.
- Triggers from pchat only so Vadim does not mute people for spamming in gchat :skull:
- Does not trigger if the overlay is disabled in settings or nor visible.

!feesh sc/h
1000 sc/h for 2h or 1000 catches/h for 2h

!feesh sc|sctotal
Session: 200 rare sc out of 2000 sc total

!feesh sc|sctotal name:Abyssal
Session: 50x Abyssal Miner

!feesh profit
Session: 100M for 2h (50M/h)

!feesh totalprofit
Total: 100M for 2h (50M/h)
If timer not enabled, just show total

!feesh profit|totalprofit top
Session: 10x Deep Sea Orb, 1x bla bla, 100x bla bla

!feesh profit|totalprofit item:Deep Sea Orb
Session: 10x Deep Sea Orb

item name validation: at least N characters, return top 10 results

!feesh profit|totalprofit item:Baby Yeti
Session: 10x Baby Yeti (Epic), 5x Baby Yeti (Legendary), 1x Level 100

## Alerts & chat

- RARE DROP! Troubled Bubble (+406 ✯ Magic Find) - with no mf also possible
- RARE DROP! Octopus Tendril (+190 ✯ Magic Find)
- Player who was dead returned back
- Alert which soul has dropped for Summoning Ring.
- Alert on Hotspot fishing without Tiki Mask
- Reminder if user is fishing with wrong hook/sinker during events or in hotspots.
  - When first Spooky mob caught: check for Phantom Hook
  - When first hotspot rod cast: check for Hotspot Hook and Sinker (except for treasure fishing)
  - Check for Junk Sinker for treasure fishing in Bayou?
  - TODO: Other stuff?
- Spooky festival event ending alert (to kill mobs before despawn)
- Phoenix proc'ed alert / Phoenix back alert (same as Spirit Mask)
- Write smarter logic to detect personal cap alert (10 sc, probably still 5 for Crimson)
- Do not include baby magma slugs when producing cap alert.
- Attach Vials drop number to the pchat message
- Clean chat for spammy messages:
  - &r&eTry clicking this &r&fThunder Spark&r&e with an &r&5Empty Thunder Bottle&r&e to collect it!&r (31)
  - The Pocket Black Hole isn't effective against Snapping Turtle! (2) 
  - &r&cThe Pocket Black Hole isn't effective against Guardian Defender!&r

## Hotspots

- Hotspot location guesser
- Render hotspot area more visible
- Show a flag when hook is in hotspot

## Fishing XP tracker

- Fishing XP tracker
- Skill level tracking above fishing 60

## Bestiary tracker

- Bestiary tracker with session/total, cuz i like to get screenshots whenever i hit clean bestiary numbers (30k abyssal, 300k agarimoo, ...)
It might be not 100% correct due to how Hypixel API works, the person who requested usually opens /be every few catches, he needs to see when he's close.
- Leaderboards tracker, to see if someone passed your position.

## Inventory features

- Full amount of expertise kills on your rod: rn i use skyhanni for that but it only says "Expertise Kills: 1M (Maxed)" while badlion used to display the whole number
- Offer supercrafting or BZ sell when items like raw fish goes to inventory (sacks are full).

## Baits

- SB added the functionality of your current bait being slown in slot #9 while fishing
- Bait changed alert
- No bait used alert
- Track baits cost in Fishing profit tracker

## Fishing bosses

- Bigger custom nametag for fishing bosses
- Kill time for fishing bosses

## Other

- Hide fire around Ragnarok
- Hide damage nametags from Flay
- Separate data tracking/settings for Alpha and Live server? Probably for Bingo too?
- Way to disable all module features with one toggle.
- Golden fish timer
"can you add a golden fish timer tracker into this? i only have golden fish diamond left and i like playing other games while just having my bobber in lava, and setting a 15min timer every time is p annoying lmao"
- Thunder spark profit - Amount gained in bottle divided by lbin for the current item
  Hurricane: 400m
  Thunder sparkes gained in session: 1m 
  Profit for session: 80m

## Personal best / personal worst

- Catches to get leg/mythic creatures
- Spooky mobs?

## Achievements

- The main blocker here is how to draw custom GUI which displays readonly list of achievements with sorting/filtering/sections. Current library used for settings does not let to draw dynamic readonly content.
- Do not count on Alpha
- 1s timeout after the main event to not override
- Achievement rarities? Common, Uncommon, Rare, etc.

Unlocked: X/Total (%)
Sort by: rarity, locked/unlocked

- Crimson Isle
  - Catch your first Thunder
  - Catch your first Jawbus
  - Get your first vial
  - Lootshare a vial
  - 400+ MF on vial (or 450?)
  - < 150 MF on vial. A non killed it, I swear!
  - No vial for 300 Jawbuses (requires tracker to be enabled) (Do I have negative MF?)
  - Full jawbus bestiary
  - Double hook Jawbus
  - B2B thunder (requires tracker to be enabled)
  - B2B2B thunder (requires tracker to be enabled)
  - B2B Jawbus (requires tracker to be enabled)
  - Smth with deaths  (Wait, what is this white circle?.. <player> was killed by Thunder.)
  - No jawbus for 1000 catches (check that in magma lord)
  - No jawbus for 3000 catches (check that in magma lord)
  - Kill Jawbus with no death
  - LS frag
- Crimson Hotspots
- Jerry
  - Start Blizzard
  - No yeti for 1000 catches (requires tracker to be enabled)
  - No reindrake for 3000 catches (requires tracker to be enabled)
  - Lootshare a baby yeti
  - B2b yeti (requires tracker to be enabled)
  - B2B2B yeti (requires tracker to be enabled)
  - Get a yeti pet with less than 50 MF
  - Smth with tons of nutcrackers (requires tracker to be enabled)
  - Fish the entire event (spend ~9 hours)
- Spooky
- Ink
- CH lava
  - Get N magma cores in 10 seconds
- CH water
- Water
  - Get 2 lucky clover cores in 10 seconds
  - Full oasis bestiary (It was so much fun... Sigh / Useless grind)
- Bayou
- Galatea
- Atoll
  - Donate 4 coins
  - Finish Puddle Jumper ride
- Marina
  - Get 1000+ sharks per festival
  - Get 1500+ sharks per festival
- Trophy
  - Gold hunter
  - Diamond hunter
- Treasure
  - Catch legendary squid
  - Catch a bouncy beach ball
  - Catch a bone
- Dye
  - Obtain aquamarine / iceberg / etc dye
- Setup
  - Giant rod
- Dirt fishing
  - Catch worm the fish
- Skill xp / Bestiary
  - 1B exp overflow
  - ? exp overflow
  - Top 10 in any fishing leaderboard
  - Top 1 in any fishing leaderboard (mobs, trophy, collection, ...)
  - All fishing bestiaries complete
- Other
  - Install the mod
  - Have the zombie slayer quest active while fishing
  - Have a inferno demonlord quest active
  - Drop a warden heart?
  - Be in party with me? :3
  - Do not fish for N days :c

- fishtastic
- oFISHial
- just squidding, you gotta be squidding me
- ink about it
- selFISH
- You're shrimply the best
- Cod bless you
- What the fish?