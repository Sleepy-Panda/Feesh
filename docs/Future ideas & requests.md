# Future ideas and players' requests

## MC versions

Support for 1.21.11.

## Fishing profit tracker

- Add confirmation for deleting items from the tracker.
- Rare drop sound on expensive items added to sacks (e.g. Emperor's Skull).
- Track Agatha tickets / Forest Essence on Galatea based on contest results.
- Use drop # in the chat message based on current profit tracker.
- Option to right align the tracker (and others too)
- Option to render icons instead of item names
- Option to decrease elapsed time in Fishing Profit Tracker (when user forgot to stop tracker being afk)
- Track scavenged coins in Fishing Profit Tracker
- Track Ice Essence drop from mobs in Fishing Profit Tracker
- Track pet level progress in coins while it's not maxed
- Find out price calculation for Kuudra Keys
- Develop calculation for Level 100 pets on Ironman. So far I'm lazy to store NPC price for each existing pet in the module
- Separate tracker modes for different areas of fishing, or ability to load named session. E.g. to separate Jerry, Spooky, Crimson, Treasure, etc
- Rewrite way to ignore compacted loot in profit tracker
- [Bug] Some items are tracked by Fishing Profit Tracker when dropped, but drop was prevented by SB settings (basically it drops and picks up again).
- [Bug] Trading with other players adds items to the profit trackers.
- [Bug] Adding a Pet to tracker does not increase earned money.

## Deployables

- See other players' Black Holes around.
- Allow viewing other players deployables in the overlay. Does someone need it?
- Overflux and its variations

## Party commands

- Commands should have a cooldown so people do not spam.
- Triggers from pchat only so Vadim does not mute people for spamming in gchat :skull:
- Ability to disable commands in settings.
- Does not trigger if the overlay is disabled in settings.

- !feesh profit - shows Total profit, elapsed time & profit/h
- !feesh profit <item name> - shows amount of the item in profit tracker (may use partial item name, case-insensitive)
- !feesh sc <mob name> - shows amount of the SC in Sea creatures tracker, percent and double hook statistics
- !feesh since <mob name> - shows since from the active tracker, e.g. Wiki Tiki, Reindrake, Lord Jawbus, etc
- !feesh sc/h - shows sea creatures per hour
- !feeshsincevial
- !feesh vials

- !kdr - kills/deaths rate for thunder/jawbus [requires API key and custom backend]
- /feeshkdr <player> - to check myself or someone privately without sending to the party chat [requires API key and custom backend]

## Alerts & chat

- Rare SC cocooned alert
- Phoenix proc'ed alert / Phoenix back alert (same as Spirit Mask)
- Customized sounds for rare catches and rare drops
  - People want to customize sounds for Jawbus etc, individually for mobs and drops
  - Sounds folder: assets
  - Ability to open folder from settings
  - Ability to test sound from settings
  - Toggle setting to enable customization
- Multiple drops that happen at the same time lead to "You're sending messages too fast" error.
- Write smarter logic to detect personal cap alert (20 for CH, 5 for Crimson)
- Do not include baby magma slugs when producing cap alert.
- Attach Vials drop number to the pchat message
- Clean chat for spammy messages:
  - &r&eTry clicking this &r&fThunder Spark&r&e with an &r&5Empty Thunder Bottle&r&e to collect it!&r (31)
  - The Pocket Black Hole isn't effective against Snapping Turtle! (2) 
  - &r&cThe Pocket Black Hole isn't effective against Guardian Defender!&r

## Hotspots

- Hotspot location guesser

## Fishing XP tracker

- Fishing XP tracker
- Skill level tracking above fishing 60

## Inventory features

- Offer supercrafting or BZ sell when items like raw fish goes to inventory (sacks are full).
- Refactor all existing features so they consume less FPS

## Baits

- Bait changed alert
- No bait used alert
- Track baits cost in Fishing profit tracker

## Fishing bosses

- Bigger custom nametag for fishing bosses
- Kill time for fishing bosses

## Other

- Way to disable all module features with one toggle.
- Other player's hooks and lines hider.
- Glowing outline / highlight rare sea creatures (make this not visible through walls) - I want to get rid of SH for that, and replicate this feature in module.
- PogData resets data file on PC crash - consider doing regular backups / research tska library as replacement for PogData
- Remove double hook reindrake logic because DH is not possible now
- Rain/Thunder widget
- Golden fish timer
"can you add a golden fish timer tracker into this? i only have golden fish diamond left and i like playing other games while just having my bobber in lava, and setting a 15min timer every time is p annoying lmao"
- Thunder spark profit - Amount gained in bottle divided by lbin for the current item
  Hurricane: 400m
  Thunder sparkes gained in session: 1m 
  Profit for session: 80m

## Achievements

Unlocked: X/Total (%)
Sort by: rarity, locked/unlocked

- Crimson
  - [EASY] Catch your first Thunder
  - [EASY] Catch your first Jawbus
  - Get your first vial
  - Lootshare a vial
  - Get 25 vials (requires tracker to be enabled)
  - 400+ MF on vial (or 450?)
  - < 150 MF on vial. A non killed it, I swear!
  - No vial for 300 Jawbuses (requires tracker to be enabled) (Do I have negative MF?)
  - Full jawbus bestiary
  - 10 10 rod
  - Double hook Jawbus
  - B2B thunder (requires tracker to be enabled)
  - B2B2B thunder (requires tracker to be enabled)
  - B2B Jawbus (requires tracker to be enabled)
  - [EASY] Smth with deaths  (Wait, what is this white circle?.. <player> was killed by Thunder.)
  - No jawbus for 1000 catches (check that in magma lord)
  - No jawbus for 3000 catches (check that in magma lord)
- Jerry
  - No yeti for 1000 catches (requires tracker to be enabled)
  - No reindrake for 3000 catches (requires tracker to be enabled)
  - Lootshare a baby yeti
  - B2b yeti (requires tracker to be enabled)
  - B2B2B yeti (requires tracker to be enabled)
  - Get a yeti pet with less than 50 MF
  - Smth with tons of nutcrackers (requires tracker to be enabled)
  - Fish the entire event (spend ~9 hours)
- Spooky
  - 2 orbs in 10 seconds
- Ink
  - Squid / night squid leaderboard
  - Ink sack collection leaderboard?
- CH
  - Get N magma cores in 10 seconds
- Water
  - Get 2 lucky clover cores in 10 seconds
  - Full oasis bestiary (It was so much fun... Sigh / Useless grind)
- Bayou
- Galatea?
- Marina
  - Get 400+ sharks per festival
- Trophy
  - Gold hunter
  - DIamond hunter
- Treasure
  - Catch legendary squid?
- Dye
  - Obtain aquamarine / iceberg / etc dye
- Giant rod
- Dirt fishing - get worm the fish
- Equip 10 10 magma lord set
- 1B exp overflow
- ? exp overflow
- Top 10 in any fishing leaderboard
- Top 1 in any fishing leaderboard (mobs, trophy, collection, ...)
- All fishing bestiaries complete
- Be in party with me :3

- Do not count on Alpha
- 1s timeout after the main event