# 13 - Lessons audit: what the code uses vs. what was taught

Checked on Nov 26 against the professor's modules: Intro to Java, Programming Environment, Syntax, Scanner/JOptionPane input, Operators, Control Structures, Arrays, and Java Swing (JFC, layouts, events with listeners/adapters/lambdas, MVC widgets, EDT/SwingWorker, custom painting).

## Directly taught (use freely in the defense)
- Variables, operators, if / switch / for / while / do-while, break / continue / return
- Arrays, including 2D arrays (the tile map and the map file loader)
- Swing and AWT: JFrame, JPanel, `paintComponent` + `super.paintComponent(g)`, Graphics, KeyListener, MouseListener, listeners/adapters
- Lambdas: `e -> {...}`, `() -> {...}` (the game loop Timer and `invokeLater` in `Main`)
- try / catch (error handling when reading data files)
- The Event Dispatch Thread and `SwingUtilities.invokeLater`

## Used in the game but not in the lecture files we received
These are the OOP and data-structure topics the project rubric itself asks for. Be ready to explain them in your own words.

| Concept | Where | One-line explanation |
|---|---|---|
| Inheritance / abstract classes | `Warship` -> Skiff, Cutter, Barge, Raider, Ghost; `Survivor` -> Tonyo etc. | Shared behavior lives in the parent; each child changes only what is different |
| Interfaces | `Light`, `GlowSource`, `Durable`, `Harvestable` | A promise that a class can do something, such as give light |
| Polymorphism | the ship list is one `ArrayList<Warship>`; each ship draws itself differently | One call, different behavior per ship type |
| Enums | `GameState`, `ResourceType`, `Twist`, `Phase` | A fixed set of named choices |
| ArrayList / HashMap | ships, defenses, survivors, high scores | A resizable array |
| File I/O | `island.txt`, `nodes.txt`, `waves.txt`, `voices.txt`, `highscores.txt` | Read lines, split, parse (try / catch) |
| `javax.swing.Timer` | game loop in `GamePanel` | Fires every 16 ms on the EDT, the same listener idea as a button |
| `ImageIO` / `BufferedImage` | `Assets`, `IntroScene` | Loads PNG files into memory |
| `static final` constants | `Constants` | Values that never change, named in one place |

## Changed on Nov 26
Method references (`this::isLit`, `this::oreLeftOnMap`) were replaced by plain lambdas (`(x, y) -> isLit(x, y)`), which the Swing module teaches. Same behavior; the 34 checks still pass.
