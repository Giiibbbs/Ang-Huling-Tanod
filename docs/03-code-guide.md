# Code Guide: how the game works and why

*A study guide for the team. It explains the code feature by feature, not line by line, using the Feynman idea:
if you can explain it simply to a classmate, you understand it. Each section has the same shape:*

- **In one sentence**: the plain explanation.
- **Why we did it this way**: the reason, and what we chose not to do.
- **How it works**: the key lines only.
- **Analogy**: something from real life.
- **Questions the professor might ask**: with short answers.

*This guide grows with the game. Every new feature gets a new section (use the template at the end).*

---

## 0. The big picture

The code is split into packages by job:

```
core      Main, GameFrame, GamePanel, GameState, Constants    the window, the loop, the screens
map       TileMap                                             the island and sea (reads island.txt)
entities  Lighthouse, Beam, Warship, Skiff                    things in the game world
ui        NightOverlay                                        what is drawn on top of the world
util      Assets                                              helpers (loading art)
```

```
Main -> GameFrame -> GamePanel --creates--> TileMap, Lighthouse, Beam, NightOverlay, ships (Warship)
                        |
                  GameState, Constants        Skiff extends Warship;  Skiff -> Assets (art)
```

- `Main` starts the program.
- `GameFrame` is the window.
- `GamePanel` is where the game runs and is drawn. Everything else will plug into it.
- `GameState` lists the screens (menu, play, pause...).
- `Constants` holds the shared numbers.
- `TileMap` loads and draws the island and sea.
- `Lighthouse` and `Beam` are the player's tool. `NightOverlay` makes the screen dark around them.
- `Warship` and `Skiff` are the enemy. `Assets` loads their art.

**Rubric link:** OOP design (30%). Each class has one job. That is the idea behind *separation of responsibilities*.

---

## 1. The game loop (`GamePanel`, using `javax.swing.Timer`)

**In one sentence:** the game repeats "update the world, then redraw the screen" about 60 times every second.

**Why we did it this way:** a game is not a program that waits for you. Things move even when you press nothing. A
`javax.swing.Timer` fits because it was taught in class and it runs on the same thread as Swing, so we never touch the
screen from two threads at once. We did not use our own `while(true)` loop with `Thread.sleep`, because it can freeze
the window and is easier to get wrong.

**How it works:**

```java
int delay = 1000 / Constants.TARGET_FPS;      // 1000 ms / 60 = 16 ms
timer = new Timer(delay, e -> {
    update();    // move things, check rules
    repaint();   // ask Swing to call paintComponent
});
timer.start();
```

`update()` is the *brain* (logic) and `paintComponent` is the *eyes* (drawing). Keeping them apart means we can pause
the game by skipping `update()` while still drawing the "PAUSED" text.

**Analogy:** a flipbook. Each page is one frame. Flip 60 pages a second and the drawing looks like it moves.

**Questions the professor might ask:**

- *Why 60 FPS?* It looks smooth to the eye, and it matches most monitors.
- *Is the timer exact?* No. 1000 / 60 is 16 ms after rounding, so it runs near 62 FPS, and Swing may be a little late
  when the computer is busy. That is fine for our game. A stricter approach would measure the real time between frames
  (delta time), which we can add if movement ever looks uneven.
- *What happens if `update()` is slow?* The next frame is late and the game feels laggy. That is why we keep `update()`
  simple and prepare data (like maps) once, not every frame.

---

## 2. The small screen, scaled up (`GamePanel.paintComponent`)

**In one sentence:** the game draws everything on a small 640x360 picture, then stretches that picture to fit whatever size
the window is.

**Why we did it this way:** it gives us three things at once.

1. **A resizable window.** The game logic always uses the same 640x360 coordinates, so resizing never breaks anything.
2. **Sharp pixel art.** We stretch with "nearest neighbor", which makes pixels bigger instead of blurry.
3. **Simple math for art.** Every tile is 16x16 pixels, so the map is 40 by 22.5 tiles on the small screen.

**How it works:**

```java
// 1. draw the game on the small image
Graphics2D g2 = screen.createGraphics();
drawGame(g2);

// 2. find the biggest size with the same shape that fits the window
double scale = Math.min((double) getWidth()  / Constants.INTERNAL_WIDTH,
                        (double) getHeight() / Constants.INTERNAL_HEIGHT);
drawW = (int) (Constants.INTERNAL_WIDTH  * scale);
drawH = (int) (Constants.INTERNAL_HEIGHT * scale);
drawX = (getWidth()  - drawW) / 2;   // black bars if the shapes differ
drawY = (getHeight() - drawH) / 2;

// 3. stretch it onto the real panel
out.drawImage(screen, drawX, drawY, drawW, drawH, null);
```

