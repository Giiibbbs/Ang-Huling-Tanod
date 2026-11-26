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
3. **Simple math for art.** Every tile is 10x10 pixels, so the map is 64 by 36 tiles on the small screen.

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

**In one sentence:** the map is a grid of 10x10 tiles (64 by 36) read from a text file, and `TileMap` draws the island, shades the
water by depth, and adds foam along the shore.

**Why we did it this way:**

- **A text file, not code.** The island lives in `atingdagat/data/island.txt`, one character per tile (`~` water,
  `:` sand, `#` grass). Anyone on the team can open it in Notepad and reshape the island without touching Java. It is
  also the file I/O the rubric asks for.
- **A 2D array.** A grid is the natural fit: `tiles[row][col]`. Looking up a tile is instant.
- **Depth in 16 px units.** `getDepth()` counts steps from the shore in tiles and then converts to units of `Constants.DEPTH_UNIT_PX` (16 px).
  So "nets go 3 deep" means about 48 px whatever the tile size is. We made the tiles smaller (16 px to 10 px) and no rule in the game had to change. Positions in `Constants` (`POST_COL`, `LIGHTHOUSE_COL`) and in `nodes.txt` are tile numbers, so those were converted once.
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

- **It follows the mouse instantly.** It first turned at a fixed speed (so the player had to plan where to look), but that felt sluggish,
  so `TURN_SPEED` is now a half turn per frame: the beam is always where the mouse is. The decision moved elsewhere: where to stand, what to light, and the fog night.
- **The beam knows the rules, and other classes ask it.** `beam.lights(x, y)` is the single place that decides what
  counts as lit. Ships and the overlay both use it, so they can never disagree.
- **The tuning numbers are named constants at the top** (`TURN_SPEED`, `HALF_ANGLE`, `LENGTH`, `LAMP_GLOW`), so Gio can
  say "make the beam wider" and it is a one-line change.

**How it works:**

*Aiming:* the angle from the lamp to the mouse comes from `Math.atan2(dy, dx)`.

```java
targetAngle = Math.atan2(y - originY, x - originX);
```

*Turning:* each frame the beam moves toward the target by at most `TURN_SPEED` (now so big that it snaps; lower it to make the beam heavier).

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
| Trees | `TREE` lines in `data/nodes.txt` | Take 1.8 s per chop and give 3 bamboo each. A `ResourceType` line: `getStock()` says a tree puts **bamboo** in the bag, so the player has one building material. Three chops, then a stump that regrows |
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

## 22. The walking tanod and the lights (`Player`, `Light`, `Lantern`, `Torch`)

**The problem we solved.** At first the only light was one beam that followed the mouse. A beam is a thin cone, so most of
the sea was always dark, and the player had to circle the beam round and round. That is a chore, not a decision.

**The fix, in plain words.** Three kinds of light now work together, and a ship does not care which one finds it.

- **`Light` (an interface).** One promise: `boolean lights(double x, double y)`. The beam, the lantern and the torch all make
  this promise. In `Warship.updateLight` a ship only asks "does any light touch me?". This is polymorphism: the ship
  code never mentions Beam, Lantern or Torch. `GameWorld.isLit` asks them all.
- **`GlowSource` (a second interface).** For lights that are a round pool (lantern, torch). `NightOverlay` reads their
  position and radius to cut a hole in the darkness for each one.
- **`Beam`** now has `update(boolean manual)`. Nobody steering: it adds `SWEEP_SPEED` to its angle every frame, so it
  turns by itself (one circle in about 5 seconds). Steering: it turns toward the mouse like before.
- **`Player`** holds the tanod's feet position, faces the way it last moved, and loads 4 sprites (`down`, `up`, `left`, `right`
  with an optional second walking frame). **Collision** is simple: each frame we try the X move and the Y move separately,
  and cancel a move if either foot would stand on a water tile. Trying X and Y separately lets the tanod slide along the beach
  and not get stuck. Diagonal walking divides by the length of the move, so it is not faster than walking straight.
- **`Lantern`** has no body. It points at the Player and lights a circle of `LANTERN_RADIUS` around it.
- **`Torch`** is a `Defense` (it sits in the same list as nets and mines), a `Light` and a `GlowSource`. One class, three promises.
  It stands on land, never blocks ships, and cannot be destroyed.

