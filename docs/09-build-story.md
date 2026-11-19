# How We Built It, Step by Step (for beginners)

*Read this if you ask: "Why is the project set up like this?" Every step says **what we did**, **why**, and **what would
have gone wrong without it**. No experience needed. When a word is new, it is explained once, right where it first
appears.*

---

## Step 1. We wrote the plan before the code

**What we did:** made the game design and project plan first (`docs/01` and `docs/02`).

**Why:** the first idea sounded like "a rally, not a game". Writing down the story, the night loop and the enemies made the
game real on paper. Now Gio and Jaron can see what they are helping to build, and we can say "that's a must-have, that's
a nice-to-have".

**Without it:** three people would build three different games, and we would run out of time deciding what to cut.

---

## Step 2. We made a GitHub repository and cloned it

**What we did:** created the repo on GitHub, and used NetBeans (Team > Git > Clone) to copy it to the laptop.

**Why:**
- **Repository (repo):** a project folder that remembers every version of every file.
- **GitHub:** a website that stores the repo online, so three people can share it.
- **Clone:** make your own working copy of the online repo.
- It is also our **backup**. If the laptop dies, the game isn't lost. It is also **proof of who did what**, which the
  README's member contributions need.

**Without it:** we would send zip files in the group chat, overwrite each other's work, and have no way to go back to
yesterday's working version.

**Why the first clone showed no branches:** an empty repo has no versions yet. A branch only exists after the first
commit, so we added a README on GitHub first.

---

## Step 3. We made the NetBeans project inside the cloned folder

**What we did:** File > New Project > Java Application, placed inside the repo folder, with no main class.

**Why inside the repo:** so Git can see the code. The repo folder is the "box" Git watches. The project is the game.

**Why a plain Java (Ant) project:** it's what we have used in class, it needs no extra setup, and it makes a runnable
JAR with **Clean and Build**.

**Without it:** the code would sit outside Git, and the JAR step at the end would be a scramble.

---

## Step 4. We added a `.gitignore`

**What we did:** a small file that lists what Git must **not** save: `build/`, `dist/`, `nbproject/private/`.

**Why:** those are generated, or are only about your own computer. If we saved them, every build would show up as a
change, and Gio's NetBeans settings would overwrite yours.

**Without it:** merge conflicts in files nobody wrote.

---

## Step 5. We split the code into packages (folders)

**What we did:** `core`, `map`, `entities`, `ui`, `util`, all inside `atingdagat`.

**Why:** a **package** is a folder of related classes. One huge file is impossible to understand and impossible to share.
So the rule is: **one class, one job**, and **one folder per kind of job**.

| Folder | Think of it as |
|---|---|
| `core` | The engine room: window, loop, screens |
| `map` | The ground |
| `entities` | The actors |
| `ui` | The screen overlays |
| `util` | The toolbox |

**Why it matters for grades:** OOP design and code quality is worth 30%. Clear classes in clear folders is what that
looks like.

**Without it:** `GamePanel` would grow to 2,000 lines, and finding "where does the beam turn?" would take ten minutes.

---

## Step 6. The skeleton: `Main`, `GameFrame`, `GamePanel`, `GameState`, `Constants`

**What we did:** the smallest program that opens a window, loops, and can change screens.

**Why first, before any art or gameplay:**
- If the loop is wrong, everything built on top is wrong, so we test the base first.
- It gave us something to **see** on day one. Colored shapes are enough to know the loop works.
- Each class has one job: `Main` starts, `GameFrame` is the window, `GamePanel` runs and draws, `GameState` lists screens,
  `Constants` holds shared numbers.

**Why `Constants`:** a number like `360` in the middle of the code is a mystery. `INTERNAL_HEIGHT` explains itself, and
we change it in one place.

**Why a `Timer` for the loop:** it was taught in class, and it keeps everything on Swing's own thread, so we can't draw
from two places at once.

---

## Step 7. A small screen (640x360) that scales up

**What we did:** draw the whole game on a small hidden picture, then stretch it to the window.

**Why:**
- **A resizable window for free.** Game positions never depend on the window size.
- **Sharp pixel art.** Enlarging with "nearest neighbor" makes crisp squares, not blur.
- **Simple math.** 16-pixel tiles fit exactly 40 across.

**Without it:** resizing the window would break the layout, the mouse position, and the art.

---

## Step 8. The map in a text file (`data/island.txt`) and `TileMap`

**What we did:** one letter per tile in a text file, and a class that reads it, shades the water by depth, and draws foam.

