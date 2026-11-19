# Ang Huling Tanod

> *"May natitira pa tayo."*

A 2D tower defense and survival game built with **Java Swing and AWT** (no external engines or libraries).
You are the last *tanod* of Isla Dulo, the final free island of a fictional invasion. By day you harvest and build.
By night you steer a lighthouse beam across the dark sea, because you can only see what the light touches, and keep
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
| First enemy ship (Skiff) with wake, art loader | Done |
| Day/night cycle, harvesting, nets, post and radio, HUD bars, win and lose | Done (first version, needs playtesting) |
| Waves and resource spots read from text files | Done |
| Cutter and barge ships, mine buoys (second defense) | Done (first version) |
| Hold-to-harvest, 4 ores with different uses, 5 kinds of swimming fish, finite ore | Done (first version) |
| Sprites: all file names wired, art arrives from Jaron | In progress (shapes shown until then) |
| Sound, high-score file, infiltrators, babaylan, Bakunawa, bosses | Next |

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
| Mouse | Aim the lighthouse beam (it turns toward the mouse) |
| P or Esc | Pause and resume |
| M | Back to the menu (while paused) |
| **Hold** left click on bamboo, ore or a fish school (day) | Harvest it. A ring fills. Let go early and it drains |
| 1 | Net tool on or off (then click shallow water; 3 bamboo) |
| 2 | Mine tool on or off (then click water near the island; 2 nickel) |
| Right click | Put the tool away |
| R | Repair the post and radio (2 meals of fish, day only) |
| C | Rewire the radio (2 copper, +25 signal) |
| V | Armor the post (3 chromite, +4 max health, up to 3 times) |
| G | Upgrade the lamp (2 gold, wider and longer beam, up to 2 times) |
| Left click (night) | Fire at a ship the beam is lighting |
| N | Skip to the next phase (test key) |

Planned controls (mouse first): move the mouse to aim the lighthouse beam, left click to fire the deck gun or place a
defense, number keys 1 to 5 to choose a defense, Space to sound the gongs. Details are in
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
        ├── entities/          Lighthouse, Beam, Warship, Skiff, Cutter, Barge, Post, Radio
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
