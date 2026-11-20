# File Reference

*A map of everything in the repository: what each file is, why it exists, and which number to change to tune the game.
Use it when you ask "where is that?" or "what does this file do?". Updated each time we add a feature.*

## The folders at a glance

```
Ang-Huling-Tanod/                  the repository (what GitHub shows)
├── README.md                      the front page
├── CREDITS.md                     assets, licenses, AI disclosure
├── .gitignore                     files Git must NOT save (build output, private settings)
├── docs/                          all documentation
└── AngHulingTanod/                the NetBeans project (the game)
    ├── src/atingdagat/            ALL our code and game files (the only folder we edit)
    ├── nbproject/                 NetBeans' own settings (don't edit by hand)
    ├── build.xml, manifest.mf     NetBeans' build recipe
    ├── build/                     (generated) compiled classes. Never committed
    └── dist/                      (generated) the finished JAR. Never committed
```

| Folder | Why it exists | Who edits it |
|---|---|---|
| `docs/` | So the team, the professor, and future us can understand the project | Everyone (see the owners in `05-git-workflow.md`) |
| `src/atingdagat/core/` | The "engine room": window, game loop, screens, shared numbers | Gabriel |
| `src/atingdagat/map/` | The island and sea | Gabriel |
| `src/atingdagat/entities/` | Things that exist in the game world (lighthouse, beam, ships) | Gabriel |
| `src/atingdagat/ui/` | What is drawn on top of the world (the night darkness; later the HUD) | Gabriel |
| `src/atingdagat/util/` | Small helpers used by many classes (art loader) | Gabriel |
| `src/atingdagat/data/` | Text files the game reads (the map; later waves, ship stats, high scores) | Gabriel and Gio |
| `src/atingdagat/res/` | **Not created yet.** Art and sound files. Jaron adds `ships/`, `buildings/`, `people/`, `ui/` | Jaron |

## Every code file

### `core/` (the engine room)

| File | What it does | Depends on |
|---|---|---|
| `Main.java` | Starts the program. Creates the window on Swing's own thread | `GameFrame` |
| `GameFrame.java` | The window. Resizable, centered, holds the panel | `GamePanel`, `Constants` |
| `GamePanel.java` | The game loop (60 times a second), scaling to fit the window, keys and mouse, the current screen. Starts a `GameWorld` | `GameWorld`, `Hud`, `TileMap` |
| `GameWorld.java` | One run of the game: the rules, ships, nets, resources, post, radio, clicks | Almost everything |
| `DayCycle.java` | The clock: night number, phase, how dark it is | `Phase`, `Constants` |
| `Phase.java` | The list DAY, DUSK, NIGHT, DAWN | (none) |
| `WavePlan.java` | Reads `waves.txt`: skiffs per night and how often | `Constants` |
| `GameState.java` | The list of screens: MENU, PLAY, PAUSE, GAME_OVER, VICTORY | (none) |
| `Constants.java` | Shared numbers: screen size, tile size, FPS, nights, lighthouse position, map file | (none) |

### `map/`

| File | What it does | Depends on |
|---|---|---|
| `TileMap.java` | Loads `island.txt` into a grid, works out water depth, draws land, water, and foam, and answers `isLand(row, col)` | `Constants` |

### `entities/`

| File | What it does | Depends on |
|---|---|---|
| `Lighthouse.java` | Where the tower stands, where the lamp is, how it looks | `Constants` |
| `Beam.java` | The turning cone of light. Answers "is this point lit?" | (none) |
| `Warship.java` | **Abstract parent** of all enemy ships: moving, being lit, the wake | `TileMap`, `Beam`, `Constants` |
| `Cutter.java` | Fast, fragile ship. Extends `Warship` | `Warship`, `Assets` |
| `Barge.java` | Slow, tough ship that hurts the post most. Extends `Warship` | `Warship`, `Assets` |
| `Skiff.java` | The first enemy ship. Extends `Warship`. Draws its sprite or a placeholder | `Warship`, `Assets` |
| `Post.java` | The grounded ship that is the island's command post. Has health | `Constants` |
| `Radio.java` | The signal bar. Weakens when ships land | (none) |

### `defense/`

