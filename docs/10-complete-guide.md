# Complete Beginner Guide to Ang Huling Tanod

*One document that explains the whole project from zero: the tools, every folder and why it exists, how the game works,
how to change it, and what to do when it breaks. If you only read one file before the demo, read this one.
For the feature-by-feature code explanation see [03-code-guide.md](03-code-guide.md).*

---

## Part 1. The big picture in one minute

**The game:** you are the last *tanod* (guardian) of Isla Dulo. By **day** you gather bamboo, ore and fish (by **holding** the mouse, and fish swim away), and build nets and
mines. By **night** the island is dark and only your **lighthouse beam** shows anything. Enemy ships sail in. You must sink or stall
them so the **post** and the **radio** survive 7 nights. If the radio keeps broadcasting "May natitira pa tayo", you win.

**You are not alone.** Survivors reach the island by bangka (Ka Inday, Migo, Tonyo) and work for you by day. Spirits from Filipino folklore can become your allies if you
show respect: a **duwende** in an anthill, a stone statue of **Lapu-Lapu**, and a **manananggal** who flies at night. Tools can be upgraded in the Workshop.

**How it is built:** plain **Java** with **Swing and AWT** (the drawing and window tools that come with Java). No game engine and no
outside libraries. Everything on screen is drawn by our code, 60 times every second.

**The three ideas that run everything:**
1. **The game loop.** A timer fires about 60 times a second. Each time: *update* (move things, apply rules), then *draw* (paint the picture).
2. **Objects.** Each kind of thing (ship, net, lighthouse) is a **class** with its own data and behavior. Many ships are many *objects* of the same class.
3. **Data in files.** The map, waves, and resource spots are text files. We change the game by editing text, not code.

---

## Part 2. The tools, and why we chose them

| Tool | What it is | Why we use it |
|---|---|---|
| **Java (JDK 26)** | The programming language and its toolkit | What the class uses |
| **Swing and AWT** | Java's built-in window and drawing library | The class requirement. `JFrame`, `JPanel`, `Graphics2D` |
| **NetBeans** | The editor (IDE) where we write, run, and build | We use it in class. Press **F6** to run |
| **Ant** | NetBeans' build system (the project type) | Plain Java projects use it. It makes the JAR |
| **Git** | Remembers every version of every file | Backup, history, teamwork |
| **GitHub** | Website that stores the Git repo online | Three of us share one project |
| **Gemini** | AI image tool | Makes the art. See `07-art-prompts.md` and `CREDITS.md` |
| **Claude** | AI assistant | Helped write code and docs. Disclosed in `CREDITS.md` |

---

## Part 3. Every folder, and why it exists

```
Ang-Huling-Tanod/                      THE REPOSITORY (the box Git watches; what GitHub shows)
├── README.md                          The front page: what it is, how to run, controls
├── CREDITS.md                         Who made what, asset licenses, AI disclosure
├── .gitignore                         A list of files Git must NOT save
├── docs/                              All the documentation (you are here)
└── AngHulingTanod/                    THE NETBEANS PROJECT (the game itself)
    ├── src/                           ALL our source: the only folder we edit
    │   └── atingdagat/                Our package root (the game's name, no spaces)
    │       ├── core/                  The engine room
    │       ├── map/                   The ground
    │       ├── entities/              Things that exist in the world
    │       ├── defense/               Things the player builds
    │       ├── economy/               Resources and what you carry
    │       ├── ui/                    What is drawn on top of the world
    │       ├── util/                  Small helpers
    │       ├── data/                  Text files the game reads
    │       └── res/                   Art and sound (added by Jaron)
    ├── nbproject/                     NetBeans' own settings (do not edit by hand)
    ├── build.xml                      The recipe NetBeans uses to build
    ├── manifest.mf                    Says which class starts the JAR
    ├── build/                         GENERATED: compiled .class files. Never commit
    └── dist/                          GENERATED: the final .jar. Never commit
```

### Why a *repository* folder and a *project* folder?