**Reach.** `GameWorld.inReach(x, y, radius)` is the distance between the tanod's feet and a point. Harvesting, building and
shooting all call it, with a different radius from `Constants` (`LAND_REACH`, `FISH_REACH`, `BUILD_REACH`, `FIRE_RANGE`). This is why
walking matters: the tanod must go to the thing.

**Steering the beam.** `GameWorld.update` sets `manual = steering && isNearLighthouse()`. SPACE held at the tower
steers. Anywhere else, the beam sweeps.

**Keys reach the world** through `GamePanel`, which reads the `keys[]` array each frame and calls `world.setInput(moveX, moveY, steering)`.

**Why this is good OOP.** Adding a new light later (a flare, a searchlight on a ship) means one new class that implements `Light`.
No ship code changes.

---

## 23. The day dial and the Workshop panel (`Hud`, `MarketPanel`)

**The day dial** is in `Hud.drawClock`. A circle is clipped (so nothing draws outside it) and painted in layers: the sky, the stars,
the sun or moon, then the ground on top (which hides the sun when it sinks below the horizon). The sky colour comes from
`DayCycle.getDarkness()` mixed with orange at dusk and dawn (`mix` blends two colours: `a + (b - a) * t`). The sun's place comes
from the phase and `progress()`: by day it moves along an arc (`cos` and `sin` of an angle that goes from PI to 0), at dusk it sinks,
at dawn it rises. The ring around the dial is an arc whose length is the progress (at night, the share of ships still to come).
`DayCycle.getSecondsLeft()` gives the "0:32 to dusk" text.

**The Workshop** is `MarketPanel`. An `enum Offer` holds the fixed facts of each row (key, label, the resource it costs, the price, a help text),
so adding an item is one new line there plus one case in `use`. Each row shows:
- a progress bar of `have / cost` (green when you can afford it, orange when not, grey when maxed),
- the numbers ("6/3 bamboo"),
- a status on the right (torches built, a level, or health).

**Clicking.** `GamePanel` asks `market.click(...)` first. It returns `true` when the click landed on the panel, and then the world does
NOT also treat it as a click on the map. If you click the panel while holding nothing, nothing else happens. Rows call the same
methods as the keys (`toggleTool`, `repair`, `rewire`, `armorPost`, `upgradeLamp`), so the keys and the panel can never disagree.
The panel only draws in the day, and **B** hides it.

**The cost label** next to the cursor while placing (`GameWorld.drawBuildGhost`) says the price and what you have.

---

## 24. Night 7 and the ending (`EndingScene`)

The Bakunawa event that used to live here was removed on Nov 26. Night 7 now ends like any other night: `checkNightOver` sees the spawner is done and the sea is empty, `onPhaseChange` advances the `DayCycle`, and the win is detected at the dawn after night 7. The `Debug` keys are now N, L, K, H and J.

## 25. Supply crates (`SupplyCrate`)

**The problem.** Ore never grows back, and the map holds less chromite (6) and gold (3) than the upgrades cost (9 and 4). A player could
run out of something they need with no way to get more.

**The fix.** A `SupplyCrate` floats in from the sea about every 24 seconds. You cast a line (same reach as fish) and hold the mouse on it.
It is a `Harvestable`, so **the hold-the-mouse code did not change at all**. Only one thing was new in the interface: a `default` method
`getHoldFrames()`. A default method gives every class a ready-made answer (the number in its `ResourceType`), and a class that wants something else
(the crate opens faster than an ore vein) overrides it. That is the whole reason the interface has it.

**Smart contents.** `GameWorld.pickCrateContents()` is a weighted random pick (nickel 4, copper 4, chromite 2, gold 1), but if
`oreLeftOnMap(type)` is 0 (all its veins are empty) that ore's weight is multiplied by 3. We measured it: gold goes from 8.9% to 23% of crates
once the gold veins are empty. Rare ores stay rare, but you can always get them.

**Timing.** `updateCrates()` counts `crateTimer`, spawns when it passes the interval (shorter when the radio is strong, so protecting the radio pays),
keeps at most 2 at once, and clears them at night. A crate drifts toward the island and stops in the shallows, bobs, and blinks in its last 6 seconds.

---

## 26. Survivors and the Pana tower (`Survivor`, `Bangka`, `PanaTower`)

