# Testing: Ang Huling Tanod

This is our proof of testing. It says what we tested, how to repeat it, what the results were on Nov 23, and what is still NOT tested. We tried not to write "works" without saying how we know.

## 1. Unit checks (`test/atingdagat/GameTests.java`)

29 small checks of the game rules, with no libraries (not even JUnit), so anyone can run them.

| Area | What is checked |
|---|---|
| Inventory | add, get, spend, spend too much fails, bamboo is capped at 30 |
| Day cycle | starts on day 1, the day is 4500 frames, dusk follows, ends after night 7 |
| Endless mode | night 8 starts on a day, the flag is set, the game is not "finished", night 9 follows |
| Waves | night 1 has ships, endless fleets grow, endless is faster but never below 60% |
| Twists | night 1 has none, endless nights never return "no twist" |
| Durability | wear, restore never above the maximum, plating raises the maximum |
| High scores | keeps only 5, best first, a 0-night run does not qualify, saved and loaded from a file, endless night 12 reads back |
| Mines | a blast reaches neighbours but not far mines, it hurts a ship, it only explodes once, costs 1 nickel |

**Run it (Windows, from the project folder `AngHulingTanod`)** after Clean and Build in NetBeans:

```
javac -cp build\classes -d build\test test\atingdagat\GameTests.java
java -cp build\classes;build\test atingdagat.GameTests
```

Expected last line: `29 passed, 0 failed`. The program exits with code 1 if anything fails.

Result on Nov 26, 2026: 29 passed, 0 failed (the 5 Bakunawa checks were removed with the feature).

## 2. Bot playthroughs (headless, no window)

We wrote a bot that plays the game by calling the same methods the mouse and keyboard call: it harvests by day, builds nets, mines, parols and Pana towers at dusk, and shoots lit ships by night. It runs the whole game faster than real time. The bot is not part of the game; it lived on the lead coder's side during development.

| Run | Result |
|---|---|
| Active bot, seeds 1 to 3 (earlier build, with the Bakunawa) | Wins all 7 nights on every seed. Then plays endless mode: survived to night 10 on two seeds and lost on night 9 or 10 on the third |
| Passive bot (no active play) | Loses on nights 3 to 5. This is by design: the beam does not shoot for you |
| Fuzz run: random keys and clicks, 18,000 frames | 0 crashes, average update 0.016 ms, average draw 4 ms |

What the bot found, and what we changed because of it (full story in `09-build-story.md`):
- Bamboo was too easy to hoard, so we capped the pack at 30.
- Nights 3 and 4 had long gaps between ships, so we made the spawn stream steadier.
- The bot never built a mine (2 nickel was too much), so mines now cost 1 nickel and chain.
- Test keys were reachable by accident, so they now need F12 (or the backtick key).

## 3. Real-window checks

A script pressed real keys in the real game window (under a virtual display): F12 turns the developer keys on, the NAME screen takes typed letters, and the victory card accepts E. We also looked at screenshots of the parol, the Bakunawa small visit, the omen and the Help screen to check nothing is cut off.

## 4. What we did NOT test (honest list)

- **No human has played the full game yet.** Balance is judged by bots. Gio's playtest on Nov 26 goes in `06-playtest-log.md`.
- The bot never mines ore, so **ore powers** (copper radio boost, chromite plating, gold lamp) are checked only by reading the code and the unit checks for plating.
- The **JAR** has not been run on a second PC (planned for Nov 29).
- **Sound** is not part of the game, so it is not tested.
- Performance was measured on one laptop only.

## 5. Bugs found and fixed (examples)

| Bug | How found | Fix |
|---|---|---|
| `NoSuchFieldError` after changing an enum | Run from NetBeans | Clean and Build; added to the how-to-run notes |
| Night 7 crashed on the pulse table | Bot on night 7 | The pulse array got a 7th entry |
| F12 "did not work" | User report | Stale build; we also added the backtick key |
| Statue floated on the sand | Screenshot | Moved onto the grass patch, added a shadow |

**Nov 26 (after removing the Bakunawa):** a headless run cleared the sea every frame from the start of night 7. The game reached the win after night 7 and endless mode kept running for 20,000 frames with no errors.