**Why a text file, not code:**
- Anyone can redraw the island in Notepad, with no Java knowledge. That helps Gio and Jaron help.
- It is the **file I/O** the rubric asks for (15% for data structures and file I/O).

**Why a folder called `data/`:** it separates "things the game reads" from "code". Later it will also hold wave plans and
ship stats, so balance changes don't need a recompile.

**Why the file is inside `src/`:** NetBeans packs everything in `src` into the JAR. A map stored at `C:\...` would not exist
on the professor's computer.

**Why a fallback island in code:** a missing file should print a warning and not crash the demo.

**Why plain colors:** it lets us test the map tonight, and art can replace it later.

---

## Step 9. The lighthouse beam (`Lighthouse`, `Beam`)

**What we did:** a lighthouse with a lamp, and a cone of light that turns toward the mouse, at a limited speed.

**Why this is the first gameplay:** the beam is what makes the game different from every other tower defense. If it isn't
fun to steer, nothing else saves the game, so we build it first.

**Why it turns slowly:** if it followed the mouse instantly there would be no decision. The delay is the decision.

**Why `Beam` answers `lights(x, y)`:** one class decides what is lit, and everything else asks it. Nobody can disagree about
what the beam touches.

**Why `Lighthouse` is separate from `Beam`:** the tower's looks will change when Jaron's art arrives, and we don't want
to touch the beam logic when that happens.

---

## Step 10. The night overlay (`NightOverlay` in `ui/`)

**What we did:** a dark layer with a beam-shaped hole.

**Why this way:** "draw only what the beam touches" is hard. "Draw everything, cover it with darkness, cut a hole for the
light" is easy and looks better, because the edges fade.

**Why in `ui/`:** it is a screen effect, not a thing in the world.

---

## Step 11. Enemy ships with inheritance (`Warship`, `Skiff`)

**What we did:** an **abstract** parent class `Warship` with everything ships share, and `Skiff` as the first child.

**Why:** the Cutter, Barge, Frigate and Dreadnought all move, get lit, and leave a wake. We write that once. Each new ship
is a few dozen lines instead of a few hundred.

**What "abstract" means:** you can't create a plain `Warship`. It is a template that forces every child to say how it looks.

**Why `GamePanel` keeps a list of `Warship`, not `Skiff`:** it can hold any ship and treat them the same. That is
**polymorphism**, and it is on the rubric.

**Why a skiff only shows when lit:** it creates the game's tension. You can't shoot what you can't see.

---

## Step 12. The art loader (`Assets`) and the `res/` folder

**What we did:** a class that loads a PNG from inside the project, shrinks it, and removes the magenta background.

**Why:**
- Image tools like Gemini can't reliably make transparent PNGs, but they can make a flat magenta background. We delete the
  magenta in code, so nobody needs an image editor.
- **Missing art returns `null` and the game draws a placeholder.** So art and code can be made at the same time, with
  nobody waiting on anybody.
- **A cache** means a file is loaded once, not once per ship.

**Why a `res/` folder:** a place only for art and sound, so Jaron's files never mix with code and nobody needs to touch
`.java` files.

**Why scale first, remove magenta second:** the blurry edge pixels are a mix of the ship and the magenta. After scaling we
can estimate and subtract the magenta, so there is no pink halo.

---

## Step 13. The `docs/` folder

**What we did:** one file per question someone might ask.

| File | The question it answers |
|---|---|
| `01-game-design` | What is the game? |
| `02-project-plan` | Who does what, and by when? |
| `03-code-guide` | How does the code work, and why? |
| `04-pitch-and-demo` | What do we say on Dec 1? |
| `05-git-workflow` | How do three of us share the repo? |
| `06-playtest-log` | Did real people play it? |
| `07-art-prompts` | How do we make art that matches? |
| `08-file-reference` | Where is everything? |
| `09-build-story` | Why is it built this way? (this file) |

**Why:** documentation and demo are 10% of the grade, but it also saves us. If you can explain why each piece exists,
the live demo and the professor's questions are easy.

---

## Step 14. How we checked our work

**What we did:** after each feature, the code was compiled and then run in a scripted test that draws the screen to a
picture, so we could look at it. For example:
- Is a ship inside the beam lit? Is one outside it not?
- Is the beam's turning correct across the 359 to 1 degree wrap?
- Does a missing art file or map file give a placeholder, not a crash?
- Does the art loader leave a pink edge?

**Why:** bugs found on Nov 19 cost minutes. Bugs found on Dec 1 cost the grade.