**The story.** After night 2, 4 and 6 a bangka (fishing boat) lands at dawn. Each carries a survivor of the Kordan blockade, a made-up
character inspired by our classmates. A caption shows who they are and what they say.

**Abstract class.** `Survivor` is `abstract`: you can never write `new Survivor()`. A "survivor" is an idea, and each real one has a job.
What every survivor shares lives in the parent (walking home over land, drawing the body, the arrival text).
The one different thing is a single method, `act(post, radio, ships, frame)`:

| Class | `act()` does |
|---|---|
| `KaInday` | every 6 s: `post.repair(1)` and `radio.restore(2)`. Also a `GlowSource`, so she glows softly |
| `Migo` | every 4 s: `radio.restore(1)`. Overrides `boostsCrates()` to return true |
| `Tonyo` | finds the nearest **lit** ship within 95 px and hits it for 1 |

`GameWorld` keeps one `ArrayList<Survivor>` and just loops `sv.act(...)`. It does not know who is who. That is the same polymorphism
as `Warship` and `Defense`. `boostsCrates()` is a normal method with a default answer (false), and only `Migo` overrides it,
so `updateCrates()` asks every survivor and Migo changes the crate speed without the world knowing his name.

**The bangka.** `Bangka` sails in a straight line along the post's row from beyond the screen edge to the first shore tile. When it arrives
`releaseCargo()` hands over the passenger once. Then the boat lingers and fades. Even nights come from the west, odd ones from the east.
The survivor then walks to a spot near the post, and `update()` refuses to step on water.

**The Pana tower.** `PanaTower implements Defense` (the fourth one). Like `Mine`, it does its work in `update(ships)`: find the nearest ship in range, hit for 1,
wait 100 frames. It does **not** check `isLit()`, so it works in the dark, which you cannot do by hand. It uses the same "free land" rule as the torch (`canPlaceTorch`).

**Test key.** J brings all three survivors to the post at once, so you do not have to play to night 6 to see them.

---

## 27. Upgrades and day workers (`Upgrades`, `Survivor.work`)

**Upgrades.** `economy/Upgrades.java` keeps five levels (0 to 3) in an array indexed by the `Gear` enum (PICKAXE, AXE, BOOTS, ROD, RIFLE; the rifle adds 70 px of gun range per level and needs chromite). The enum holds each tool's fixed facts. The class holds the changing levels and answers three questions for the world: `holdMultiplier(target)` (how long a harvest takes), `speedMultiplier()` (walking) and `extraFishReach()`. Costs are arrays: bamboo 4, 6, 8 and ore 0, 1, 1 for levels 1, 2, 3 (nickel for pickaxe and rod, copper for axe and boots). `GameWorld.holdFramesFor()` is the one place where a harvest time is worked out, so the progress ring and the real time always agree.

**Workers.** Each survivor has a small state machine in `Survivor.work()`: IDLE, GOING, CHOPPING, CARRYING. At the start of a day `GameWorld.payCrew()` takes 1 meal per survivor (if there are none, that survivor rests). A paid survivor finds the nearest free tree or bamboo near the post that can be walked to over land (a line check, so nobody teleports across water), chops it (4 s), carries the bamboo home and adds it to your bag. The quota is 3 loads a day. At dusk they walk home for their night jobs. A green dot in the Workshop crew strip means paid and working, red means resting.

**Why this design.** The meal wage is a sink for fish (meals used to pile up). The quota stops workers from stripping the island. Polymorphism is unchanged: the world still calls `act()` on every survivor and does not care who is who.

## 28. Folklore (`Punso`, `LapuLapuStone`, `Manananggal`)

**The punso and the duwende** (`entities/Punso.java`). An anthill on the west shore. It is a `Harvestable` (holding the mouse on it is how you leave the gift) and a `GlowSource`. It stirs after night 1 (`monsterEvent(1)`), and 3 bamboo wakes the duwende (`GameWorld.giveGift()`). Each morning `GameWorld.duwendeGift()` adds 1 ore: usually the one of copper, nickel and chromite you have least of, and gold 15% of the time. It is a small way to fix ore running out. It is not a `Defense`, because it never fights: forcing it into that interface would be wrong, so `GameWorld` just holds it and draws it.

