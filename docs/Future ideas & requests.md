# Future ideas and players' requests

## Fishing profit tracker

- View price as 7 days avg to avoid price manipulations.
- Add levelled Magma Necklaces as some people level them for profits. Vanquished Magma Necklace upgraded to +10☠! Vanquished Magma Necklace upgraded to +1☠!
- When there is no fishing rod in hand, the fish profit tracker and seacreature tracker can be hidden.
- Propose compacting or selling items like raw fish going to the inventory (full sack).
- Track Agatha tickets / Forest Essence on Galatea based on contest results.
- Use drop # in the chat message based on current profit tracker.
- Option to render icons instead of item names
- Track scavenged coins in Fishing Profit Tracker
- Track Ice Essence drop from mobs in Fishing Profit Tracker
- Track pet level progress in coins while it's not maxed
- Find out price calculation for Kuudra Keys
- Develop calculation for Level 100 pets on Ironman. So far I'm lazy to store NPC price for each existing pet in the module
- Separate tracker modes for different areas of fishing, or ability to load named session. E.g. to separate Jerry, Spooky, Crimson, Treasure, etc
- Rewrite way to ignore compacted loot in profit tracker
- [Bug] Some items are tracked by Fishing Profit Tracker when dropped, but drop was prevented by SB settings (basically it drops and picks up again).
- [Bug] Trading with other players adds items to the profit trackers.

## Deployables

- See other players' Black Holes around.
  Context: About the Black Hole list for parties: my idea was simply to show which players have already placed a Black Hole. When several Black Holes are stacked in the same spot, it can be a bit hard to visually tell who has placed one and who hasn’t. The main reason I thought of this is that some players still use vanilla or other third-party clients (such as BLC) and might not have access to this mod. In those situations, having a simple list can help party members coordinate and gently remind each other if needed.

- Allow viewing other players deployables in the overlay. Does someone need it?
- Overflux and its variations

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
- Alert on Hotspot fishing without Tiki Mask
- Reminder if user is fishing with wrong hook/sinker during events or in hotspots.
  - When first rod cast on Jerry: check for Icy Sinker
  - When first Spooky mob caught: check for Phantom Hook
  - When first hotspot rod cast: check for Hotspot Hook and Sinker (except for treasure fishing)
  - Check for Junk Sinker for treasure fishing in Bayou?
  - TODO: Other stuff?
- Alert if player attempts to be scammed by Kat by giving her Epic Megalodon.
- Rare SC cocooned alert
- Spooky festival event ending alert (to kill mobs before despawn)
- Phoenix proc'ed alert / Phoenix back alert (same as Spirit Mask)
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

## Bestiary tracker

- Bestiary tracker with session/total, cuz i like to get screenshots whenever i hit clean bestiary numbers (30k abyssal, 300k agarimoo, ...)
It might be not 100% correct due to how Hypixel API works, the person who requested usually opens /be every few catches, he needs to see when he's close.

## Inventory features

- Full amount of expertise kills on your rod: rn i use skyhanni for that but it only says "Expertise Kills: 1M (Maxed)" while badlion used to display the whole number
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

- Command to check profits for buying items from Fear Mongerer shop rather than selling candies as is
- Way to disable all module features with one toggle.
- Remove double hook reindrake logic because DH is not possible now
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