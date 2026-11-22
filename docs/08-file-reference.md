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
| `src/atingdagat/entities/` | Things that exist in the game world (lighthouse, beam, ships, the tanod, survivors, folklore allies) | Gabriel |
| `src/atingdagat/ui/` | What is drawn on top of the world: night darkness, HUD, the Workshop panel, the ending | Gabriel |
| `src/atingdagat/util/` | Small helpers used by many classes (art loader) | Gabriel |
| `src/atingdagat/data/` | Text files the game reads (the map, waves, resource spots; later high scores) | Gabriel and Gio |
| `src/atingdagat/res/` | Art (PNG) in subfolders: `ships/`, `fish/`, `player/`, `nodes/`, `buildings/`, `items/`, `survivors/`, `monsters/`, `ui/`. Every file is optional: if one is missing the code draws a shape. Sound is not added yet | Jaron |

## Every code file

### `core/` (the engine room)

| File | What it does | Depends on |
|---|---|---|
| `Main.java` | Starts the program. Creates the window on Swing's own thread | `GameFrame` |
| `GameFrame.java` | The window. Resizable, centered, holds the panel | `GamePanel`, `Constants` |
| `GamePanel.java` | The game loop (60 times a second), scaling to fit the window, every key and the mouse, the current screen. Starts a `GameWorld`. Test keys: J, K, H, L, N, E | `GameWorld`, `Hud`, `TileMap` |
| `GameWorld.java` | One run of the game: the rules, ships, nets, resources, post, radio, clicks | Almost everything |
| `DayCycle.java` | The clock: night number, phase, how dark it is | `Phase`, `Constants` |
| `Phase.java` | The list DAY, DUSK, NIGHT, DAWN | (none) |
| `Durable.java` | Interface: durability, wear, repair cost | Net, Torch, PanaTower |
| `Raider.java` | Ship that shoots towers and torches (`act` hook) | `Warship` |
| `Ghost.java` | Ship that is slow to show, quick to vanish | `Warship` |
| `Flare.java` | The F flare light | `Light`, `GlowSource` |
| `WavePlan.java` | Reads `waves.txt`: skiffs per night and how often (columns 6 and 7: raiders, ghosts) `Constants` |
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
| `Beam.java` | The turning cone of light (380 px long, 21 degrees each side). Lamp upgrades make it longer and wider. Answers "is this point lit?" | (none) |
| `Light.java` | The interface for anything that lights a point (beam, lantern) | (none) |
| `GlowSource.java` | The interface for anything that makes a round pool of light at night (lantern, torch, Ka Inday, the punso) | (none) |
| `Lantern.java` | The tanod's own small light | `Light`, `GlowSource` |
| `Player.java` | The walking tanod: WASD, stays on land, 12 sprites. Walks faster with upgraded boots | `TileMap`, `Constants` |
| `Warship.java` | **Abstract parent** of all enemy ships: moving, being lit, the wake, hit size | `TileMap`, `Beam`, `Constants` |
| `Skiff.java` | The first enemy ship. Extends `Warship` | `Warship`, `Assets` |
| `Cutter.java` | Fast, fragile ship. Extends `Warship` | `Warship`, `Assets` |
| `Barge.java` | Slow, tough ship that hurts the post most. Extends `Warship` | `Warship`, `Assets` |
| `Bakunawa.java` | The moon-eating dragon event and the Kordan flagship. The eclipse puts the lamp out | `Beam`, `Assets` |
| `Post.java` | The grounded ship that is the island's command post. Has health and armor levels | `Constants` |
| `Radio.java` | The signal bar. Weakens when ships land | (none) |
| `Survivor.java` | **Abstract parent** of the allies who walk or fly: walking home, day work (chop, carry), drawing | `TileMap`, `ResourceNode`, `Assets` |
| `KaInday.java` | Babaylan survivor (after night 2). Heals the post and radio. Glows | `Survivor`, `GlowSource` |
| `Migo.java` | Radio-nerd survivor (after night 4). Keeps the signal up, brings crates faster | `Survivor` |
| `Tonyo.java` | Sentry survivor (after night 6). Shoots lit ships | `Survivor` |
| `Manananggal.java` | The flying ally (after night 5). Swoops on ships at night. Does not work for meals | `Survivor`, `GlowSource` |
| `Punso.java` | The duwende's anthill on the west shore. After a gift of bamboo he leaves ore every morning | `Harvestable`, `GlowSource` |
| `Bangka.java` | The fishing boat that carries a survivor to the shore at dawn | `Survivor` |

### `defense/`