| File | What it does | Depends on |
|---|---|---|
| `Defense.java` | The interface every defense promises to follow | (none) |
| `Net.java` | A bamboo net on shallow water. Ships stop and cut it | `Defense`, `Constants` |
| `Mine.java` | A mine buoy. Explodes when a ship comes close | `Defense`, `Warship` |

### `economy/`

| File | What it does | Depends on |
|---|---|---|
| `ResourceType.java` | Every resource as one line of data: bamboo, 4 ores, 5 fish (name, color, hold time, yield, meals, regrow) | (none) |
| `Harvestable.java` | The interface for anything you can hold the mouse on to collect | (none) |
| `FishKind.java` | What makes each fish different: speed, depth, how common, school size | `ResourceType` |
| `FishSchool.java` | A swimming school of fish. Wanders, stays at its depth, fades away | `FishKind`, `TileMap` |
| `Inventory.java` | How much of each the player has. `spend()` refuses if too little. `spendMeals()` eats cheap fish first | `ResourceType` |
| `ResourceNode.java` | A bamboo grove or ore vein on land. Has limited units. Loads from `nodes.txt` | `Harvestable`, `TileMap`, `Constants` |

### `ui/`

| File | What it does | Depends on |
|---|---|---|
| `NightOverlay.java` | Paints the darkness (stronger at night, lighter at dusk and dawn) and cuts the beam-shaped hole in it | `Beam`, `Constants` |
| `Hud.java` | The top bar (night, resources, post and radio bars), the hint line, the phase banner | `GameWorld` |
| `Popup.java` | Floating text like "+2 Bamboo" | (none) |
| `Explosion.java` | The expanding ring when a mine goes off | (none) |

### `util/`

| File | What it does | Depends on |
|---|---|---|
| `Assets.java` | Loads a PNG from inside the project, shrinks it, removes the magenta background, remembers it | (none) |

### `data/`

| File | What it does |
|---|---|
| `island.txt` | The island map, 64 columns by 36 rows (10 px tiles). `~` water, `:` sand, `#` grass. Edit it in Notepad |
| `waves.txt` | For each night: skiffs, cutters, barges, and seconds between ships |
| `nodes.txt` | Where bamboo, trees and the ore veins are: `TYPE column row units` (`TREE` is a choppable tree). Fish are not in the file |

## Where to change a number (tuning cheat sheet)

Gio: this is the "make it easier or harder" list. Ask Gabriel before changing, then playtest.