The **repository** is what Git tracks. It holds the game *and* the docs. The **project** is the NetBeans part, only the game. Putting
the project inside the repo means one `git push` saves everything.

### Why `src/` holds the data and art, not just code

NetBeans packs **everything inside `src/`** into the JAR. If the map were at `C:\Users\...` it would not exist on the professor's
computer. Inside `src/` it travels with the game. The game finds it with `getResourceAsStream("/atingdagat/data/island.txt")`.

### What is a *package*?

A package is a folder of related classes. The first line of every `.java` file says which one it belongs to, for example
`package atingdagat.entities;`. One class, one job. One folder per kind of job.

| Package | Think of it as | Classes inside |
|---|---|---|
| `core` | The engine room | `Main`, `GameFrame`, `GamePanel`, `GameWorld`, `DayCycle`, `Phase`, `WavePlan`, `GameState`, `Constants` |
| `map` | The ground | `TileMap` |
| `entities` | The actors | `Lighthouse`, `Beam`, `Warship` (abstract), `Skiff`, `Cutter`, `Barge`, `Post`, `Radio` |
| `defense` | What you build | `Defense` (interface), `Net`, `Mine` |
| `economy` | What you collect | `ResourceType`, `ResourceNode`, `FishSchool`, `FishKind`, `Harvestable`, `Inventory` |
| `ui` | Screen layers | `NightOverlay`, `Hud`, `Popup`, `Explosion` |
| `util` | The toolbox | `Assets` |

The per-file table (what each file does and what it depends on) is in [08-file-reference.md](08-file-reference.md).

### The data files (`src/atingdagat/data/`)

| File | Format | Edit it to |
|---|---|---|
| `island.txt` | 64 columns x 36 rows of `~` water, `:` sand, `#` grass | Redraw the island |
| `waves.txt` | `night skiffs cutters barges seconds` per line | Make nights easier or harder |
| `nodes.txt` | `TYPE column row units` per line (`BAMBOO`, `TREE`, `NICKEL`, `COPPER`, `CHROMITE`, `GOLD`) | Move or add spots, or change how much a vein holds |

Lines starting with `#` are comments. A bad line is skipped with a message, and a missing file uses defaults. The game does not crash.

### The art folder (`src/atingdagat/res/`)

Jaron's PNGs. The exact file names and sizes are in [07-art-prompts.md](07-art-prompts.md). If a file is missing the game draws a
simple shape instead, so art can arrive piece by piece.

---

## Part 4. What happens, in order, from double-click to gameplay

1. **`Main`** starts the program. It builds the window on Swing's own thread (safe).
2. **`GameFrame`** is the window. It holds one **`GamePanel`**.
3. **`GamePanel`** starts a 60-per-second **Timer**. It also loads the **`TileMap`** once.
4. In the **menu**, pressing Enter makes a new **`GameWorld`**. That is one whole run of the game.
5. Every frame the panel calls **`GameWorld.update()`** and then repaints:
   - the tanod walks, the beam sweeps (or turns by hand), the lantern follows,
   - **`DayCycle`** ticks (Day, Dusk, Night, Dawn). Each dawn can trigger a story event (a bangka, a stirring tree),
   - by day: fish swim, crates float in, survivors work, the duwende leaves ore,
   - at night, ships spawn from a shuffled queue built from `waves.txt`,
   - ships move, get lit by the beam, cut nets, trigger mines, land, and get shot by the Pana towers, Tonyo and the manananggal,
   - the post and radio take damage, and the game checks for win or loss.
6. Drawing happens in this order (first = at the back):
   **map, resource spots, fish, crates, defenses (and the Lapu-Lapu stone, the punso), boats, post, survivors, lighthouse, the tanod, ships, build preview, night darkness with a beam hole, beam glow, the Bakunawa, gun shot, explosions, popups, then the HUD and the Workshop**.
7. The picture is drawn on a small **640x360** hidden image, then scaled up to the real window. That is why the window can be resized.

