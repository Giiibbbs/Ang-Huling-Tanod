# Future improvements: Ang Huling Tanod

Ideas we thought of but did not put in the final version, so the code stays small and testable. Each one says what it would do, how we would build it with our current classes, and what we cut it for.

## 1. Day squall (cut on Nov 23)

**Idea.** From night 2, most days a storm breaks when about 50 s of the day are left. It covers one random half of the island (west, east, north or south) for 18 s. Nothing there can be harvested ("Squall - too rough"), and the player has to move to the other side.

**We built it and removed it.** It worked in the bot tests (dark rain overlay, a lightning flash, a banner and a radio line), but it made the 75 s day feel shorter and the bot's harvest time suffer. We want to see Gio's playtest first.

**How to bring it back.** `GameWorld` needs a timer, a side number, `squallBlocks(x, y)` checked in `updateHolding()`, and a `drawSquall()` that tints one half of the screen and draws rain lines. About 90 lines in total. A kinder version: the storm blocks only fishing, or only gives the bamboo a 25% bonus on the dry side.

## 2. More Bakunawa moments

Today the Bakunawa appears once, on night 7, after the last ship has set sail. Ideas:
- **Omen on nights 3 and 5:** the moon turns red and the sea ripples, with a radio line from Ka Inday ("Narinig mo ba ang kulog sa dagat?"). No gameplay change, only foreshadowing.
- **Small bite on night 4 or 6:** the moon is half eaten for 10 s and the beam goes dim. SPACE gongs end it early. It teaches the gong mechanic before the finale.
- **Endless mode:** the Bakunawa returns every 3rd night with a bigger gong meter.
- **Reuse:** `Bakunawa` already has stages IDLE, RISING, GONGS, SWALLOW and DONE. A "small" mode only needs a shorter meter and no swallow.

## 3. Day events and island life

- Fishing boom: the sea has 2x fish for one day.
- Typhoon warning: the next night is darker, so the player should stock parols.
- Sari-sari trader: a bangka sells ore for fish meals once per night.
- Market day (palengke): fish meals sell for bamboo.
- Barrio fiesta after night 4: one free upgrade, a lantern parade and a banner.

## 4. Filipino touches (not yet done)

- Bayanihan: two survivors carrying a big node together, twice as fast.
- Salubong: short Tagalog radio lines in each language option.
- Sinulog-style drum rhythm in the gong event (needs sound, which our class has not covered).
- A kubo (nipa hut) as the survivors' home building.

## 5. Gameplay and balance

- Difficulty setting on the menu: Easy / Normal / Hard (scales ships and the post's health).
- More Raider and Ghost art (sprites by Jaron, now drawn by code).
- More survivors with a job each (a cook that makes meals last longer, a lookout that spots Ghosts).
- Weather that affects the beam (fog already exists as a twist).
- Co-op: a second player on the keyboard with the arrow keys.

## 6. Technical

- Split `GameWorld.java` (about 70 KB) into `ShipSpawner`, `StoryTeller` and `SquallSystem`.
- A `test/` folder with JUnit tests and a `TESTING.md` for proof of testing.
- Sound (clap, gong, explosions) using `javax.sound.sampled`, after the class covers it.
- Save and load a run in the middle of the day.
