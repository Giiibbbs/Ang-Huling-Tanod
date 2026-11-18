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

The skeleton has five classes in the package `atingdagat.core`.

```
Main  ->  GameFrame  ->  GamePanel  <-  GameState
                              |
                          Constants
```

- `Main` starts the program.
- `GameFrame` is the window.
- `GamePanel` is where the game runs and is drawn. Everything else will plug into it.
- `GameState` lists the screens (menu, play, pause...).
- `Constants` holds the shared numbers.

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

## 7. What we plan to add (and what each will teach)

| Feature | Concept it shows | Rubric area |
|---|---|---|
| `TileMap` from a text file | 2D arrays, file I/O, exceptions | Data structures and file I/O |
| `Beam` and night overlay | `AlphaComposite`, geometry, angles | Gameplay, polish |
| `Warship` (abstract) with `Skiff`, `Cutter`, `Barge` | Inheritance, polymorphism | OOP |
| `Defense` interface | Interfaces | OOP |
| `ArrayList<Warship>` | Collections | Data structures |
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