`Math.min` picks the *smaller* of the two ratios, so the picture never gets cropped. If the window is a different
shape, the leftover space becomes black bars (the same as watching a movie on a different screen).

**Mouse clicks.** The mouse reports window pixels, but the game thinks in 640x360. So we convert:

```java
public int toGameX(int windowX) {
    return (int) ((windowX - drawX) * (double) Constants.INTERNAL_WIDTH / drawW);
}
```

We subtract the black bar (`drawX`), then divide by the scale. Every click in the game will go through this.

**Analogy:** a photocopier with a zoom button. You make the original small once, then you enlarge it to fit the paper.

**Questions the professor might ask:**

- *Why not just draw at full size?* Then every position and size would depend on the window, and the art would need to
  be redrawn for each size. This way we write the logic once.
- *What are the downsides?* If the window is not a clean multiple of 640x360 (like 2x or 3x), some pixels come out
  slightly wider than others. We could force whole-number scaling if it bothers us.
- *What is `BufferedImage`?* An image in memory that we can draw on, the same way we draw on the screen.

---

## 3. Game states (`GameState` enum and `GamePanel`)

**In one sentence:** the game is always on exactly one screen (menu, play, pause, game over, or victory), and that choice
decides what is updated and drawn.

**Why we did it this way:** an `enum` is a fixed list of allowed values, so the game *cannot* be in a state that does not
exist. A mistyped `"pasue"` in a `String` would compile and fail silently, but a wrong enum name is a compile error. It
is also a rubric requirement (Menu, Play, Pause, Game Over).

**How it works:**

```java
public enum GameState { MENU, PLAY, PAUSE, GAME_OVER, VICTORY }
```

```java
private void update() {
    if (state != GameState.PLAY) {
        return;                       // nothing moves in the menu or while paused
    }
    // ...game logic here
}
```

The same `switch (state)` idea is used in the key handler (what P means in each state) and in `drawGame` (what to draw).

**Day and night are not states.** They are *phases inside PLAY*. If we made them states, pausing at night would need
its own "night pause" state. Keeping them inside PLAY avoids that.

**Analogy:** a TV on one channel at a time. Changing the channel changes everything on the screen, but it is still one TV.

**Questions the professor might ask:**

- *Why an enum and not constants like `int MENU = 0`?* The compiler checks enums, so typos and invalid values are caught
  early, and the code reads better.
- *How does pause work?* `update()` returns early when the state is not PLAY, but `paintComponent` still draws the last
  frame, so the screen does not go blank.

---

## 4. Keyboard and mouse input (`KeyAdapter`, `MouseAdapter`)

**In one sentence:** the panel listens for key presses and mouse clicks, and either changes the state or remembers which
keys are held.

**Why we did it this way:**

- A `KeyAdapter` lets us override only the methods we need (`keyPressed`, `keyReleased`) instead of writing all methods
  of `KeyListener`.
- A `boolean[] keys` array remembers which keys are *held down*. We use an array because it was covered in class, and
  key codes are small numbers, so `keys[code]` is a direct lookup.
- The key handler (`handleKeyPress`) is for *one-time* actions like "press P to pause". The array is for *held* actions,
  like moving something while a key stays down. Right now the array is filled but not read, because nothing moves with
  keys yet. The beam will use the mouse, but we keep the array for later.

**How it works:**

```java
@Override
public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code < keys.length) keys[code] = true;   // remember it is held
    handleKeyPress(code);                        // one-time actions
}
```

**Analogy:** a doorbell (one ring, one action) versus a light switch (stays on until you turn it off).

**Questions the professor might ask:**

- *Why `if (code < keys.length)`?* Some keys have key codes above 255. Without the check, the array would throw an
  `ArrayIndexOutOfBoundsException`.
- *Why does the panel need `setFocusable(true)` and `requestFocusInWindow()`?* A component only receives key events when
  it has focus. Without these, the window opens but key presses go nowhere.

---

## 5. `Constants`: one place for the numbers

**In one sentence:** every shared number (screen size, FPS, tile size, number of nights, the title) lives in one class.

**Why we did it this way:** to change something once and have it change everywhere. If the number of nights changes
from 7 to 5, we edit one line, not twenty. It also avoids "magic numbers" (a bare `360` in the code that nobody can
explain).

**How it works:**

```java
public final class Constants {
    private Constants() { }                          // nobody makes a Constants object
    public static final int INTERNAL_WIDTH = 640;
    public static final int TOTAL_NIGHTS = 7;
}
```

`final class` means nobody can extend it, the `private` constructor means nobody can create one, and `static final`
fields belong to the class and cannot change.

**Analogy:** the settings page of the game, written in code.

**Questions the professor might ask:**

- *What does `static final` mean?* `static`: one copy shared by everybody. `final`: the value never changes.
- *Why not a text file?* Constants that are fixed rules of the program stay in code. Numbers we want to *tune*
  (ship stats, wave sizes) will go in text files later, so Gio can change them without recompiling.