---

## Part 5. The classes, in plain words

| Class | One-sentence job | Key idea it shows |
|---|---|---|
| `GamePanel` | Runs the loop, scales the screen, reads keys and mouse | Game loop, input |
| `GameWorld` | Holds one run and applies all the rules | Separating rules from window code |
| `DayCycle` and `Phase` | The clock and how dark it is | Enum, state over time |
| `WavePlan` | Reads `waves.txt` | File I/O, arrays |
| `Durable` | Things that wear and can be mended (net, torch, Pana) | Interface |
| `Raider`, `Ghost` | Ships that shoot towers / hide in the dark | Inheritance, overriding the `act` hook |
| `Flare` | The F flare light | Interfaces `Light`, `GlowSource` |
| `TileMap` | Loads the island and draws water, sand and foam | 2D arrays, file I/O |
| `Beam` | A turning cone of light. Answers "is this point lit?" | Math (angles), encapsulation |
| `NightOverlay` | Paints darkness with a beam-shaped hole | Drawing with compositing |
| `Warship` (abstract) | What all ships share: move, be lit, leave a wake, cut nets | **Inheritance**, abstract classes |
| `Skiff`, `Cutter`, `Barge` | Each ship's speed, health, damage and looks | Subclasses, **polymorphism** |
| `Defense` (interface) | The promise every defense keeps | **Interface** |
| `Net`, `Mine`, `Torch`, `PanaTower`, `LapuLapuStone` | Different defenses | Same interface, different behavior |
| `ResourceType` and `FishKind` | Every resource as a line of data. What makes each fish different | Enum with fields: data, not if-else |
| `ResourceNode`, `FishSchool` | A still bamboo or ore spot, and a swimming school of fish | **Interface** (`Harvestable`), a wandering movement rule |
| `Inventory` | What you carry, and fish counted in meals | Array indexed by enum |
| `Post`, `Radio` | What you protect | Encapsulation (private data, public methods) |
| `Hud`, `Popup`, `Explosion` | Information and effects on top | Separation of concerns |
| `Light`, `GlowSource` (interfaces) | Anything that lights a point, or makes a pool of light | **Interface**, one idea reused by the beam, lantern, torch, Ka Inday |
| `Player` | The walking tanod | Input, collision with the map |
| `Torch`, `PanaTower` | A light on the shore, and a tower that shoots in the dark | More classes behind the same `Defense` interface |
| `Upgrades` | The five tool levels and what they cost and do | One small class answering "how fast?", "how far?" |
| `MarketPanel` | The Workshop: BUILD and UPGRADES tabs, the crew strip | Separating drawing and clicking from the rules |
| `SupplyCrate` | A crate floating in with ore | A third `Harvestable` |
| `Survivor` (abstract), `KaInday`, `Migo`, `Tonyo`, `Manananggal` | The allies, each with one job. The manananggal flies and does not work for meals | **Abstract class**, **polymorphism**, overriding |
| `Bangka` | The boat that brings a survivor | A small state machine (sail, drop off, fade) |
| `LapuLapuStone` | The statue (a landmark for now) | A `Defense` that does nothing, which is fine |
| `Punso` | The duwende's anthill | `Harvestable` and `GlowSource` |
| `Bakunawa`, `EndingScene` | The eclipse event and the ending | Events driven by a timeline |
| `Assets` | Loads art, shrinks it, removes the magenta background, remembers it | Caching, missing-file safety |
| `Constants` | Every shared number in one place | No "magic numbers" |

### The rubric, and where we show it