| File | What it does | Depends on |
|---|---|---|
| `Defense.java` | The interface every defense promises to follow | (none) |
| `Net.java` | A bamboo net on shallow water. Ships stop and cut it | `Defense`, `Constants` |
| `Mine.java` | A mine buoy. Explodes when a ship comes close | `Defense`, `Warship` |
| `Torch.java` | A torch post that lights the shore all night (up to 20) | `Defense`, `GlowSource` |
| `PanaTower.java` | A bamboo arrow tower that shoots ships even in the dark (up to 10) | `Defense`, `Warship` |
| `RadioVoices.java` (in `core/`) | Reads `data/voices.txt` into a list of voices (night, island, speaker, lines) | (none) |
| `HelpScreen.java` (in `ui/`) | The one-page How to Play screen | (none) |
| `LapuLapuStone.java` | The stone statue of Lapu-Lapu on the northeast islet. A landmark with a story for now; implements `Defense` so the world draws it | `Defense` |
| `Twist.java` (in `core/`) | The enum of night twists: none, two fronts, fog, static, surge, hardened, assault | (enum) |

### `economy/`

| File | What it does | Depends on |
|---|---|---|
| `ResourceType.java` | Every resource as one line of data: bamboo, 4 ores, 5 fish (name, color, hold time, yield, meals, regrow) | (none) |
| `Harvestable.java` | The interface for anything you can hold the mouse on to collect | (none) |
| `FishKind.java` | What makes each fish different: speed, depth, how common, school size | `ResourceType` |
| `FishSchool.java` | A swimming school of fish. Wanders, stays at its depth, fades away | `FishKind`, `TileMap` |
| `SupplyCrate.java` | A crate that floats in from the sea with ore. Hold on it to open | `Harvestable`, `ResourceType` |
| `Upgrades.java` | The tool levels: pickaxe, axe, boots, fishing rod, rifle (0 to 3), their costs and effects | `Inventory`, `Harvestable` |
| `Inventory.java` | How much of each the player has. `spend()` refuses if too little. `spendMeals()` eats cheap fish first | `ResourceType` |
| `ResourceNode.java` | A bamboo grove or ore vein on land. Has limited units. Loads from `nodes.txt` | `Harvestable`, `TileMap`, `Constants` |

### `ui/`

| File | What it does | Depends on |
|---|---|---|
| `NightOverlay.java` | Paints the darkness (stronger at night, lighter at dusk and dawn) and cuts the beam-shaped hole in it | `Beam`, `Constants` |
| `Hud.java` | The top bar (night, resources, post and radio bars), the hint line, the phase banner | `GameWorld` |
| `MarketPanel.java` | The WORKSHOP on the left: BUILD and UPGRADES tabs, clickable rows, the crew strip | `GameWorld` |
| `EndingScene.java` | The ending: lights across the sea, the helicopter, the title card | `TileMap`, `Survivor` |
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
| `waves.txt` | For each night: skiffs, cutters, barges, seconds between ships, raiders, ghosts (last two optional) |
| `nodes.txt` | Where bamboo, trees and the ore veins are: `TYPE column row units` (`TREE` is a choppable tree). Fish are not in the file |

## Where to change a number (tuning cheat sheet)

Gio: this is the "make it easier or harder" list. Ask Gabriel before changing, then playtest.

| You want to... | File | Name of the number | Now |
|---|---|---|---|
| Make the beam wider or narrower | `entities/Beam.java` | `BASE_HALF_ANGLE` | 21 degrees |
| Make the beam reach further | `entities/Beam.java` | `BASE_LENGTH` | 380 pixels |
| Make the beam turn slower or faster (now instant) | `entities/Beam.java` | `TURN_SPEED` | PI (instant); try 0.045 for slow |
| Change the always-lit circle around the lamp | `entities/Beam.java` | `LAMP_GLOW` | 44 pixels |
| Make the night darker or brighter | `ui/NightOverlay.java` | `NIGHT` (last number = darkness) | 232 of 255 |
| Make a night easier or harder | `data/waves.txt` | skiffs and seconds per night | see the file |
| Move or add bamboo and ore spots, or change how much a vein holds | `data/nodes.txt` | the rows (last number = units) | see the file |
| How long a resource takes to harvest | `economy/ResourceType.java` | the "hold" number on its line | bamboo 90, ores 150 to 240, fish 40 to 140 (frames) |
| How much you get per harvest, or how fast bamboo regrows | `economy/ResourceType.java` | "yield" and "regrow" | bamboo gives 2, regrows 1 unit per 25 s |
| Fish speed, depth, how common, school size | `economy/FishKind.java` | the numbers on each line | see the file |
| How many fish schools at once, how often they appear | `core/Constants.java` | `MAX_FISH_SCHOOLS`, `FISH_SPAWN_FRAMES` | 9, every 3 s |
| What an ore buys | `core/Constants.java` | `MINE_COST_NICKEL`, `REWIRE_COST_COPPER`, `ARMOR_COST_CHROMITE`, `LAMP_COST_GOLD` and the nearby numbers | 2, 2, 3, 2 |
| Make the day longer or shorter | `core/Constants.java` | `DAY_FRAMES`, `DUSK_FRAMES`, `DAWN_FRAMES` | 75 s, 8 s, 7 s |
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
| Flare and repair | `core/Constants.java` | `FLARE_COOLDOWN`, `FLARE_LIFE`, `FLARE_RADIUS`, `FLARE_RANGE`, `REPAIR_REACH`, `REPAIR_FRAMES` | 20 s, 6 s, 70, 260, 56, 24 |
| Change the number of nights | `core/Constants.java` | `TOTAL_NIGHTS` | 7 |
| How many torches or Pana towers you may build | `core/Constants.java` | `TORCH_MAX`, `PANA_MAX` | 20, 10 |
| Upgrade prices and effects | `economy/Upgrades.java` | `BAMBOO_COST`, `ORE_COST`, `WORK_SAVING`, `CAST_SAVING`, `BOOT_BONUS`, `RIFLE_RANGE_BONUS` | 4/6/8 bamboo, 0/1/1 ore |
| How far the gun shoots (before Rifle upgrades) | `core/Constants.java` | `FIRE_RANGE` | 200 pixels |
| Survivors' day work | `core/Constants.java` | `WORKER_WAGE_MEALS`, `WORKER_QUOTA`, `WORKER_CHOP_FRAMES` | 1 meal, 3 loads, 4 s |
| The radio voices and the ending story | `data/voices.txt` | one line per night | 7 voices |
| How hard the screen shakes | `core/GameWorld.java` | the `addShake(power, frames)` calls | 2.5 to 4.5 px |
| When the Lapu-Lapu story appears | `core/Constants.java` | `STATUE_NIGHT` | night 3 |
| Pulse size and night twists | `core/GameWorld.java`, `core/Twist.java` | `pulseSize()`, `Twist.forNight()` | 1,1,2,2,3,3,4 |
| The manananggal | `core/Constants.java` | `MANANANGGAL_NIGHT`, `MANANANGGAL_RANGE`, `MANANANGGAL_DAMAGE` | night 5, 190 px, 2 |
| The duwende | `core/Constants.java` | `DUWENDE_NIGHT`, `DUWENDE_GIFT_BAMBOO`, `PUNSO_COL`, `PUNSO_ROW` | night 1, 3 bamboo, (15, 28) |
| Where the Lapu-Lapu stone stands | `core/Constants.java` | `STATUE_COL`, `STATUE_ROW` | (52, 8) |
| Redraw the island | `data/island.txt` | the letters | see the file |