---

## 6. Starting the program safely (`Main` and `GameFrame`)

**In one sentence:** `Main` creates the window on Swing's own thread, then starts the loop.

**Why we did it this way:** Swing is not thread-safe, so windows must be created on the *Event Dispatch Thread*.
`SwingUtilities.invokeLater` does exactly that.

**How it works:**

```java
SwingUtilities.invokeLater(() -> {
    GameFrame frame = new GameFrame();
    frame.setVisible(true);
    frame.startGame();          // starts the timer after the window exists
});
```

`GameFrame` adds the `GamePanel`, sets the starting size (1280x720), allows resizing, sets a minimum size (640x360), and
centers the window.

**Analogy:** a restaurant. The kitchen (the Swing thread) has one chef at a time. You hand orders to the chef instead of
cooking on your own.

**Questions the professor might ask:**

- *What is the EDT?* The one thread that Swing uses for all drawing and events.
- *Why `EXIT_ON_CLOSE`?* So closing the window also stops the program and its timer.

---

## 7. The island and sea (`TileMap`, in `atingdagat.map`)

**In one sentence:** the map is a grid of 16x16 tiles read from a text file, and `TileMap` draws the island, shades the
water by depth, and adds foam along the shore.

**Why we did it this way:**

- **A text file, not code.** The island lives in `atingdagat/data/island.txt`, one character per tile (`~` water,
  `:` sand, `#` grass). Anyone on the team can open it in Notepad and reshape the island without touching Java. It is
  also the file I/O the rubric asks for.
- **A 2D array.** A grid is the natural fit: `tiles[row][col]`. Looking up a tile is instant.
- **Loaded from inside the project.** We read the file with `getResourceAsStream`, not a path like `C:\...`, so it still
  works from the JAR on another computer.
- **A safe fallback.** If the file is missing or has no land, we print a warning and build a default oval island in code.
  The game never crashes because of a map problem.
- **Colored squares first.** The map is drawn with plain colors, so we see a result tonight and art can replace it later.

**How it works:**

*Loading, with exception handling:*

```java
try (InputStream in = TileMap.class.getResourceAsStream(resourcePath)) {
    if (in == null) { return false; }            // file missing -> fallback island
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    // read line by line, copy each character into tiles[row][col]
} catch (IOException e) {
    return false;                                // unreadable -> fallback island
}
```

The map starts full of water, then only `:` and `#` are copied in. Short lines are padded with water, long lines are cut,
and unknown characters are treated as water.

*Depth, the "ripple":* we want shallow water to be light and deep water dark. For every water tile we need its distance
from the shore.

```java
// land = 0. Water touching depth 0 becomes 1, touching 1 becomes 2, ...
for (int d = 1; d <= MAX_DEPTH; d++)
    for each tile: if (depth == -1 && a neighbor has depth d - 1) depth = d;
```

It is like dropping a pebble and watching rings spread out from the land. It only needs loops and arrays. The same
"spread out from a start" idea becomes BFS pathing for the ships later.

*Drawing in two passes:* first every tile (land, or water colored by its depth), then the foam on top, so a neighbor tile
never paints over it. Land gets a small steady color variation from a `hash(col, row)`, so it looks natural but never
flickers, because the same tile always gets the same number. Foam checks the four sides (and corners) of each water tile
for land, and its width follows a sine wave, so it breathes in and out:

```java
double wave = Math.sin(frame * 0.06 + (r + c) * 0.5);   // -1 to +1, smooth
```

*Questions for other classes:* `isLand(row, col)`, `isWater`, `getTile`, and `getDepth`. Ships, defenses, and pathing
will ask the map instead of reading the array themselves (encapsulation). Out-of-bounds asks are answered "sea", so
nobody can crash it by asking about row -1.

**Analogy:** a game of battleship's grid, painted like a real map. Each square has one letter. The painter (`draw`) reads
the letters and colors the squares.

**Questions the professor might ask:**

- *Why a 2D array and not an ArrayList?* The map size never changes and we look up tiles by position, which is exactly
  what an array is good at.
- *Why `char[][]` and not numbers?* The file is readable by humans, and each tile type has a letter we recognize.
- *What happens if the file is missing?* A warning is printed and a default island is built. This is our exception
  handling for file I/O.
- *Why is the foam drawn in a second pass?* If we drew it with the tiles, the next tile drawn would cover half of it.
- *How would you add a new tile type?* Add a character constant (for example a rock `R`), copy it in `load`, and add a
  color case in `drawLand`. The file and the rest stay the same.
- *Why is `MAP_ROWS` 23 when 22.5 rows fit?* 360 / 16 = 22.5, so we round up to cover the whole screen. The last row is
  cut in half.

---