| Rubric item (weight) | Where to point |
|---|---|
| OOP design and code quality (30%) | Abstract `Warship` and its 3 subclasses. Abstract `Survivor` and its 4 children. `Defense`, `Harvestable` and `GlowSource` interfaces, with `Punso` implementing two of them. Private fields with getters. One class, one job |
| Gameplay and mechanics (30%) | Day and night loop, beam aiming, harvesting, tool upgrades, nets, mines, torches and towers, 3 ship types, survivors and folklore allies, the eclipse, 7 nights, win and lose |
| Data structures and file I/O (15%) | `ArrayList` for ships, defenses and nodes. Arrays for the map, waves, inventory and wake. Text files for map, waves and nodes, read with try-catch |
| UI, graphics, polish (15%) | HUD bars, popups, banners, day and night lighting, resizable window, art |
| Documentation and demo (10%) | This `docs/` folder, the README, the demo script, the playtest log |

---

## Part 6. Recipes: how to change the game

### Make it easier or harder
Edit `data/waves.txt`. Fewer ships or longer gaps is easier. No recompiling is needed, but rerun the game.

### Move a bamboo or ore spot
Edit `data/nodes.txt`. Bamboo and ore must be on land. Fish are not in this file because they swim by themselves. Check the column and row against `island.txt`.
(Column = how far right, starting at 0. Row = how far down.)

### Change how long the day is
`core/Constants.java`: `DAY_FRAMES` (75 seconds now), `DUSK_FRAMES`, `DAWN_FRAMES`. 60 frames = 1 second.

### Add a new enemy ship (for example, a Frigate)
1. Copy `Cutter.java` to `Frigate.java`. Change the class name, the numbers in `super(...)`, `landingDamage`, the name, and the shape.
2. In `WavePlan.java`, add `FRIGATE` to the `Kind` enum and read one more column. (Or use `waves.txt` only if you keep the column count.)
3. In `GameWorld.spawnShip`, add a `case FRIGATE:`.
4. Add the sprite path `res/ships/frigate.png` and its size.

### Add a new defense
1. Make a class that `implements Defense`. Fill in its five methods (and `update` if it should act on its own).
2. Add a value to the `Tool` enum in `GameWorld`, a key in `GamePanel.handleKeyPress`, a `placeXxx` method, and a line in `Hud.drawHint`.

### Add a new ore or a new fish
- **Ore:** add one line to `ResourceType` (copy the `COPPER` line and change the name, color and numbers), put it in `nodes.txt`, and decide its use in `GameWorld` and `Constants`.
- **Fish:** add one line to `ResourceType` and one line to `FishKind` (speed, depth range, how common). It will start swimming on its own.

### Add a new ally (for example, a tikbalang)
1. If it **walks or flies** and helps, make a class that `extends Survivor` (copy `Manananggal`). Fill in `act()` and the story text. If it **stays in one place and fights**, make a class that `implements Defense` (copy `LapuLapuStone`).
2. Add the night it arrives to `Constants` and one `if` in `GameWorld.monsterEvent`.
3. Add a line to the story, the README and these docs. Art is optional.

### Add a tool upgrade
Add a value to the `Gear` enum in `Upgrades.java` (key, name, ore, description), an effect method, and one line in `GamePanel` for its key. The Workshop row appears by itself.

### Change an upgrade price or how strong it is
`economy/Upgrades.java`: `BAMBOO_COST`, `ORE_COST`, `WORK_SAVING`, `CAST_SAVING`, `BOOT_BONUS`, `RIFLE_RANGE_BONUS`.

### Add art
Save the PNG with the exact name in `res/`. See `07-art-prompts.md`. No code changes.

---

### How the ending story works
The radio voices live in `data/voices.txt` (one line per night: island, speaker, what you hear, what they say at the end). `RadioVoices` reads the file; dusk shows the voice and `EndingScene` answers it. To change the story, edit the file in Notepad.

### What makes the nights harder
`GameWorld.spawnShips()` sends ships in pulses (`pulseSize(night)`), each from a different side. `Twist.forNight(n)` gives the night its twist (fog, static, surge, hardened, assault). To add a twist: add a line to the `Twist` enum with a title and a story line, then handle it where the effect happens (the beam for fog, `spawnShip` for speed and health). To make the game easier or harder without coding, edit `data/waves.txt`.

## Part 7. Git in plain words

