# Project Plan

**Deadlines:** final submission **Nov 30, 2026** (no testing after that). Live demo **Dec 1, 2026**.
Rubric: OOP design and code quality 30%, gameplay and mechanics 30%, data structures and file I/O 15%,
UI/graphics/polish 15%, documentation and demo 10%.

## Roles

| Member | Owns |
|---|---|
| **Gabriel** | All Java code: architecture, game loop, entities, defenses, file I/O, sound, building the JAR |
| **Gio** | Presentation slides, demo explanation, **playtesting** (and the playtest log), balance feedback, **the paper (1-page proposal)** |
| **Jaron** | Art assets (CC0 packs plus Gemini-assisted pieces), README, **UML diagram** (goes into Gio's paper), CREDITS |

Everyone speaks in the live demo. See [04-pitch-and-demo.md](04-pitch-and-demo.md).

## Schedule

Dates up to Nov 21 are agreed. After that is a **proposed** plan to adjust as we learn how fast we go.

| Date | Goal | Done when |
|---|---|---|
| Nov 18 | Project skeleton: window, game loop, states | The window opens, Enter/P/M work. **Done** |
| Nov 18 to 19 | Tile map from a text file, depth shading, foam | You see a real island and sea. **Done** |
| Nov 18 to 20 | Lighthouse beam, night overlay, a moving skiff | The beam lights the skiff, the rest is dark. **Done** |
| Nov 19 to 21 | Day/night cycle, harvesting, a net, post and radio, HUD bars, waves from a file | **Playable core. Done (first version)**: Gio can start playtesting |
| Nov 19 to 23 | Cutter and barge, mine buoys, waves from a text file. **Done (first version)** | Nights 1 to 7 are playable. Next: tune with Gio's playtests |
| Nov 24 | Post, radio, island health, win and lose, high-score file | A full run from night 1 to the end screen |
| Nov 25 | Sound, menu polish, art integration begins | Sounds on every action |
| Nov 26 | Should-haves: infiltrators, babaylan | The identify mechanic works |
| Nov 27 | Bakunawa eclipse, frigate, dreadnought | Nights 4 to 7 are playable |
| Nov 28 | Playtest round, balance, bug fixes | Gio's log has real results |
| Nov 29 | Build the JAR, test on another computer, README and UML final | JAR runs on a clean PC |
| Nov 30 | **Code freeze and submit.** Rehearse the demo | Nothing new after submission |

## Feature tiers and the cut order

Build must-haves first. Full lists are in [01-game-design.md](01-game-design.md).

**If time runs short, cut in this order:**

1. Recon drone and blood effects
2. Tikbalang, engkanto, and classmate cameos
3. The Lapulapu hero call
4. Babaylan and Bakunawa
5. Infiltrators

**Never cut:** the lighthouse beam, three defenses, three ship types, the HUD bars, Menu/Play/Pause/Game Over screens,
sound, and the high-score file. These are the rubric's core.

## Rubric checklist

| Rubric item | How we cover it | Where to show it |
|---|---|---|
| OOP design (30%) | Abstract `Warship` with subclasses, `Defense` and `NightEvent` interfaces, encapsulated entities, `GameState` enum | UML, code guide |
| Gameplay (30%) | Beam, resources, 7 nights, wave pacing, folklore events | Live demo |
| Data structures and file I/O (15%) | Arrays and `ArrayList` for entities, 2D array for the tile map, text files for map, waves, stats, and high scores, with try-catch | Code guide, demo |
| UI and polish (15%) | HUD, screens, art, sound, resizable window | Live demo |
| Documentation and demo (10%) | README with contributions, design doc, proposal and UML, demo script, playtest log | This repository |

## Required deliverables

- [ ] Working game (runnable JAR)
- [ ] 1-page proposal / paper (Gio), with the UML diagram from Jaron
- [ ] README with member contributions
- [ ] Peer playtest log (Gio)
- [ ] CREDITS with licenses and AI disclosure
- [ ] Live demo, 5 to 7 minutes, all members speaking

## Risks

| Risk | What we do |
|---|---|
| Running out of time | Follow the cut order above, and protect the "never cut" list |
| Art arrives late | The game runs with colored shapes first. Art is swapped in later |
| JAR fails on another PC | Build it by Nov 29 and test on a school PC or a friend's laptop |
| Merge conflicts | Follow [05-git-workflow.md](05-git-workflow.md): each person edits their own files |
| Demo crashes live | Have a recorded backup video and a second copy of the JAR |
