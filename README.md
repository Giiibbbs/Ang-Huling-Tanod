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

Work in progress. Current build: **project skeleton** (window, game loop, screen states).

| Area | State |
|---|---|
| Window, resizable game screen, game loop | Done |
| Menu / Play / Pause screens | Done (placeholder visuals) |
| Island tile map | Next |
| Lighthouse beam and night overlay | Planned |
| Warships, defenses, resources, HUD | Planned |
| Sound, high-score file | Planned |

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
| P or Esc | Pause and resume |
| M | Back to the menu (while paused) |

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
| [CREDITS.md](CREDITS.md) | Asset sources, licenses, and AI-assistance disclosure |

## Project structure

```
Ang-Huling-Tanod/
├── README.md
├── CREDITS.md
├── docs/
└── AngHulingTanod/            NetBeans project
    └── src/atingdagat/
        └── core/              Main, GameFrame, GamePanel, GameState, Constants
```

More packages (`map`, `entities`, `defense`, `ui`, `util`) are added as features are built.

## Team and contributions

| Member | Role |
|---|---|
| Gabriel Gabiliño | Lead programmer: architecture, game logic, game loop, file I/O, sound |
| Gio | Presentation slides, demo explanation, playtesting |
| Jaron | Art assets, README, UML and proposal |

> Update this table with specific contributions before submission. The commit history backs it up.

## Originality

The concept, story, and mechanics are the group's own and were approved by our professor, who found no similar game on
the Play Store. The invading empire (the Kordan Dominion) is entirely fictional, and no real country is depicted.

> TODO (team): add the date we checked the Play Store ourselves.

## Credits

See [CREDITS.md](CREDITS.md) for all third-party assets, licenses, and the note on AI-assisted art.