## Art files (what the code expects)

Every file is optional. If one is missing the game draws a shape and prints a short message. Use a **real PNG** (not WebP) with a flat magenta (255,0,255) background. The size is what the game scales it to.

| File | Used by | Size in the game | Status |
|---|---|---|---|
| `res/ships/skiff.png`, `cutter.png`, `barge.png` | the three ships | 38x12, 40x12, 58x25 | Done (Jaron). Ships point **right** |
| `res/ships/flagship.png` | the Kordan flagship in the eclipse | 100x50 | Done (Jaron) |
| `res/player/` (`up`, `down`, `left`, `right`, each with `2` and `3` for the walk) | the tanod | 12 files | Done |
| `res/fish/<name>.png` | the 5 fish (`tamban`, `galunggong`, `bangus`, `tuna`, `lapulapu`) | set per fish in `FishKind` | Done |
| `res/nodes/<name>.png` (`bamboo`, `ore`, `tree`) | resource spots. One `ore.png` is tinted into all four ores | 16x16, tree 20x28 | Placeholders |
| `res/ui/<name>.png` (`bamboo`, `fish`, `nickel`, ...) | HUD icons | 10x10 | Placeholders |
| `res/buildings/` (`lighthouse`, `post`, `net`, `mine`, `torch`, `pana`) | the buildings | lighthouse and post see the classes, net 10x10, mine 16x16, torch 12x16, pana 14x20 | Mostly placeholders |
| `res/items/crate.png` | the supply crate | 16x16 | Placeholder |
| `res/survivors/<name>.png` (`kainday`, `migo`, `tonyo`) | the three survivors | 18x22 | Placeholder (code-drawn) |
| `res/monsters/lapulapu_stone.png` | the Lapu-Lapu stone statue | 28x48 | Done. Gemini art, cropped and saved at 157x240; the game scales it down. The code draws a placeholder if it is missing |
| `res/nodes/acacia.png`, `mango.png`, `coconut.png`, `banana.png` | the other trees (`tree.png` is narra) | 20x28 | Optional. The code draws each |
| `res/ui/fruit.png` | fruit HUD icon | 10x10 | Optional |
| `res/monsters/manananggal.png` | the manananggal | 36x40 | Done. Gemini art, cropped and saved at 216x240; one still image. The code draws her if it is missing |
| `res/monsters/punso.png` | the duwende's anthill | 22x22 | Optional. The code draws it |

Full prompts and the style rules are in [07-art-prompts.md](07-art-prompts.md).

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
| `10-complete-guide.md` | The one-file beginner guide to the whole project | Gabriel |

## Things that are NOT part of the project

- `Claude outputs/` (if it appears next to `docs/`): a copy of a file the chat app saved. Don't commit it. Delete it.
- `build/` and `dist/`: made by NetBeans every time you run or build. Git ignores them.
- `nbproject/private/`: your personal NetBeans settings. Git ignores it.