**The Lapu-Lapu stone** (`defense/LapuLapuStone.java`). A grey statue with a spear on the northeast islet. After night 3 `monsterEvent(3)` tells the story: Lapu-Lapu was turned to stone, not killed, and some say he is only sleeping. He does nothing yet. He is a `Defense` only so the world already updates and draws him; ships cannot reach the islet. This replaced the kapre and balete tree. The plan is to let the player free him later (a task, then he fights), which is why he is a class of his own and not just a drawing.

**The manananggal** (`entities/Manananggal.java`). She extends `Survivor`, so the world already knows how to update and draw her. She overrides three things: `update(map)` (she flies over the sea, a walking survivor cannot), `act()` (a small state machine: ROOST, SWOOP, RETURN) and `draw()`. `needsWage()` is false, so `payCrew()` and `work()` skip her. She arrives at dawn after night 5 from `monsterEvent(5)`.

**Why this design.** Story events are plain methods called from the dawn phase, like the bangka. New allies need no change to the main loop: they are either a Defense or a Survivor. Optional art: `res/monsters/lapulapu_stone.png` (36x44), `res/monsters/punso.png` (22x22) and `res/monsters/manananggal.png` (30x26), both on flat magenta (255,0,255). Without them the code draws both. Test key `H` wakes the duwende and brings her at once.

## 28b. Nights that escalate, Filipino trees, smaller shop (`Twist`, `ResourceType`, `MarketPanel`)

**Pulses and twists (FNAF style).** Ships no longer trickle in one by one. `GameWorld.pulseSize(night)` gives 1, 2, 1, 1, 3, 4, 4 ships per pulse (nights 3 and 4 use 1 for a steady stream), and `spawnShips()` sends each pulse from different sides (never more than 4, there are only 4 sides). The wait between pulses is `waves.txt` seconds times the pulse size, so the average pace still matches the file. Each night also has one `Twist` (enum with a title and a story line): NONE, TWO_FRONTS, FOG (beam 30% shorter via `Beam.setFog`), STATIC (radio drains while ships remain), SURGE (ships 25% faster), HARDENED (+1 health through `Warship.powerUp`), ASSAULT (bigger, quicker pulses). Teaching point: an enum can carry data, so adding a twist is one new line plus one small `case`.

**Trees.** `ResourceType` now has Narra and Acacia (give bamboo) and Mango, Coconut, Banana (give FRUIT, which counts as meals). `isTree()` replaced the old single TREE check. `ResourceNode.drawTree` draws each kind differently in code, so the game works with no art files.

**Shop.** `MarketPanel` rows are now single-line chips with a thin progress bar, so the panel is about 188 px tall instead of 258.

**Balance changes.** Manananggal: 1 damage, 4 s cooldown, 4 swoops per night. Duwende: a different gift each morning in a 4-day rotation (ore, fruit, supply crate, repairs). Waves in `waves.txt` were lowered after a headless bot test; a crude bot with full upgrades now reaches nights 6-7.

## 28c. Radio voices, How to Play and combat feel (`RadioVoices`, `HelpScreen`, `Warship`)

**Radio voices** (`core/RadioVoices.java`, `data/voices.txt`). A text file with one voice per line: `night | island | speaker | what you hear | what they say at the end`. `RadioVoices` reads it with a `BufferedReader` into a `List<Voice>`; a bad line is skipped and a missing file gives an empty list (the game never crashes). At dusk `GameWorld.onPhaseChange(DUSK)` shows tonight's voice with `tellStory`. `EndingScene` builds its story lines from the same list, so the ending always matches the voices the player heard. This is our data-structures and file-I/O story for the paper: a file parsed into a typed list, with safe fallbacks.

**How to Play** (`ui/HelpScreen.java`, `GameState.HELP`). A new state. `GamePanel.openHelp()` remembers which screen to return to (`helpReturn`), the game is frozen while it shows (only `PLAY` updates the world), and the screen just draws text from arrays at the top of the class. Open with H on the menu or F1 anytime.

**Combat feel.** Three small ideas, all in existing classes. (1) `Warship.hit()` now records `flash` and `pendingDamage`; `GameWorld.updateShips()` turns `takePendingDamage()` into a floating "-2" `Popup` and draws a white flash, so every damage source (gun, mine, Pana, manananggal) shows without changing those classes. (2) `GameWorld.addShake(power, frames)` plus `getShakeOffset()`; `GamePanel` moves the world picture by that offset (not the HUD). Ship deaths, mines and post hits shake. It uses its own `Random` so shaking never changes the waves. (3) A pulsing ring is drawn around ships the beam is lighting (`beam.lights`).

