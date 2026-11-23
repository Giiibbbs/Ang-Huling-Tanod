# Game Design: Ang Huling Tanod

*Source of truth for what the game is. If the code and this file disagree, discuss it as a team, then fix one of them.*

## Overview

Ang Huling Tanod is a 2D tower defense and survival game where you defend the last free island of a fictional invasion
until the world hears that someone is still there. It is built with Java Swing and AWT and fits the class genre
**Tower Defense**, with survival and management on top.

What makes it different:

- **A lighthouse beam you steer.** At night you only see what the beam lights.
- **Resources that double as weapons.** Overharvesting the island weakens your defenses.
- **Philippine folklore in the mix:** Bakunawa, the babaylan, the tikbalang, and the engkanto.
- **A radio you must keep alive** to broadcast "May natitira pa tayo" every night.

## Story and setting

The Philippines has fallen to a fictional naval empire, the **Kordan Dominion**. **Isla Dulo**, a resource-rich island at
the far end of the archipelago, is the last place still free.

You are the last tanod. The Dominion wants the island's fish, timber, and ore. The rest of the map has gone silent, so
your radio is the only link to the outside.

When the Dominion's guns shook the seabed, the island's old guardians woke up. That is why a modern fleet can meet
Bakunawa and the babaylan in the same game.

The Dominion, its flag, its language, and its ships are invented, and no real country is shown. Combat is stylized, and
blood effects stay off by default.

## The loop

One night is one pass through the loop. Surviving night 7 triggers the Bakunawa eclipse, and then the ending (see "The ending" below).

1. **Day: harvest and build.** Send villagers to gather fish, bamboo, and ore, and spend resources on defenses.
2. **Dusk: preview and prepare.** A preview shows what the next wave looks like, so you can adjust.
3. **Night: defend and broadcast.** Steer the beam, identify targets, fire, and keep the radio alive.
4. **Dawn: report and repair.** Count the damage and repair. Then ask: is night 7 done? If yes, the ending plays
   and you win. If no, the next night begins.

The big decision in every loop is how much to harvest, because overharvesting weakens the island.

## Resources, defenses, and the post

The island's wealth is your arsenal: harvest by day, build before dusk, and don't strip the island bare.

| Resource | Source | Used for |
|---|---|---|
| Fish | Reef and fishing spots | Food and crew morale |
| Bamboo | Groves | Nets, watchtowers, decoys |
| Ore | The old mine | Mines, coastal guns, repairs |

**Island health** drops when you overharvest. A low value weakens every defense and lowers the engkanto's favor.

| Defense | Cost | What it does |
|---|---|---|
| Net barrier | Bamboo | Slows ships in a lane |
| Watchtower | Bamboo | Extends what you can see at night |
| Decoy light | Bamboo | Pulls ships off course |
| Mine buoy | Ore | Damages barges before they land |
| Coastal gun | Ore | Main damage against ships |

**The post** is a grounded ship on the reef. It holds the radio and carries a deck gun that you aim with the mouse. If
its integrity reaches zero, you lose.

### Philippine minerals to showcase

The island's ore can show real Philippine strengths. In 2024 the country ranked second in nickel production, and gold
made up half of its metallic mineral value.

| Mineral | Philippine fact (2024) | In-game use (proposal) |
|---|---|---|
| Nickel | Second-ranked producer, 9.5% of world output; 36 of 58 operating metal mines | Heavy defenses: mine buoys and reinforced nets |
| Gold | 28,870 kg mined; 50% of metallic mineral value | Rare drop that pays for radio upgrades; raiders target it |
| Copper | 53,182 t of copper content mined; 3 operating mines | Power and wiring: lighthouse lamp and radio repairs |
| Chromite | 127,613 t mined; 4 operating mines | Armor plating for the post and the coastal gun |
| Cobalt and scandium | Produced alongside nickel; the Philippines ranked 7th in cobalt | Small bonus drops when nickel is processed |

For flavor, villagers can work a *Minahang Bayan*, a declared small-scale mining area; 50 of the 63 declared areas are
for gold. To keep scope small, **ship Ore as one resource** and show these as named ore types in the interface.

