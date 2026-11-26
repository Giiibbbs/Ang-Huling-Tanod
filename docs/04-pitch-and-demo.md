# Pitch and Demo Script

**Slot:** live demo on **Dec 1, 2026**, **5 to 7 minutes**, all members speak.
Target **6 minutes** so there is room for a slow moment or a question.

> Last checked against the build of **Nov 19** (day cycle, harvesting, nets, mines, skiff, cutter, barge, post and radio).
> Items marked **[UPDATE]** depend on things not built yet (sound, folklore events, art, playtest numbers). Fill them in after the final build.

## Who says what

| Time | Speaker | Part |
|---|---|---|
| 0:00 to 1:00 | **Gio** | Hook and the story |
| 1:00 to 3:30 | **Gabriel** | Live demo of gameplay, narrated |
| 3:30 to 4:30 | **Jaron** | Art, assets, and how we made it |
| 4:30 to 5:30 | **Gabriel** | Code: OOP, data structures, file I/O |
| 5:30 to 6:15 | **Gio** | Playtesting results and what we changed |
| 6:15 to 6:30 | **All** | Close |

Gio hosts the intro and the close because he has the slides. Gabriel drives the keyboard during the demo, so he
narrates while he plays and nobody swaps seats mid-demo.

## Script

### 1. Hook and story (Gio, 1 minute)

> "Imagine the whole Philippines has gone silent. A fictional empire, the Kordan Dominion, has taken every island but
> one. Isla Dulo, the last free island at the end of the archipelago. You are its last tanod.
>
> By day you harvest fish, bamboo, and ore, and build defenses. By night it is pitch dark. The only thing you can see is
> what your lighthouse beam touches. And you have one more job: keep the radio alive so the world hears three words,
> *May natitira pa tayo*. Survive seven nights, and a relief fleet comes."

Slide: title, the island, one line of the pitch. Say that the empire is fictional, and no real country is shown.

### 2. Live gameplay (Gabriel, 2.5 minutes)

Run from the JAR, not from the IDE. Narrate what you do and *why*.

1. **Menu.** Press Enter. *(Mention: the window is resizable, drag it once.)*
2. **Day.** Show the **day dial** (sun, time left) and the **Workshop** panel on the left. **Walk** with WASD to a bamboo grove and **hold** the mouse on it until the ring fills. Chase a swimming fish school with the cursor. Click the **Net** row in the Workshop (point at the cost and the progress bar) and place a net. Place a **torch** too.
   "Harvesting takes time, ore never grows back, and fish swim away. Fish repair, bamboo makes nets, nickel makes mines, copper fixes the radio, chromite armors the post, gold upgrades the lamp. The day is short, so I have to choose."
   Mention the story: "Survivors from the blockade escape by bangka. Press **J** to bring all three: Ka Inday heals the post, Migo keeps the radio strong, and Tonyo shoots lit ships. We built them from one abstract `Survivor` class." Show the **Pana tower** (key 4).
   Open the **UPGRADES** tab (key U) and buy one level of a tool: "five tools go to level 3 for bamboo and a little ore, and the survivors even work for meals by day." Press **F1** to show the How to Play page, then **H** (in game) for the folklore allies: the **Lapu-Lapu stone** tells the island's old story, the **manananggal** flies out at night, and the **duwende** leaves ore every morning. "Respect is rewarded: the duwende answers a gift. All of them plug into the same `Survivor` and `Defense` classes, so the main loop did not change."
3. **Dusk, then night.** Let the screen fade, or press **F12** (or the **`** key) once (turns the developer keys on), then **N** to skip. Point at the beam sweeping by itself, the lantern, and the parol (the star lantern).
   "Only lit things are visible. The beam sweeps by itself, my lantern lights a small circle, and torches light fixed spots. I can also stand at the lighthouse and hold Space to steer the beam by hand."
4. **A ship appears in the beam.** Click it to shoot. Show a net stopping a ship, and a mine going off. From night 2 a **Raider** stops and shoots your tower or torch: show the red tracer and the "-1". Press **F** to fire a flare and light a second side. By day, stand next to a damaged defense and **hold X** to mend it (it costs bamboo). "Everything wears down, so bamboo is for building and for upkeep."
   "I can only shoot what the beam has found."
5. **The ending.** Press **L**, then **N** until it is night 7, and let it end (or skip ahead with N). Then let the ending play (about 30 seconds), or press ENTER to skip to the title card.
   "Lights across the sea, other islands answering. You were never the last tanod."
6. **Pause** with P to show the pause state, then resume.
7. **HUD.** Point at the two bars: **Post** health and **Radio** signal. Say how you lose (either hits zero) and how you win (survive night 7).
   Show that the three ship types need different answers: the fast cutter, the tough barge, the common skiff.

If time is short, press **F12** (or **`**, the key above Tab) to turn on the developer keys, then **N** to skip phases (it also ends the night early). The developer keys are OFF by default so a tester cannot skip nights by accident.