## 28d. Wear, Raiders, Ghosts and the flare (`Durable`, `Raider`, `Ghost`, `Flare`, `Warship.act`)

**`Durable` interface.** Net, Torch and PanaTower implement it: `getDurability`, `getMaxDurability`, `restore`, `wear` (floors at 1), `repairCostPerPoint`, plus default methods `isDamaged` and `drawWear` (a row of pips). `GameWorld` loops over `List<Defense>` and handles anything `instanceof Durable`, so a new defense gets wear and repair by adding "implements Durable". At dawn every Durable calls `wear(1)`. Holding X (`setRepairHeld`) finds the nearest damaged Durable within `REPAIR_REACH` and, every `REPAIR_FRAMES`, spends bamboo to `restore(1)`.

**`Warship.act(defenses)` hook.** `update()` now does `if (!act(defenses)) move(map, defenses);`. The default `act` returns false (ship just sails). `Raider` overrides it: it picks the nearest live Pana tower or torch within 85 px, turns toward it, and every 100 frames damages it by 1, storing a shot for `GameWorld` to draw as a tracer and a "-1" popup. Teaching point: one small overridable method (the Template Method pattern) lets a subclass change behavior without touching the parent's loop.

**`Ghost`** only changes two protected fields, `litRise` and `litFall`, so it is slow to become visible and fades fast. **`Flare`** implements `Light` and `GlowSource`, flies an arc for 24 frames, then burns; `GameWorld.isLit` and `glowSources` include it. `waves.txt` has two optional columns, raiders and ghosts (default 0).

**Ore powers (the night side of ores).** `GameWorld.applyOreBonuses()` runs every frame and pushes three ore levels into the defenses: chromite armor level becomes `Durable.setPlating(level)` (max durability +1 per level), copper wiring becomes `Torch.setRadiusScale(...)` (wider light), and the gold lamp level scales the flare's `life`. The lesson is the same as before: the world loops over `Durable` and `Torch`, and each class decides what the number means for itself. Ore hold times in `ResourceType` were cut by a third because a 75 s day was fully used by bamboo and fruit, so ores were never mined.

## 28e. The high-score table (`ScoreEntry`, `HighScores`, `Debug`, the NAME screen)

**File I/O.** `HighScores` reads `highscores.txt` with a `BufferedReader` when the game starts and writes it with a `PrintWriter` every time a score is added. Both are in try-with-resources and `try-catch`: a missing file just means an empty table, and a damaged line is skipped (`ScoreEntry.fromLine` returns `null`), so the file can never crash the game. Format, one run per line: `name|nightsSurvived|postHealth`.

**Data structure.** An `ArrayList<ScoreEntry>` kept sorted and cut to `MAX_SCORES` (5). `ScoreEntry implements Comparable<ScoreEntry>`: more nights first, then more Post health, so `Collections.sort` puts the best run at index 0. `getTop()` returns a copy, so other classes cannot change the real list (encapsulation).

**The flow.** A new `GameState.NAME` screen. When a run ends (GAME_OVER, or the end of the VICTORY story) `GamePanel.finishRun()` builds a `ScoreEntry`. If `qualifies()` says it made the table, the player types a name (`keyTyped`, letters, digits, space, - and _, up to 12 characters) and presses ENTER; ESC skips. The menu then shows the top 5. `GameWorld.getNightsSurvived()` gives the score: all 7 on a win, otherwise the nights before the one that broke us.