Sources: [USGS, The Philippines (2024 report)](https://www.usgs.gov/centers/national-minerals-information-center/philippines)
and [MGB, Minerals Industry at a Glance (April 2025)](https://mgb.gov.ph/images/Mineral_Statistics/2024/MINERALS_INDUSTRY_AT_A_GLANCE_Updated_11_April_2025.pdf).

## Wear, repair and the flare

Nets, torches and Pana towers have durability (4, 3, 4). Each dawn every one loses 1 (never below 1), and Raider shots take more. Hold X near a damaged defense to mend one point at a time (nets and torches cost 2 bamboo per point, Pana 3). You can carry at most **30 bamboo** (`BAMBOO_CAP`); extra is lost and a full pack shows "Bamboo full (30)" without using up the node. This is the tension on resources: bamboo is for building AND upkeep. At night press F to fire a flare at the mouse (range 260 px, burns 6 s, 20 s cooldown); it lights ships like the beam does, so you can cover a second side.

## Enemies

The Kordan fleet comes through the fog in five ship classes. The stats are placeholders to tune during playtesting
(stats will live in text files so they can be changed without touching code).

| Enemy | Role | What it does | Counter |
|---|---|---|---|
| Skiff | Fast scout | Arrives first and marks the island; an unlit skiff makes the next wave bigger | Beam and quick shots |
| Cutter | Gunboat | Stops at short range and fires at defenses | Heavier defenses and nets |
| Raider | Tower hunter (from night 2) | Stops at range 85 and shoots the nearest Pana tower or torch, 1 damage per shot | Light it fast with the beam or a flare, kill it before it fires |
| Ghost ship | Stealth (from night 3) | Slow to appear in light, quick to vanish again, so the beam must stay on it | Flare gun (F) holds the light in place |
| Landing barge | Troop carrier | Beaches and spawns raiders | Mine buoys before it lands |
| Signal frigate | Priority target | Jams the radio while in range | Decoys, then focused fire |
| Dreadnought | Boss, night 7 | Outranges defenses and shells them from afar | Decoys, then hit its weak points |

Ground and stealth units:

- **Raiders** land and walk inland toward the resource sites.
- **Saboteur divers** stay invisible to the beam until they are close and cut your nets.
- **Infiltrators** look like fishing bangkas. Identify them with the beam before firing; shooting a real fisher costs
  morale.
- **Recon drone (optional)** marks targets and is countered by a flak tower. Add it only after the core works.

Every ship has weak points: hitting the engine makes it drift, and hitting the bridge ruins its aim.

**Wave pacing over 7 nights:**

| Night | What happens |
|---|---|
| 1 to 2 | Skiffs only |
| 3 | Cutters, and the first infiltrator |
| 4 | The Bakunawa eclipse |
| 5 | Landing barges and raiders |
| 6 | The signal frigate |
| 7 | The dreadnought boss and the final broadcast |

## Folklore layer

The island's guardians wake up as the invasion deepens. Each one adds a tactical choice and is portrayed with respect.

| Guardian | When | Effect |
|---|---|---|
| Bakunawa | Eclipse night (night 4) | Rises from the sea and attacks everything, including the Kordan fleet. Drive it off with noise (gongs and pots, as in the myth) or bait ships into its path. |
| Babaylan | Always available | A healer who restores crew and defenses, blesses one tower per night, calms the sea to slow ships, and reads omens at dusk to preview the next wave. |
| Tikbalang | Forest zone | Raiders that enter the forest are misdirected and walk in circles. |
| Engkanto | Tied to island health | Overharvesting angers them, and they withdraw their blessing, which weakens your towers. |

Bakunawa and the babaylan are **should-haves**. The tikbalang zone and the engkanto favor are **nice-to-haves**.

## Heroes and cameos

**Lapulapu**, the Mactan datu who resisted Magellan in 1521, joins as the island's stone guardian and your
**once-per-run hero call**. ("Lapulapu" is the official spelling under Executive Order No. 152 of 2021; "Lapu-Lapu" is
also common.) Very little is reliably known about his life, and nobody knows what he looked like.

- **Legend fit:** local legend says he never died and was turned to stone, guarding the seas of Mactan. On Isla Dulo he is
  a stone figure on the shore that wakes with the other guardians.
- **Hero call:** once per run, call on him to raise a reef wall for one night. Ships stop and anchor offshore instead of
  landing, as at Mactan, where rocks and coral reefs kept Magellan's ships from reaching the beach. Raiders cannot land
  that night.
- **Portrayal:** a stone silhouette, not a realistic portrait, and no invented words in his mouth.

**Classmate cameos (nice-to-have):**

- **As heroes:** named villager companions, each with a cartoon avatar loaded from an images folder, a name from a text
  file, and one small ability.
- **As enemies:** only as a clearly marked comedy cameo, such as a Kordan captain skin. Keep the invaders themselves
  serious.
- Ask each classmate before using their name or face.

Sources: [Wikipedia, Lapulapu](https://en.wikipedia.org/wiki/Lapulapu) and
[Culture Trip, The Story of Lapu-Lapu](https://theculturetrip.com/asia/philippines/articles/the-story-of-lapu-lapu-the-legendary-filipino-hero).

## Controls and HUD

Controls are mouse-first, so anyone can learn them in a minute.

| Input | Action |
|---|---|
| Mouse click | Place a defense, or send villagers to harvest by day |
| W A S D | Walk the island (the tanod cannot enter the sea) |
| Hold Space at the lighthouse | Steer the beam by hand at night. Otherwise it sweeps by itself |
| Left click | Harvest, build, or fire at a lit ship, all within the tanod's reach |
| Space | Sound the horn and gongs (noise) |
| 1 to 5 | Choose a defense |
| P | Pause |
| Esc | Back to the menu |

> The current skeleton also uses **Enter** (start), **P or Esc** (pause), and **M** (menu from pause). We'll reconcile
> these with the table above when the Play state is built.

The HUD shows only what the player needs: night counter, island health, post integrity, radio signal, resources, the
defense bar, and a dusk preview of the next wave.

## Win, lose, and scoring

You **win** by surviving 7 nights. The ending is a story told in two parts.

### The ending

**Night 7: the Bakunawa.** When the last wave of night 7 has set sail, the moon goes dark (an eclipse) and the Bakunawa, the
moon-eating dragon of Filipino folklore, rises from the sea. **The lighthouse lamp goes out**, so only the lantern and the
torches give light. The Kordan flagship waits at sea in a red glow. In the old stories people drove the Bakunawa away by making
noise with pots and gongs, so the player must **tap Space to sound the gongs** and fill a noise meter (it drains if you stop). When
it is full the dragon dives, swallows the Kordan flagship, and every ship still afloat goes down with it. Then it sinks away and the
moon returns.

**The radio voices.** Every dusk the radio catches one faint voice from another island (Isla Verde, Lubang, Maricaban, Sibuyan, Culion, Burias, Fortune Island), shown in the story box. They are read from `data/voices.txt`, one line per night. Seven nights, seven voices.

**Dawn of the eighth day.** A short story plays over the island (about 31 seconds, ENTER skips it):

1. The sea is dark and quiet. The Kordan fleet is gone.
2. Tiny lights appear one by one across the water.
3. The same seven voices answer one by one, by name, as their bangkas with lanterns sail in while the sun rises.
4. A helicopter flies in over the post.
5. The title card: **"May natitira pa tayo."** and **"You were never the last tanod."**

The idea: the title says "the last tanod", and the ending shows he never was. The radio broadcast was heard.

You **lose** when any of these happens:

- Post integrity reaches zero.
- The radio signal stays at zero for too long.
- Island health reaches zero.

Score comes from nights survived, enemies stopped, infiltrators correctly identified, and island health left at the
end. The best scores are saved to a file.

## Art direction

3/4 top-down pixel art with more detail than a low-resolution look: the game draws on an internal 640x360 screen and
scales it up (see the code guide). 10x10 tiles (the island is 64 by 36 tiles). Free CC0 asset packs plus AI-assisted reference art for custom pieces;
every source is listed in [CREDITS.md](../CREDITS.md). The art stays inspiration-only: we do not copy any existing
game's art.

## Features by priority

Build the must-haves first, and cut from the bottom if time runs short.

| Tier | Features |
|---|---|
| Must-have | Island tile map loaded from a file, harvesting by day, day and night cycle with a dusk preview, lighthouse beam and fog, net barrier, mine buoy, coastal gun, deck gun, skiff, cutter, and landing barge, post integrity, radio signal, and island health bars, Menu, Play, Pause, Victory, and Game Over screens, sound, high-score file |
| Should-have | Infiltrators and the identify mechanic, signal frigate and dreadnought boss, babaylan, Bakunawa eclipse event, watchtower, decoy light, overharvesting effects, relief fleet ending |
| Nice-to-have | Raiders and saboteur divers, recon drone and flak tower, tikbalang zone, engkanto favor, optional blood effects setting |

## Java notes and open decisions

Everything uses basic Java, Swing, and AWT, with no external libraries. The full class diagram comes with the proposal.

- **Window and drawing:** a `JFrame` with a `JPanel` that draws everything in `paintComponent` using `Graphics2D`.
- **Game loop:** a `javax.swing.Timer`.
- **Game states:** an enum (`GameState`): MENU, PLAY, PAUSE, GAME_OVER, VICTORY. Day and night are phases *inside* PLAY.
- **Core classes (planned):** `Island` (tile map from a text file), an abstract `Warship` with subclasses and
  `WeakPoint`, a `Defense` interface, `Beam` (a rotating cone that lights ships), `Radio`, `Post`, `ResourceNode`, and a
  `NightEvent` interface for the Bakunawa eclipse.
- **Pathing:** BFS along sea lanes for ships and along land tiles for raiders.
- **Files:** high scores and settings in plain text, with try-catch around every read and write.
- **Sound:** `javax.sound.sampled` with royalty-free WAV files.

Open decisions for the team:

- [ ] Ship stats and how many defenses per night
- [ ] Length of day and night
- [ ] Whether to include the drone
- [ ] Final art style and who draws what
- [ ] Whether blood effects ship at all (default off)
- [ ] Professor's confirmation of the gameplay changes, and our own Play Store check


---

## Build status (what is in the game now)

This document is the full vision. The list below is what the playable build contains as of **Nov 19**. The rest follows the
schedule in [02-project-plan.md](02-project-plan.md).

| Design item | In the build? | Note |
|---|---|---|
| Island map from a text file, depth shading | Yes | `data/island.txt`. 64 x 36 tiles of 10 px. The island is about 1.2 times bigger than the first version, same shape, with a smoother coast |
| Trees | Yes | 11 trees on the island. Hold the mouse on one to chop it (3 bamboo each, 3 chops per tree, grows back slowly) |
| Day, dusk, night, dawn cycle | Yes | Dusk fades the light in. It is not yet a "preview of the night" |
| Harvesting (bamboo, 4 ores, 5 fish) | Yes | You HOLD the mouse. Bamboo regrows slowly, ore never does, fish schools swim and must be followed |
| Ore uses | Yes | Ores power the night. Nickel: mines. Copper (2): radio boost, and torches glow 25% wider per level (2 levels). Chromite (2): Post +4 health, and every net, torch and Pana gets +1 durability per level (up to +2). Gold (1): wider lamp beam, and flares burn 40% longer per level. Ore is finite (27 units on the map, plus crates), and mining is quick (100-160 frames per unit) so it fits inside a 75 s day |
| Lighthouse beam and darkness | Yes | The beam now sweeps by itself, so there is no more circling to cover blind spots |
| Walking tanod, lantern, torch posts | Yes | WASD. The lantern lights a small circle. Torches (2 bamboo, up to 20, on land) light fixed patches of shore (radius 50 px) |
| Day dial and Workshop panel | Yes | The dial shows the sun and moon, the time left in the phase, and ships left at night. The Workshop (left, by day) lists every build and upgrade with cost, what you have, and level. You can click rows or use the keys |
| Survivors by bangka | Yes | After night 2, 4 and 6 a bangka lands at dawn with a survivor, a classmate-inspired character. **Ka Inday** (Babaylan) slowly heals the post and radio. **Migo** (radio nerd) keeps the signal climbing and brings crates faster. **Tonyo** (sentry) shoots lit ships near the post. They stand with you in the ending |
| Upgrade center and day workers | Yes | The Workshop has an UPGRADES tab. Five tools go to level 3 (the new Rifle adds gun range) for bamboo (every level) and a little ore (levels 2 and 3): pickaxe and axe make mining and chopping about 18% quicker per level, boots walk 15% faster per level, the rod casts faster and 15 px further. Each day every survivor is paid 1 meal and then chops trees and bamboo and carries up to 3 loads to the post. No meal means they rest |
| Escalating nights | Yes | Ships arrive in pulses (1, 2, 1, 1, 3, 4, 4 together; nights 3 and 4 are a steady one-at-a-time stream so the pressure never lets up) from different sides. Each night has a twist: 1 calm, 2 two fronts, 3 fog (beam 30% shorter), 4 radio static, 5 faster ships, 6 tougher ships, 7 all-out assault. The beam follows the mouse instantly. Trees: narra and acacia give bamboo; mango, coconut and banana give fruit (meals) |
| Folklore allies | Yes | **Duwende:** after night 1 the anthill (punso) on the west shore stirs. Leave 3 bamboo and he leaves you 1 ore every morning (the one you have least of, sometimes gold). **Lapu-Lapu stone:** after night 3 the story tells of the statue on the northeast islet: Lapu-Lapu was turned to stone, not killed, and may only be sleeping. For now it is a landmark. Freeing him is a planned feature. **Manananggal:** after night 5 she arrives, a story twist: the Kordans hunted her too. She does not work for meals. At night she swoops on the nearest ship in range and returns to her roost over the post |
| Pana tower | Yes | 4 bamboo, up to 4, on free land. Shoots the nearest ship in range about every 1.7 s with no light needed |
| Supply crates | Yes | A crate floats in about every 24 s (every 14 s if the radio is 70 or more). Cast a line and hold the mouse on it to get 1-2 ore. Ore that has run out on the map is 3 times as likely to be inside, so the player is never stuck. Drifts away at night or after 45 s |
| Reach limits | Yes | Land things 40 px, fish 120 px (cast from the shore), building 80 px, gun 200 px |
| Net barrier | Yes | 3 bamboo, shallow water. Ships stop and cut it |
| Mine buoy | Yes | 1 nickel. Explodes when a ship comes within 18 px, 4 damage in 38 px, and sets off any mine within its blast (chain reaction) |
| Skiff, cutter, landing barge | Yes | Waves in `data/waves.txt` |
| Post and radio, win and lose | Yes | Post health and radio signal. There is no separate "island health" bar yet |
| Gun | Simplified | Click a lit ship to fire. The aimed deck gun and the coastal gun are planned |
| Repair | Simplified | Press R in the day (2 meals of fish). No resource-depletion penalty yet |
| High-score file | Yes | Top 5 by nights survived, ties broken by Post health left; name typed after the run |
| Sound | No | Not covered in class, so left out |
| Bakunawa eclipse (night 7) | Yes | A scripted event: moon eaten, lamp out, tap Space for the gongs, the flagship is swallowed. Not a boss fight with health |
| Ending scene | Yes | Lights across the sea, a radio voice, bangkas, sunrise, helicopter, title card. Replaces the plain victory text |
| Infiltrators, babaylan, frigate, dreadnought | Not yet | See the cut order in the plan |


## Added Nov 23: endless mode, the mender and the parol

- **Endless mode.** After the ending card, press E to keep playing. Night 8 starts with a fresh day and half the Post healed. From night 8 the fleet is 15% bigger per night (on night 7's plan) and arrives up to 40% faster, and the seven twists repeat (Two Fronts, Fog, Static, Surge, Hardened, All-out Assault). The run ends when the Post falls or the radio dies (or Q in the pause menu), and the nights survived go to the high-score table as "N (endless)". It exists because the first win otherwise ends the game: the high-score table now has a reason to replay.
- **Tonyo the mender.** Besides shooting, when he is at home he mends the most worn net, parol or Pana by 1 point every 12 s, free. It gives the third survivor a second job and lowers the bamboo pressure of upkeep.
- **Parol.** The torches are drawn as parols, the star lanterns of a Filipino Christmas (four colours, twinkling). It is the same Defense in code (`Torch`); only the look and the words changed.

## Added Nov 23: more Bakunawa moments

The Bakunawa used to appear only on night 7. Now it teaches and returns:
- **Nights 3 and 5, the omen:** the moon turns red and Ka Inday warns on the radio. Nothing else changes.
- **Nights 4 and 6, a small visit:** halfway through the night the moon dims and the lamp goes out. Tap SPACE to sound the gongs until the meter is full, and the dragon sinks back (no swallow, no flagship). If the player is too slow the survivors join in after 8 s. This teaches the gong before the finale and makes the player use flares and parols in the dark.
- **Night 7:** the full event, unchanged.
- **Endless mode:** a small visit every 3rd night (10, 13, ...) with a faster-draining meter.