| You want to... | File | Name of the number | Now |
|---|---|---|---|
| Make the beam wider or narrower | `entities/Beam.java` | `HALF_ANGLE` | 16 degrees |
| Make the beam reach further | `entities/Beam.java` | `LENGTH` | 240 pixels |
| Make the beam turn faster | `entities/Beam.java` | `TURN_SPEED` | 0.045 |
| Change the always-lit circle around the lamp | `entities/Beam.java` | `LAMP_GLOW` | 44 pixels |
| Make the night darker or brighter | `ui/NightOverlay.java` | `NIGHT` (last number = darkness) | 232 of 255 |
| Make a night easier or harder | `data/waves.txt` | skiffs and seconds per night | see the file |
| Move or add bamboo and ore spots, or change how much a vein holds | `data/nodes.txt` | the rows (last number = units) | see the file |
| How long a resource takes to harvest | `economy/ResourceType.java` | the "hold" number on its line | bamboo 90, ores 150 to 240, fish 40 to 140 (frames) |
| How much you get per harvest, or how fast bamboo regrows | `economy/ResourceType.java` | "yield" and "regrow" | bamboo gives 2, regrows 1 unit per 25 s |
| Fish speed, depth, how common, school size | `economy/FishKind.java` | the numbers on each line | see the file |
| How many fish schools at once, how often they appear | `core/Constants.java` | `MAX_FISH_SCHOOLS`, `FISH_SPAWN_FRAMES` | 9, every 3 s |
| What an ore buys | `core/Constants.java` | `MINE_COST_NICKEL`, `REWIRE_COST_COPPER`, `ARMOR_COST_CHROMITE`, `LAMP_COST_GOLD` and the nearby numbers | 2, 2, 3, 2 |
| Make the day longer or shorter | `core/Constants.java` | `DAY_FRAMES`, `DUSK_FRAMES`, `DAWN_FRAMES` | 50 s, 8 s, 7 s |
| Change the starting bamboo | `core/Constants.java` | `START_BAMBOO` | 6 |
| Net price (bamboo) | `defense/Net.java` | `COST_BAMBOO` | 3 |
| How deep a net can go | `core/Constants.java` | `NET_MAX_DEPTH` | 3 tiles |
| How hard a landing hurts the post | `entities/Skiff.java`, `Cutter.java`, `Barge.java` | `landingDamage` (skiff keeps the default in `Warship`) | 3, 2, 6 |
| How much a landing weakens the radio | `core/Constants.java` | `RADIO_PER_POST_DAMAGE` | 3 times the post damage |
| Mine strength | `defense/Mine.java` | `BLAST_RADIUS`, `BLAST_DAMAGE` | 30 px, 3 |
| How far out mines can go | `core/Constants.java` | `MINE_MAX_DEPTH` | 5 tiles |
| Cutter or barge speed and health | `entities/Cutter.java`, `Barge.java` | the numbers in `super(...)` | 1.0 / 2 and 0.3 / 8 |
| Repair price and strength | `core/Constants.java` | `REPAIR_FISH_COST`, `REPAIR_POST_AMOUNT`, `REPAIR_RADIO_AMOUNT` | 2 meals, +4, +15 |
| How fast you can fire | `core/Constants.java` | `GUN_COOLDOWN` | 30 frames |
| How long a ship takes to cut a net | `entities/Warship.java` | `ATTACK_INTERVAL` | 45 frames per hit, 4 hits |
| Make skiffs faster | `entities/Skiff.java` | the speed in `super(...)` | 0.55 |
| Make skiffs tougher | `entities/Skiff.java` | the health in `super(...)` | 3 |
| How long a ship stays visible after the beam leaves | `entities/Warship.java` | the `0.02` in `updateLight` | about 1 s |
| Move the lighthouse | `core/Constants.java` | `LIGHTHOUSE_COL`, `LIGHTHOUSE_ROW` | 19, 10 |
| Change the window title | `core/Constants.java` | `TITLE` | Ang Huling Tanod |
| Change the number of nights | `core/Constants.java` | `TOTAL_NIGHTS` | 7 |
| Redraw the island | `data/island.txt` | the letters | see the file |

## Art files (what the code expects)

| File to add | Used by | Size in the game | Notes |
|---|---|---|---|
| `res/ships/skiff.png` | `Skiff` | 32 x 16 pixels | Ship points **right**. Magenta background. Already wired up |
| `res/ships/cutter.png`, `barge.png` | planned | to decide | Wired when the classes exist |
| `res/buildings/lighthouse.png` | planned | about 32 x 48 | Not wired yet. The lighthouse is drawn with shapes |
| `res/people/villager.png` | planned | about 16 x 24 | Not wired yet |

Full prompts and the style rules are in [07-art-prompts.md](07-art-prompts.md). If a file is missing, the game draws a
placeholder and prints a short message. It never crashes.

## What is in the docs folder

| File | Purpose | Main author |
|---|---|---|
| `01-game-design.md` | What the game is | Gabriel |
| `02-project-plan.md` | Who does what, and when | Gabriel |
| `03-code-guide.md` | How and why the code works (study guide) | Gabriel |
| `04-pitch-and-demo.md` | The demo script | Gio |
| `05-git-workflow.md` | Team rules for Git | Gabriel |
| `06-playtest-log.md` | Playtest results | Gio |
| `07-art-prompts.md` | Gemini style bible and prompts | Jaron |
| `08-file-reference.md` | This file | Gabriel |
| `09-build-story.md` | Why we built it step by step, for beginners | Gabriel |

## Things that are NOT part of the project

- `Claude outputs/` (if it appears next to `docs/`): a copy of a file the chat app saved. Don't commit it. Delete it.
- `build/` and `dist/`: made by NetBeans every time you run or build. Git ignores them.
- `nbproject/private/`: your personal NetBeans settings. Git ignores it.