**`Debug`.** The developer keys (N, L, E, K, H, J) only work after F12 (or the ` key) turns them on, so a player cannot skip nights by accident. A red note shows while they are on.

## 28f. Mines that chain, endless mode and the mender (`Mine`, `DayCycle`, `WavePlan`, `GameWorld`, `Torch`)

- **Chain reaction (`Mine.detonate`, `Mine.reaches`, `GameWorld.updateDefenses`).** `update()` only decides WHEN a mine goes off; `detonate(ships)` does the blast and can be called by someone else. `updateDefenses()` first updates every defense, then loops: any mine that has exploded sets off every unexploded mine its blast reaches, until nothing new goes off (a `while` loop over two nested `for` loops), then the dead ones are removed. Cost is `Constants.MINE_COST_NICKEL = 1`.
- **Endless mode.** `DayCycle` got an `endless` flag and `continueEndless()` (night = 8, phase DAY, `finished = false`); `advance()` no longer sets `finished` when `endless` is true. `GameWorld.startEndless()` turns the outcome from WON back to PLAYING. `WavePlan.getCount/getInterval` scale for nights above 7 (a formula, not more lines in `waves.txt`). `Twist.forNight` cycles `all[1 + (night - 2) % 6]` for those nights. `ScoreEntry.getNightsText()` writes "N (endless)". In `GamePanel` the VICTORY case reads E, the PAUSE case reads Q (only when endless) and both call existing methods (`startEndless`, `finishRun`).
- **Mender (`Survivor.mendsDefenses`, `GameWorld.updateMenders`).** Another virtual method with a default (`false`) that only `Tonyo` overrides, so the world asks every survivor the same question without `instanceof`. It looks for the `Durable` with the lowest `durability / max` and calls `restore(1)`.
- **Parol (`Torch.draw`).** The star is a 10-point `fillPolygon` that alternates a long and a short radius around the centre; the colour comes from `PAROL_COLORS[(col + row) % 4]`.

## 28h. Splitting GameWorld (`ShipSpawner`, `Announcer`, `SeaLife`)

`GameWorld` had grown to about 72 KB, more than one class should do. On Nov 23 we moved three self-contained jobs into their own classes in `core/`, and changed nothing about how the game plays (the bots gave the same results before and after):

| New class | Job | What GameWorld still does |
|---|---|---|
| `ShipSpawner` | Builds tonight's queue from the `WavePlan`, sends ships in pulses from different sides, applies the Twist (stronger ships) | Calls `spawner.startNight(night)` at dusk's end and `spawner.update(...)` every night frame; asks `allSpawned()` and `halfSpawned()` |
| `Announcer` | The big banner and the story card (text and a fade-out timer) | Calls `announcer.banner(...)` and `announcer.story(...)`; the HUD getters pass through |
| `SeaLife` | The fish schools and the supply crates: spawn, move, remove, draw, find what is under the mouse | Calls `sea.update(...)`; if something swam away while being harvested it stops the harvest |

The idea is **composition**: GameWorld HAS a spawner, an announcer and a sea, and talks to each through a few public methods, instead of owning all of their fields. `SeaLife` cannot see the inventory or the map nodes, so GameWorld hands it what it needs: a `ToIntFunction<ResourceType>` (`this::oreLeftOnMap`) to ask how much ore is left. A **method reference** like that is a short way to pass a method as an argument. It is still a big class (about 64 KB); the next candidates are the folklore events, the survivors and the build tools (see `11-future-improvements.md`).

## 28i. The opening comic (`IntroScene`, `GameState.INTRO`)

The game now starts in a new state, `INTRO`, before `MENU`. `IntroScene` (in `ui/`) holds six pictures and two arrays of text (English captions and Filipino lines), and a counter `index` for the current panel and `timer` for how long it has been shown. `update()` adds 1 to `timer` every frame and calls `next()` after 7 seconds; ENTER or SPACE calls `next()` and ESC calls `skip()`. `draw()` shows the picture with a fade-in (an `AlphaComposite`) and a slow zoom (it crops a slightly smaller part of the picture each frame and stretches it to the screen), then a dark band with the caption, wrapped by a small `wrap()` method that measures words with `FontMetrics`. The pictures are read with `ImageIO.read` from `res/intro/panel1.png` to `panel6.png`; a missing file gives `null` and a dark blue background, so the game never crashes. The pictures contain no letters because the code draws the text. `GamePanel` only needed three small changes: the starting state, a `case INTRO` in the key handler, and a call in `update()` and `drawGame()`.

## 29. What we plan to add (and what each will teach)

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
| Tile | A 10x10 square of the map. |
| Nearest neighbor | Scaling that enlarges pixels without blur. |
| EDT | Event Dispatch Thread, Swing's one thread. |
| Enum | A fixed list of named values. |

## 28g. Bakunawa visits (removed Nov 26)

This section described the omen, the small visits and the `RETREAT` stage. All of it was removed together with the `Bakunawa` class, and `GameWorld` no longer calls `updateEclipse()`.