### 3. Art and assets (Jaron, 1 minute)

> "Our art is 3/4 top-down pixel art. We used free CC0 asset packs for tiles and water, and AI-assisted references for
> custom pieces. Everything is credited in the repository."

Show: one sprite, and how a Gemini prompt produced it (`docs/07-art-prompts.md`). **[UPDATE: after Jaron's art is in]**
Be honest about what was made by hand, what came from packs, and what was AI-assisted.

### 4. How the code is built (Gabriel, 1 minute)

Show the UML and **two** short code moments. Do not scroll through code.

- **OOP:** "`Warship` is an abstract class. `Skiff`, `Cutter`, and `Barge` extend it and change how they move and attack.
  `Defense` is an interface, so a net and a mine are kept in one list and treated the same by the game, even though a net waits and a mine acts by itself."
- **Data structures and file I/O:** "The island map is a 2D array loaded from a text file. The wave plan and the harvest spots are text files too,
  with try-catch around every read, so a missing file never crashes the game. High scores are a file too: `highscores.txt`, top 5, read when the game starts and written when you finish a run."
- **Game loop:** "A Swing Timer calls `update()` then `repaint()` sixty times a second. The game draws on a small
  640x360 screen and scales it, so the window can be any size."

### 5. Playtesting (Gio, 45 seconds)

> "We had [N] classmates play. They found [problem 1] and [problem 2]. Because of that, we changed [change 1] and
> [change 2]."

Use real numbers from [06-playtest-log.md](06-playtest-log.md). **[UPDATE]**

### 6. Close (all)

One sentence each, for example:

- Gio: "Every night is a decision about what to risk."
- Jaron: "The island's look is meant to feel like home."
- Gabriel: "May natitira pa tayo. Thank you."

## Likely questions

| Question | Short answer |
|---|---|
| Is this original? Is there a similar game? | The concept was approved by our professor, who found no similar game on the Play Store. The empire, ships, and mechanics are our own. |
| Which genre is it? | Tower defense with survival and resource management. |
| Where is inheritance? | `Warship` is abstract. `Skiff`, `Cutter` and `Barge` extend it and only set speed, health, landing damage and looks. |
| Where is an interface? | `Defense`, with `Net` and `Mine`. `NightEvent` is planned for the folklore events. |
| Where is file I/O? | The map (`island.txt`), waves (`waves.txt`) and harvest spots (`nodes.txt`) are read from text files. The high scores (`highscores.txt`) are read at start-up and written after each run: `HighScores` keeps a sorted `ArrayList` of `ScoreEntry` objects (`Comparable`), and skips damaged lines instead of crashing. |
| What if the file is missing? | We catch the exception and fall back to defaults or show a message. The game does not crash. |
| Why Swing Timer, not a thread? | It was taught in class, and it runs on the Swing thread, so drawing is safe. |
| Who did what? | Gabriel: code. Gio: slides, demo, playtesting. Jaron: art, README, UML. See the README. |
| Did you use AI? | Yes, for code help and art references. We understand and can explain the code, and the art is credited in CREDITS.md. |
| What would you add next? | The nice-to-haves: raiders, the drone, the tikbalang zone. |

## Rehearsal and backup plan

- [ ] Rehearse **twice** with a timer. Cut, don't rush, if you go over 7 minutes.
- [ ] Use the JAR on the **same computer** used for the demo. Test it there before Dec 1.
- [ ] Record a 2-minute **backup video** of a full run, in case the game crashes live.
- [ ] Keep a **second copy** of the JAR on a flash drive and in the repo.
- [ ] Check sound before starting (volume on, speakers connected).
- [ ] Know the **fastest way to restart** the game if it breaks.
- [ ] Everyone reads the code guide, so anyone can answer a code question.


## Extra demo beats (Nov 23)

- After the ending card, press **E** to show endless mode (night 8 and more, scaling fleets). Press P then **Q** to retire and save the score.
- Place two mines close together (2 key, click water) and let a ship hit one: the chain reaction.
- Tonyo's "mended" popup over a worn net or parol.

- **Opening comic:** the game opens with a 6-panel comic (about 40 s). For the demo press **ESC** to skip it, or let it play for the first 20 seconds of the pitch as the story hook.