## 8. The lighthouse beam (`Beam` and `Lighthouse`)

**In one sentence:** the beam is a cone of light that turns toward the mouse, and it can answer one question: "is this
point lit?"

**Why we did it this way:**

- **It turns at a fixed speed instead of snapping to the mouse.** If it snapped, there would be no decision to make.
  Because it needs about a second and a half to swing around, the player has to plan where to look. That is the core of the
  game.
- **The beam knows the rules, and other classes ask it.** `beam.lights(x, y)` is the single place that decides what
  counts as lit. Ships and the overlay both use it, so they can never disagree.
- **The tuning numbers are named constants at the top** (`TURN_SPEED`, `HALF_ANGLE`, `LENGTH`, `LAMP_GLOW`), so Gio can
  say "make the beam wider" and it is a one-line change.

**How it works:**

*Aiming:* the angle from the lamp to the mouse comes from `Math.atan2(dy, dx)`.

```java
targetAngle = Math.atan2(y - originY, x - originX);
```

*Turning smoothly:* each frame the beam moves toward the target by at most `TURN_SPEED`.

```java
double diff = angleDiff(targetAngle, angle);
if (Math.abs(diff) <= TURN_SPEED) angle = targetAngle;
else angle += Math.signum(diff) * TURN_SPEED;
```

`angleDiff` returns the *shortest* turn. Angles wrap around (359 degrees and 1 degree are only 2 degrees apart), and
without this the beam would spin the long way around.

