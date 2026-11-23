# Ang Huling Tanod

> *"May natitira pa tayo."*

A 2D tower defense and survival game built with **Java Swing and AWT** (no external engines or libraries).
You are the last *tanod* of Isla Dulo, the final free island of a fictional invasion. By day you harvest and build.
By night the sea goes dark and you can only see what the light touches: the lighthouse beam sweeping the water, your own
lantern, and the torches you planted. Walk the island, light the ships, shoot them, and keep
the radio alive so the world hears that someone is still here. Survive seven nights.

Final project for **Object-Oriented Programming**, BSIT 3A, University of Batangas Lipa City.
Final submission: **Nov 30, 2026**. Live demo: **Dec 1, 2026**.

---

## Status

Work in progress. Current build: **playable core**. Harvest by day, build nets, and defend the post at night with the lighthouse beam across 7 nights.

| Area | State |
|---|---|
| Window, resizable game screen, game loop | Done |
| Menu / Play / Pause screens | Done (placeholder visuals) |
| Island tile map (loaded from a text file, depth shading, foam) | Done |
| Lighthouse beam and night overlay (only lit things are visible) | Done |
| Walking tanod (WASD), lantern, auto-sweeping beam, torch posts, reach limits | Done |
| Day dial (sun, moon, time left) and clickable Workshop panel with costs and progress | Done |
| Supply crates: ore floats in from the sea (smart: favors the ore that has run out) | Done |
| Survivors by bangka (Ka Inday, Migo, Tonyo) at dawn after nights 2, 4, 6, plus the Pana tower | Done |
| Bigger island on a finer 10 px tile grid, with 11 choppable trees; brighter torches | Done |
| Night 7: Bakunawa eclipse event (lamp goes out, sound the gongs, the flagship is swallowed) | Done |
| Ending scene: lights across the sea, bangkas, sunrise, helicopter, title card | Done |
| First enemy ship (Skiff) with wake, art loader | Done |
| Day/night cycle, harvesting, nets, post and radio, HUD bars, win and lose | Done (first version, needs playtesting) |
| Waves and resource spots read from text files | Done |
| Cutter and barge ships, mine buoys (second defense) | Done (first version) |
| Upgrade center (pickaxe, axe, boots, fishing rod, level 3) and survivors working by day for meals | Done |
| Folklore allies: the duwende in the anthill (after night 1), the Lapu-Lapu stone statue (story, after night 3) and the manananggal (after night 5) | Done |
| Wear and tear with hold-X repair, Raider and Ghost ships, flare gun (F), How to Play screen, radio-voices ending, combat feel | Done (needs Gio's playtest) |
| Final ship art by Jaron: skiff, cutter, barge and the Kordan flagship | Done |
| Hold-to-harvest, 4 ores with different uses, 5 kinds of swimming fish, finite ore | Done (first version) |
| Sprites: all file names wired, art arrives from Jaron | In progress (shapes shown until then) |
| High-score table (`highscores.txt`, name entry, top 5 on the menu), developer keys hidden behind F12, "TAP SPACE" prompt on night 7 | Done |
| Sound, infiltrators, babaylan, bosses | Not planned (sound was not covered in class) |

The full list lives in [docs/02-project-plan.md](docs/02-project-plan.md).

## How to run

**From NetBeans (Apache NetBeans, JDK 26):**
1. Clone this repository.
2. Open the `AngHulingTanod` folder as a project (File > Open Project).
3. Right-click the project > **Run**, or press **F6**. The main class is `atingdagat.core.Main`.

**From the JAR** (available at final submission):
```
java -jar AngHulingTanod.jar
```

## Controls

Current build:

| Key | Action |
|---|---|
| Enter | Start from the menu |
| W A S D (or arrow keys) | Walk the island. You cannot walk into the sea |
| Mouse | Point at things to harvest, build or shoot |
| Space (hold, next to the lighthouse, night) | Steer the beam by hand toward the mouse. Otherwise it sweeps by itself |
| Space (tap, during the Bakunawa eclipse, night 7) | Sound the gongs. Tap fast to fill the noise meter |
| F (night) | Flare gun: lights a patch of sea at the mouse for 6 s (20 s cooldown) |
| Hold X (day or night, near a damaged defense) | Mend it: 1 point per press-and-hold, costs bamboo |
| P or Esc | Pause and resume |
| H (menu) or F1 (anytime) | The one-page How to Play screen |
| M | Back to the menu (while paused) |
| **Hold** left click on bamboo, ore or a fish school (day) | Harvest it. A ring fills. Let go early and it drains |
| Click a row of the **WORKSHOP** panel (left, day only) | Build or upgrade. Each row shows its cost, what you have, and a progress bar |
| B | Hide or show the workshop panel |
| 1 | Net tool on or off (then click shallow water; 3 bamboo) |
| 2 | Mine tool on or off (then click water near the island; 2 nickel) |
| 3 | Torch tool on or off (then click free land; 2 bamboo). Lights the shore all night, up to 20 |
| 5, 6, 7, 8, 9 | Buy the next level of the pickaxe, axe, boots, fishing rod or rifle (the rifle adds 70 px of gun range per level) (also click a row in the Workshop UPGRADES tab) |
| F12 (or the ` key, if F12 needs Fn on your laptop) | Turns the developer keys ON or OFF (they are OFF by default). A red "DEBUG KEYS ON" note shows when they are on |
| H (in game, F12 on) | Test key: wakes the duwende, and brings the manananggal now |
| U | Switch the Workshop between the BUILD and UPGRADES tabs |
| 4 | Pana tower tool on or off (then click free land; 4 bamboo). Shoots ships in range, even in the dark, up to 10 |
| Right click | Put the tool away |
| R | Repair the post and radio (2 meals of fish, day only) |
| C | Rewire the radio (2 copper, +25 signal; torches also glow 25% wider, up to 2 times) |
| V | Armor the post (2 chromite, +4 max health, up to 3 times; also plates nets, torches and towers: +1 durability per level, up to 2) |
| G | Upgrade the lamp (1 gold, wider and longer beam, and flares burn 40% longer, up to 2 times) |
| Left click (night) | Fire at a LIT ship (the beam, your lantern or a torch must be lighting it, and it must be within gun range) |
| N (F12 on) | Skip to the next phase (test key) |
| L (F12 on) | Jump to the day before the last night (test key) |
| E (F12 on) | Start the Bakunawa eclipse right now, at night (test key) |
| K (F12 on) | Drop a supply crate now, in the day (test key) |
| J (F12 on) | Bring all three survivors to the post now (test key) |

Planned: more defenses on the number keys. Details are in
[docs/01-game-design.md](docs/01-game-design.md).

## Documentation

| File | What is in it |
|---|---|
| [docs/01-game-design.md](docs/01-game-design.md) | Story, mechanics, enemies, folklore, win and lose rules |
| [docs/02-project-plan.md](docs/02-project-plan.md) | Roles, schedule, feature tiers, what we cut first |
| [docs/03-code-guide.md](docs/03-code-guide.md) | Feature-by-feature explanation of the code, for study and the demo |
| [docs/04-pitch-and-demo.md](docs/04-pitch-and-demo.md) | The 5-7 minute demo script and Q&A prep |
| [docs/05-git-workflow.md](docs/05-git-workflow.md) | How the three of us share the repo without conflicts |
| [docs/06-playtest-log.md](docs/06-playtest-log.md) | Peer playtest results and what we changed |
| [docs/07-art-prompts.md](docs/07-art-prompts.md) | Style bible and Gemini prompts, so all art matches |
| [docs/08-file-reference.md](docs/08-file-reference.md) | Every file and folder, plus where to change each number |
| [docs/09-build-story.md](docs/09-build-story.md) | Why we built it this way, step by step, for beginners |
| [docs/10-complete-guide.md](docs/10-complete-guide.md) | **Start here if you are new.** Tools, every folder, how it runs, recipes, glossary |
| [CREDITS.md](CREDITS.md) | Asset sources, licenses, and AI-assistance disclosure |

## Project structure

```
Ang-Huling-Tanod/
├── README.md
├── CREDITS.md
├── docs/
└── AngHulingTanod/            NetBeans project
    └── src/atingdagat/
        ├── core/              Main, GameFrame, GamePanel, GameWorld, DayCycle, Phase, WavePlan, GameState, Constants
        ├── map/               TileMap (island and sea)
        ├── entities/          Lighthouse, Beam, Warship, Skiff, Cutter, Barge, Raider, Ghost, Flare, Post, Radio
        ├── defense/           Defense (interface), Net, Mine
        ├── economy/           ResourceType, ResourceNode, FishSchool, FishKind, Harvestable, Inventory
        ├── ui/                NightOverlay, Hud, Popup, Explosion
        ├── util/              Assets (art loader)
        └── data/              island.txt, waves.txt, nodes.txt
```

Art goes in `res/` when Jaron adds it.

## Team and contributions

| Member | Role |
|---|---|
| Gabriel Gabiliño | Lead programmer: architecture, game logic, game loop, file I/O, sound |
| Gio | Presentation slides, demo explanation, playtesting, the paper (1-page proposal) |
| Jaron | Art assets, README, UML diagram, CREDITS |

> Update this table with specific contributions before submission. The commit history backs it up.

## Originality

The concept, story, and mechanics are the group's own and were approved by our professor, who found no similar game on
the Play Store. The invading empire (the Kordan Dominion) is entirely fictional, and no real country is depicted.

> TODO (team): add the date we checked the Play Store ourselves.

## Credits

See [CREDITS.md](CREDITS.md) for all third-party assets, licenses, and the note on AI-assisted art.


## Latest changes
- Nights escalate: ships come in pulses from several sides, and every night has a twist (fog, static, surge, hardened, assault).
- Filipino trees: narra, acacia (bamboo) and mango, coconut, banana (fruit = meals).
- Smaller shop panel; Manananggal weaker; Duwende gives a different gift each day.

- New: wear and tear (nets, torches, Pana towers lose 1 durability each dawn, mend with X for bamboo), Raiders (shoot your towers and torches), Ghost ships (slow to show, quick to vanish in the light), and the flare gun (F) so nights have more to do.
- New: How to Play page (H on the menu, F1 anytime), radio voices from other islands (one each dusk, they answer in the ending), and combat feel (hit flash, damage numbers, screen shake, a ring around ships lit by the beam).