**What we can't check from the chat:** how it feels in a real window on your computer. That is why the first thing after
each feature is **you run it with F6 and tell us what you see**.

---

## Step 15. The day cycle, harvesting, nets (Nov 19 to 21)

**What we did:** a clock (`DayCycle`), a `GameWorld` class for one run, harvest spots, a net, a post, a radio, and a HUD.

**Why in this order:** the beam was only fun to look at. A game needs a **loop with a reason**: collect, prepare, survive,
repeat. Each piece answers one question.

| Piece | Question it answers |
|---|---|
| `DayCycle` | When is it safe, and when is it dangerous? |
| `ResourceNode`, `Inventory` | What can I do in the day? |
| `Net` (via the `Defense` interface) | What does the day buy me at night? |
| `Post`, `Radio` | What am I protecting, and how do I lose? |
| `Hud` | How do I know what is happening? |

**Why we made `GameWorld`:** `GamePanel` was turning into a 600-line class. We moved the rules out and left the window
parts behind. Starting a new run became one line: `new GameWorld(map)`.

**Why the waves and spots are in text files:** Gio can balance the game in Notepad.

**Why a click-to-shoot weapon came now:** with no way to hurt ships, the night would just be watching. The rule "you can only
shoot what the beam lights" makes the beam matter for fighting. It is a placeholder for the deck gun.

**How we checked:** a bot played all seven nights, and a second test proved a net really stops a ship for about three
seconds. Then we looked at rendered pictures of the day and the night.

---

## Step 16. More ships, a second defense, and art that plugs in (Nov 19)

**What we did:** `Cutter` and `Barge` (two more `Warship` children), `Mine` (a second `Defense`), a shuffled wave queue, and sprite
loading for every object.

**Why:** with one ship and one defense, every night felt the same. Now each ship asks for a different answer:
- the **cutter** is too fast to track, so you need luck or a mine,
- the **barge** is too tough to shoot down, so you stall it with nets and blow it up with a mine,
- the **skiff** is the easy one that fills the gaps.

**Why they were quick:** `Warship` already did the hard work. A new ship is a constructor and a drawing method.

**Why `Mine` needed a new method on the interface:** a net sits still, but a mine has to look around. We added
`update(ships)` to `Defense` with an empty default. The old `Net` did not change.

**Why we wired every sprite now:** so Jaron can drop in one image at a time and see it right away. Nobody waits for anybody.

**How we checked:** a bot that only shoots lost around night 4 to 7. A bot that also harvested, repaired and built won every time. A real
player will land between those two. That is what Gio's playtest decides.

---

## Step 17. Resources that take effort and run out (Nov 19)

**The problem we noticed:** one click, one resource, always there. It made the day trivial.

**What we did:**
- **Hold the mouse** on a spot until a ring fills. Letting go early wastes the progress.
- **Ore never grows back** and bamboo grows back slowly. Every vein is a choice.
- **Fish swim** around the sea in 5 kinds. You must follow a school with the cursor.
- **Four ores**, each with its own job: nickel (mines), copper (radio), chromite (armor), gold (lamp).

**Why data and not if-else:** the ores differ only by numbers (time, yield, use). Numbers belong in a table (the `ResourceType` enum), one line each. Adding an ore is one line.
Fish differ in *behavior* (they swim), so they got a class. The rule of thumb: **different numbers = data, different behavior = a class.**

**Why `Harvestable`:** bamboo sits still, a school moves, but the player does the same thing to both. One interface means one harvesting method.

**Why one ore picture is enough:** the game recolors a single grey rock into all four ores, so Jaron's art list stays short.

**How we checked:** a test confirmed that early release gives nothing, that a followed fish is caught and an unfollowed one is not, that every school spawns at its kind's depth and stays there, and that each ore use works. A bot that harvested, built and repaired still won, so the
waves are probably too easy for a perfect player. Gio's playtest decides what to tune.

---

## The pattern behind all of it

1. **One job per class.** (Easy to find, easy to change.)
2. **Numbers in one place.** (Easy to tune.)
3. **Never crash on missing files.** (Safe demo.)
4. **See something working early.** (Motivation and early bug-finding.)
5. **Write down the why.** (Study guide, demo, and professor questions all in one.)

## Where to read next

- To understand the code line by line: [03-code-guide.md](03-code-guide.md)
- To find a file or change a number: [08-file-reference.md](08-file-reference.md)
- To prepare the demo: [04-pitch-and-demo.md](04-pitch-and-demo.md)