*Is a point lit?* Two checks: is it close enough (distance), and is it inside the cone (the angle to the point is within
`HALF_ANGLE` of the beam's angle)? Anything within `LAMP_GLOW` of the lamp is always lit.

```java
return Math.abs(angleDiff(Math.atan2(dy, dx), angle)) <= HALF_ANGLE;
```

`Lighthouse` is a small separate class that holds where the tower stands and draws it. The beam starts from the lamp
near its top. Splitting them means we can replace the drawing with Jaron's sprite without touching the beam logic.

**Analogy:** a searchlight on a prison wall. It is heavy, so you can't whip it around, and you only know what is in
the circle of light.

**Questions the professor might ask:**

- *What does `atan2` do?* It turns a direction (dx, dy) into an angle, and it works in all four quadrants.
- *Why a separate `angleDiff`?* To handle the wrap-around at 360 degrees.
- *Why are the numbers constants?* So they can be tuned in one place during playtesting.
- *Why do Beam and Lighthouse live in `entities`?* They are things in the game world. UI code lives elsewhere.

---

## 9. Darkness with a hole in it (`NightOverlay`)

**In one sentence:** at night we paint a dark layer over the screen and then erase a beam-shaped hole in it.

**Why we did it this way:** the obvious idea is "draw only what the beam touches", but that gets complicated. The trick
is the reverse: draw *everything* normally, then put a dark sheet on top, and cut the beam out of the sheet. The world
shows through the hole.

**How it works:** each frame, on a see-through image the size of the screen:

1. Fill it with dark blue (alpha 232 of 255, so a faint hint of the island is still visible).
2. Switch to `AlphaComposite.DstOut`. In this mode, anything we paint *removes* darkness instead of adding color.
3. Paint the cone shape (`Arc2D` in `PIE` mode) with a radial gradient. The gradient is strong near the lamp and fades
   to nothing at the tip, so the light fades with distance.
4. Repeat with a few wider, fainter cones. That softens the edge of the beam.
5. Erase a circle around the lamp (the always-lit glow), then draw the finished layer over the game.

```java
d.setComposite(AlphaComposite.DstOut);                 // painting now ERASES darkness
d.setPaint(new RadialGradientPaint(...));              // strong at the lamp, zero at the tip
d.fill(cone);
```

After the darkness, `Beam.drawLight` adds a faint warm yellow glow in the same shape, so the beam looks like it is
shining.

**Analogy:** a sheet of black paper with a hole cut in it, held over a picture. Move the hole and you see a different
part.

**Questions the professor might ask:**

- *What is `AlphaComposite`?* It controls how new pixels combine with what is already there. `DstOut` removes.
- *Why a gradient?* Real light fades with distance. A flat cone would look like a cut-out.
- *Isn't it slow to redraw a full-screen layer every frame?* The layer is only 640x360, so it is cheap. We never scale
  it ourselves, because the whole screen is scaled once at the end.
- *Why the sign flip on the arc angle?* `Arc2D` measures angles counter-clockwise, but our screen angles go clockwise.

---

## 10. Enemy ships and inheritance (`Warship`, `Skiff`)

**In one sentence:** `Warship` is the parent class with everything ships share, and `Skiff` is a child that only adds
what makes it a skiff.

**Why we did it this way:** the Cutter, Barge, Frigate, and Dreadnought are all ships. They all move, get lit by the
beam, and leave a wake. Writing that once in `Warship` means each new ship is about 30 lines. This is **inheritance**,
and it is a big part of the OOP rubric (30%).

**How it works:**

```java
public abstract class Warship {
    public void update(TileMap map, Beam beam) { move(map); updateLight(beam); updateWake(); }
    protected abstract void drawShape(Graphics2D g);   // each ship says what it looks like
    public abstract String getName();
}

public class Skiff extends Warship {
    public Skiff(...) { super(x, y, targetX, targetY, 0.55, 3); }   // speed 0.55, 3 health
    protected void drawShape(Graphics2D g) { ... }
}
```

- **`abstract`** means you can't create a plain `Warship`. It is only a template. Each subclass must fill in the
  abstract methods.
- **Polymorphism:** `GamePanel` keeps `ArrayList<Warship> ships`. It calls `ship.update(...)` and `ship.draw(...)` on
  every ship without caring which kind it is, and Java runs the right version.
- **Moving:** the ship sails in a straight line toward the lighthouse and stops when it touches a land tile, by asking
  the map `isLand(row, col)`. (Smarter path-finding around islands comes later.)
- **Being lit:** when the beam lights it, `lit` jumps up quickly. When it leaves the beam, `lit` fades slowly, so you
  still see it for about a second. At night the ship is drawn with `lit` as its opacity, so unlit ships are invisible.
- **The wake:** the last 10 positions are stored in plain arrays used as a *ring buffer* (when full, the newest position
  overwrites the oldest). They are drawn as fading, growing white circles.
- **Removing ships safely:** `GamePanel` loops through the list *backwards* when removing ships. Going forwards, removing
  one would shift the next one into its place and skip it.

**Analogy:** a car factory. The shared chassis (engine, wheels) is `Warship`, and each model (Skiff, Cutter) is a different
body on the same chassis.

**Questions the professor might ask:**

- *What is the difference between an abstract class and an interface?* An abstract class can hold shared code and fields
  (our movement and wake). An interface is only a list of things a class promises to do, and we will use one for
  `Defense`.
- *What is polymorphism?* Using a parent type (`Warship`) to hold different children, and the right method runs
  automatically.
- *Why `protected`?* Subclasses can use the field but outside classes can't. The rest stays `private`.
- *Why `super(...)` in the constructor?* It runs the parent's constructor first, to set up the shared parts.

---

## 11. Art loading and the magenta trick (`Assets`)

**In one sentence:** `Assets` loads a picture from inside the project, shrinks it to the size we need, and turns its solid
magenta background into transparency.

**Why we did it this way:**

- AI image tools can't make real transparent PNGs reliably, but they can make a flat magenta background. We key it out
  in code, so no one needs an image editor.
- **It never crashes on missing art.** If `res/ships/skiff.png` doesn't exist, `get` returns `null` and the ship draws a
  placeholder shape. When Jaron adds the PNG, it just appears, with no code change.
- **Images are cached,** so the same file is read once, not once per ship.

**How it works:**

1. `getResourceAsStream` reads the file from inside the project (so it also works from the JAR).
2. The image is halved again and again until it is close to the target size, then scaled the last step. One giant jump
   (1024 to 32 pixels) would look jagged.
3. **Only then** the magenta is removed. Scaling first means the edge pixels are blended with magenta, and we can clean
   them up.
4. An edge pixel is a *mix* of the object color and magenta. We estimate how magenta it is, make it that much
   transparent, and subtract the magenta from its color. So there's no pink halo.

```java
double magenta = (Math.min(red, blue) - green) / 255.0;   // pure magenta = 1, grey = 0
```

A pixel under 0.20 is left alone, which is why the crimson pennant on the skiff survives. Over 0.85 it becomes fully
transparent.

**Limit to remember:** art must not use magenta, pink, or purple. They would be treated as background.

**Analogy:** a green-screen in a film studio. The studio is magenta instead of green.

**Questions the professor might ask:**

- *What is a cache?* A saved copy so we don't repeat slow work. Here, a `HashMap` from a file name and size to the image.
- *Why return `null` instead of throwing an exception?* Missing art is not a game-ending problem, so we recover with a
  placeholder and a message.
- *What is chroma keying?* Removing one color so what is behind shows through, as with a green screen.

---

## 12. The clock: day, dusk, night, dawn (`Phase` and `DayCycle`)

**What it does:** decides which part of the day it is, and how dark the sky is (0 = bright day, 1 = full night).

**Why we did it:** the whole game is "prepare in the light, survive in the dark". Something has to count the time and
say when the light changes. We gave that one job to one class.

**How it works, like you are new:**

- A **`Phase`** is an `enum` (a fixed list of names): `DAY`, `DUSK`, `NIGHT`, `DAWN`. Writing `Phase.NIGHT` is clearer
  than writing the number `2`.
- `DayCycle` keeps three things: the night number (1 to 7), the current phase, and a `timer` that counts frames.
- Every frame, `update()` adds 1 to the timer. When the timer reaches the length of the phase (`Constants.DAY_FRAMES`
  and so on), `advance()` moves to the next phase and resets the timer. A `switch` picks the next phase.
- **Night has no timer.** It ends when the last ship of the night is gone. The world tells the clock with `advance()`.
- `getDarkness()` returns 0 in the day, a number that climbs from 0 to 1 during dusk, 1 at night, and falls from 1 to 0
  at dawn. Drawing code multiplies by it, so the light changes smoothly and no code needs "if night then...".
- After night 7 ends, `finished` becomes true, and that is the win condition.

**Without it:** the old test key N flipped a `boolean night`, which cannot do dusk, dawn, or counting nights.

**Try it:** in `Constants`, change `DAY_FRAMES` to `10 * TARGET_FPS` and run. The day becomes 10 seconds.

---

## 13. One run of the game (`GameWorld`)

**What it does:** holds everything that exists in one run: the clock, ships, nets, resources, post, radio, the beam.
`GamePanel` makes a `new GameWorld(map)` when you press Enter.

**Why we did it:** `GamePanel` was becoming a giant class that did windows, keys, drawing and all game rules. A class
that does everything is the thing the "OOP design" rubric item warns about. Now:

| Class | Its one job |
|---|---|
| `GamePanel` | The window: loop, scaling, keys and mouse, screens |
| `GameWorld` | The rules: what happens each frame, what a click does |
| `Hud` | Showing numbers on top |

**Bonus:** a new game is just `new GameWorld(map)`. Everything resets, with no code to "clear each list by hand".
The map is passed in because it never changes, so we load it only once.

**How a frame goes (`GameWorld.update`):**
1. Aim and update the beam.
2. Ask the clock to tick. If the phase changed, run `onPhaseChange` (a banner, refill resources at dawn, reset the spawn counter at night).
3. At night, maybe spawn a skiff.
4. Move ships, remove dead ones, hurt the post for ships that landed.
5. Count down the gun, the banner and the popups.
6. Is the night over? Did we lose?

**Why `checkNightOver` is its own method:** small methods with clear names read like a story. You can explain `update` to
the professor in six sentences.

---

## 14. Waves and resource spots in text files (`WavePlan`, `ResourceNode.load`)

**What it does:** reads `data/waves.txt` (how many skiffs each night, and how fast) and `data/nodes.txt` (where bamboo
and the ore veins are).

**Why text files, again:** Gio can make the game harder by editing one number in Notepad, with no Java and no recompile.
It also covers the rubric's **file I/O** (15%).

**What is inside the code, step by step:**
1. `getResourceAsStream(path)` opens the file from inside the project (so it works from the JAR too).
2. A `BufferedReader` reads it line by line. Lines starting with `#` are comments and are skipped.
3. Each line is split into words with `split("\\s+")`, and `Integer.parseInt` turns text into numbers.
4. **Every step can fail**, so each is inside `try/catch`. A bad line prints a warning and is skipped. A missing file
   uses defaults. The game never crashes because of a typo.
5. `WavePlan` stores its numbers in two **arrays** (`count[night]`, `interval[night]`). The array index is the night number.
6. `ResourceNode.load` also **checks the map**: bamboo and ore must be on land (fish are not in the file: they swim by themselves). A spot on the wrong tile
   is skipped with a message. We trust the data less than we trust ourselves.

**Try it:** open `waves.txt`, change night 1 from `4` skiffs to `1`, run, and play.

---

## 15. Harvesting: hold, follow, run out (`ResourceType`, `ResourceNode`, `FishSchool`, `Harvestable`, `Inventory`)

**What it does:** resources are no longer one click. You **hold** the mouse on a spot until a ring fills.

| Thing | Where | What makes it a decision |
|---|---|---|
| Bamboo | Groves on land (`data/nodes.txt`) | Takes 1.5 s per unit. Grows back slowly |
| Nickel, copper, chromite, gold | Ore veins on land | Take 2.5 to 4 s per unit. **Never grow back**, so every vein is finite |
| Fish (5 kinds) | Swim around the sea | Schools **move**. Keep the cursor on one or the ring drains. They vanish after about 28 seconds |

**The old click-and-done was too easy** because resources were always there. Now time, the ring, and running out make the day matter.

**How one frame of harvesting works (`GameWorld.updateHolding`):**
1. Is the button held, the tool "harvest", and is it not night? If not, the ring drains.
2. Ask `findHarvestable(mouse)`: "what is under the cursor?" Fish first, then land spots.
3. If it is the same thing as last frame, `holdProgress++`. If it is a new thing, restart at 0.
4. When `holdProgress` reaches that resource's `holdFrames`, `collect()` gives the player one portion, and the ring resets.
5. If the cursor slips off, `holdProgress` drops by 3 each frame. Follow the fish and you keep your progress.

**Why `ResourceType` has no if-else:** every resource is **one line of data** in the enum (name, group, color, hold time,
yield, meals, regrow time). The code that harvests does not care which resource it is. To add a new ore, add one line.
Behavior that is truly different (swimming) got its own class, `FishSchool`.

**The `Harvestable` interface:** a bamboo grove sits still, and a fish school swims, but both can say "am I near this point?",
"is there anything left?" and "give one portion". Because both promise that, `GameWorld` has **one** harvesting method for both.

**Fish (`FishKind`, `FishSchool`):**
- Each kind has a speed, a depth range (1 = beach, 6 = open sea), and a weight (how common). Tuna is fast and lives deep. Lapu-lapu is slow, rare and
  deep, and is worth 8 meals. Tamban is common and shallow, worth 1.
- `spawnFish()` picks a kind with a **weighted random roll**, then searches for a water tile at the right depth.
- A school **wanders**: its turning speed drifts a little each frame, so it curves like a real swimmer. If its next step is land, too deep,
  too shallow or off screen, it turns away.
- Fish are counted in **meals** (`Inventory.getMeals`). Repairing eats the **cheapest** fish first (`spendMeals`), so you keep the big catches.

**Ore uses (so the four ores matter):**

| Ore | Use | Key | Cost | Effect |
|---|---|---|---|---|
| Nickel | Mine buoy | 2 | 2 | See section 19 |
| Copper | Rewire the radio | C | 2 | Radio +25 |
| Chromite | Armor the post | V | 3 | Post max health +4 (up to 3 times) |
| Gold | Upgrade the lamp | G | 2 | Beam wider and longer (up to 2 times) |

All the costs are in `Constants`. `Beam` and `Post` had fixed numbers before. To allow upgrades, those became ordinary fields that an
upgrade method changes. That is why the old `static final` numbers were renamed `BASE_...`.

**Art trick:** one grey `ore.png` can make all four ores. `Assets.tint` blends each pixel toward the ore's color, so Jaron needs one rock, not four.

**Try it:** in `ResourceType`, change `TUNA`'s hold time from `100` to `20`, run, and see how much easier tuna becomes.

---

## 16. Nets and the `Defense` interface (`Defense`, `Net`)

**What it does:** you spend 3 bamboo to place a net on **shallow** water (up to 3 tiles from shore). Ships cannot sail
through it. They stop and cut it, which takes about 3 seconds, and that time is what you buy.

**The interface idea in plain words:** an `interface` is a **promise of abilities**. `Defense` says: "anything that is a
Defense can say which tile it fills, whether it blocks ships, take damage, say if it is destroyed, and draw itself."
`Net` makes that promise. Later `Post`-guns, mines and decoy lights make the same promise. `GameWorld` keeps **one list of
`Defense`** and never needs to know which kind each one is. That is **polymorphism** again, this time with an interface
instead of a parent class, and both are on the rubric.

**Inheritance or interface?** `Warship` is a **parent class** because ships share real code (moving, the wake).
`Defense` is an **interface** because a net and a mine share abilities but no code.

**How a ship meets a net (`Warship.findBlocker`):**
- Each frame the ship looks a little ahead of its bow (and a little to each side) and converts those points to tile
  numbers: `col = floor(x / 16)`.
- If a `Defense` that blocks ships fills one of those tiles, the ship stops moving, counts 45 frames, and calls
  `damage(1)` on it. After 4 hits the net is destroyed, `GameWorld` removes it, and the ship moves on.
- The net draws fewer ropes as it weakens, so the player can see it failing.

**Why placing is checked in `canPlaceNet`:** water only, shallow only, not on top of another net, not on a fish spot.
The same method colors the preview square green or red, so the rule is written once and used twice.

---

## 17. The post, the radio, and the HUD (`Post`, `Radio`, `Hud`)

**The story reason:** the post is the grounded ship that is the island's last command post. The radio is the voice saying
"May natitira pa tayo". If either dies, the island is silent.

**The rules (all numbers live in `Constants`):**

| What happens | Effect |
|---|---|
| A ship reaches the shore | Post loses its landing damage (skiff 3, cutter 2, barge 6). Radio loses 3 times that |
| Post reaches 0, or Radio reaches 0 | You lose |
| Night 7 ends with both alive | You win |
| You press R in the day (costs 2 meals of fish) | Post +4, Radio +15 |

**`Hud` only reads, and never changes the game.** It draws from getters on `GameWorld`. If the HUD has a bug, the game
rules still work. That separation is called **separation of concerns**, and it is how you keep a big program fixable.

**The player's weapon (a placeholder for now):** at night, left-click a ship that the beam is lighting. It takes 1 damage
(skiffs have 3 health), with a 30-frame cooldown. **You cannot shoot what you cannot see**, so the beam matters even
for fighting. Later this is replaced by the deck gun and more defenses from the design document.

**How we checked it:** a scripted test played all 7 nights with a bot. A bot that aims perfectly but builds nothing and
never repairs wins about two of three runs. A player who does nothing loses on night 2. A real player with nets and
repairs should do better than the bot.

---

## 18. More ships: variety through inheritance (`Cutter`, `Barge`)

**What it does:** the night now mixes three ship types.

| Ship | Speed | Health | Landing hurts the post by | Counter |
|---|---|---|---|---|
| Skiff | normal (0.55) | 3 | 3 | Anything |
| Cutter | fast (1.0) | 2 | 2 | A mine, or a lucky shot |
| Barge | slow (0.3) | 8 | 6 | Nets (stall it) and mines |

**Why this was cheap to build:** all moving, lighting, wake and net-cutting code is already in `Warship` (section 10).
`Cutter` and `Barge` each have **only a constructor, a name, and a `drawShape`**, about 40 lines each. That is the payoff
of inheritance. We pay once in `Warship`, and every new ship is nearly free.

**New small idea:** `landingDamage` is a field in `Warship` that a subclass sets in its constructor (`landingDamage = 6;`).
The world asks `ship.getLandingDamage()` and does not care which ship it is. A barge hurts more without a single `if (ship is a barge)` anywhere.

**The wave queue (`WavePlan.Kind`, `GameWorld.buildQueue`):**
1. `waves.txt` now says, per night, how many skiffs, cutters and barges.
2. At dusk-to-night, `buildQueue()` puts that many of each `Kind` into an `ArrayList`, then `Collections.shuffle` mixes them.
3. Every few seconds, `spawnShip(kind)` takes the next one. A `switch` creates the right class.
4. The night ends when the queue is empty and the last ship is gone.

---

## 19. A second defense: the mine (`Mine`)

**What it does:** key 2, cost 2 nickel, placed on water near the island. It floats there. When any ship gets within 14 pixels it
explodes and hurts every ship within 30 pixels (3 damage), then it is gone.

**Why it matters for the rubric:** `Net` waits to be attacked. `Mine` acts on its own. Both are `Defense`, so `GameWorld` still holds
one list. To allow this, the interface got one new method with a **default** (empty) body:

```java
default void update(List<Warship> ships) { }
```

`Net` ignores it. `Mine` uses it to look for ships. **We did not change `Net` at all**, which is why interfaces are useful: new
behaviour can be added without breaking old classes.

**Detail:** a mine "destroys itself" by exploding, so `isDestroyed()` returns true. When `GameWorld` removes it, it checks
`d instanceof Mine` and starts an `Explosion` (an expanding ring) at that spot.

**Why ore matters:** each resource has a job: fish heals, bamboo makes nets, nickel makes mines, copper fixes the radio, chromite armors the post, gold upgrades the lamp. That is the day's decision: **what do you spend your limited harvest time on?**

---

## 20. The tool system (`GameWorld.Tool`)

Before, the code had `boolean netMode`. With two tools, a boolean cannot say which. A small `enum Tool { HARVEST, NET, MINE }`
can, and so can a third tool later. Pressing the same key again puts the tool away, and right-click always does.

---

## 21. Art that plugs itself in (`Assets` everywhere)

Every drawn thing now tries to load its sprite first, and **draws its shapes if the file is missing**. So Jaron can add PNGs one at
a time and each appears with **no code change**. The exact file names and sizes are the list in `07-art-prompts.md`.

---

## 22. What we plan to add (and what each will teach)

| Feature | Concept it shows | Rubric area |
|---|---|---|
| `Cutter`, `Barge` | More subclasses of `Warship` | OOP |
| More `Defense` types (coastal gun, decoy light) | Interfaces, more polymorphism | OOP |
| `ScoreManager` | File read/write with try-catch | File I/O |
| `SoundManager` | `javax.sound.sampled` | Audio requirement |
| BFS pathing | Queues and graph search | Data structures |

---

## Section template (copy for each new feature)

```
## N. Feature name (`ClassName`)

**In one sentence:**
**Why we did it this way:**
**How it works:** (key lines only)
**Analogy:**
**Questions the professor might ask:**
```

## Glossary

| Term | Meaning |
|---|---|
| Frame | One picture of the game. 60 per second. |
| Game loop | The repeat of update then draw. |
| Internal screen | The small 640x360 image we draw on. |
| Tile | A 16x16 square of the map. |
| Nearest neighbor | Scaling that enlarges pixels without blur. |
| EDT | Event Dispatch Thread, Swing's one thread. |
| Enum | A fixed list of named values. |