| Word | Meaning |
|---|---|
| **Commit** | A saved snapshot on **your** computer, with a message |
| **Push** | Send your commits up to GitHub |
| **Pull** | Get the commits others pushed |
| **Clone** | Make your own copy of the repo |
| **Conflict** | Two people changed the same lines. Avoided by each person editing their own files |

**Daily routine:** pull, work, commit with a clear message, push. Team rules are in [05-git-workflow.md](05-git-workflow.md).
**A commit is not on GitHub until you push.** This confused us once, so remember it.

---

## Part 8. When something goes wrong

| What you see | Likely cause | Fix |
|---|---|---|
| Red underlines after pulling | NetBeans has not rebuilt | Clean and Build (Shift+F11) |
| `Art not found (using placeholder)` in the output | A sprite file is missing | Normal. Add the PNG or ignore it |
| `file not found ... using defaults` | A data file is missing or misnamed | Check the file is in `src/atingdagat/data/` |
| `skipped a bad line` | A typo in `waves.txt` or `nodes.txt` | Fix that line |
| The game is too dark or too bright | `NIGHT_ALPHA` in `NightOverlay` | Change the number |
| A ship hides forever | It is not in the beam | Aim at it. Dark is the point |
| Cannot place a net | Water too deep, tile taken, or not enough bamboo | Read the popup. Nets: depth 1 to 3 |
| JAR does not run on another PC | Java version or a missing data file | Run `java -jar` and read the error. Test before Nov 29 |
| Pink edges on art | The object contains magenta, pink or purple | Redo the art without those colors |

---

## Part 9. Glossary

- **Class**: a blueprint. **Object**: one thing made from it.
- **Inheritance**: a child class gets everything from a parent class (`Cutter` extends `Warship`).
- **Abstract class**: a parent you cannot create directly. It forces children to fill in the gaps.
- **Interface**: a list of abilities a class promises to have (`Defense`).
- **Polymorphism**: treating different kinds the same way (a list of `Warship` can hold skiffs and barges, a list of `Survivor` holds walkers and a flyer).
- **Story event**: something that happens once at a chosen dawn (a bangka lands, the Lapu-Lapu story is told).
- **Ally**: a character on your side. Ours are survivors and folklore spirits.
- **Punso**: an anthill. In folklore a duwende may live in it. **Lapu-Lapu**: the Visayan chieftain who stopped Magellan's men; in our story he is turned to stone, not killed.
- **Headless test**: a small program that runs the game with no window and prints numbers, so we can check rules fast.
- **Encapsulation**: keep data `private` and let the outside use only methods.
- **Enum**: a fixed list of names (`Phase.NIGHT`).
- **ArrayList**: a list that grows. **Array**: a fixed-size list.
- **Frame**: one pass of the loop (1/60 second).
- **Tile**: one square of the map (10 x 10 pixels). The map is 64 x 36 tiles.
- **Sprite**: a picture of a thing. **Placeholder**: a simple shape drawn until the sprite exists.
- **Chroma key**: removing a background color (our magenta) to make it transparent.
- **Package**: a folder of related classes.
- **JAR**: the single file that holds the finished game.
- **Commit, push, pull, clone**: see Part 7.

---

## Part 10. Where to read next

| If you want to... | Read |
|---|---|
| Know what the game is | [01-game-design.md](01-game-design.md) |
| See the schedule and roles | [02-project-plan.md](02-project-plan.md) |
| Understand each feature's code | [03-code-guide.md](03-code-guide.md) |
| Prepare the demo | [04-pitch-and-demo.md](04-pitch-and-demo.md) |
| Use Git without conflicts | [05-git-workflow.md](05-git-workflow.md) |
| Record playtests | [06-playtest-log.md](06-playtest-log.md) |
| Make art | [07-art-prompts.md](07-art-prompts.md) |
| Find a file or number | [08-file-reference.md](08-file-reference.md) |
| Know why we built it this way | [09-build-story.md](09-build-story.md) |
