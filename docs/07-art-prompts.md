# Art Prompts (for Gemini)

*Owner: Jaron. Written by the art director role. Goal: every sprite looks like it belongs in the same game.*

The game draws on a 640x360 screen with 10x10 tiles, in a 3/4 top-down view. Sprites must match that.

## The plan: test first, then bulk

1. Generate the **3 test sprites** below (skiff, lighthouse, villager) using the **style bible**.
2. Run the **checklist**. If they don't look like one family, change the style bible, not the individual prompts.
3. Only when the 3 match, generate the rest of the list (bottom of this file) with the same style bible.

Don't generate 30 images before step 2. If the style is wrong, all 30 are wasted.

## Style bible (paste this at the start of every prompt)

```
STYLE: high-detail pixel art game sprite, 3/4 top-down view (camera about 45 degrees above the object),
crisp clean pixel edges, limited palette of about 24 colors, warm tropical Philippine island setting,
strong readable silhouette, thin dark outline, light coming from the top-left with darker shading on the
bottom-right. Mood: dusk-blue sea, warm lantern-yellow accents.
RENDERING: one single isolated object, centered, filling most of the image, on a flat solid pure magenta
background (#FF00FF). No ground, no water, no cast shadow on the background, no text, no letters, no border,
no frame, no watermark, no glow or soft blur into the background, hard edges only.
FACTION: the invaders are the fictional "Kordan Dominion": charcoal-black and crimson colors, angular
geometric shapes, a simple invented three-triangle emblem. It must not look like any real country's flag or
insignia.
```

## Test sprite 1: Skiff (enemy scout boat)

*In-game size: about 32x16 pixels. Faces right (east), so the game can rotate it.*

```
[STYLE BIBLE]
SUBJECT: a small, fast enemy scout boat of the Kordan Dominion navy. Narrow charcoal-black steel hull,
angular and aggressive, pointed bow facing right, a small open deck with one mounted light gun, a tiny
crimson pennant at the stern, the invented three-triangle emblem on the side. Seen from above at a 3/4 angle.
The boat is about twice as long as it is wide.
```

## Test sprite 2: Lighthouse (the beam's source)

*In-game size: about 28x48 pixels.*

```
[STYLE BIBLE]
SUBJECT: a small weathered stone lighthouse on a rocky base on a tropical island. Round stone tower with faded
white and muted red bands, a glass lamp room at the top glowing warm yellow, a small balcony rail, bamboo
scaffolding and a few wooden crates at the base, a little moss on the stones. Seen from a 3/4 top-down angle so
we can see the roof of the lamp room and the front of the tower.
```

## Test sprite 3: Villager (harvester)

*In-game size: about 16x24 pixels. Respectful, not a caricature.*

```
[STYLE BIBLE]
SUBJECT: a Filipino island villager worker, adult, facing the camera and standing straight, wearing a
salakot (wide woven hat), a simple light shirt and rolled-up trousers, barefoot, carrying a small woven basket
on one hip. Friendly, dignified expression, simple clothes in warm earth tones. Seen from a 3/4 top-down angle.
```

**One rule for every image:** do **not** use magenta, hot pink, or purple anywhere on the object. The game removes the
magenta background by color, so a pink sail or purple cloth would turn see-through. (Crimson and red are fine.) Add this
line to the style bible if the tool uses pink:

```
COLOR RULE: the object itself must contain no magenta, hot pink, or purple.
```

## Checklist: do the three look like one game?

Look at them **side by side**, then **shrunk to their in-game size** (32x16, 28x48, 16x24).

- [ ] Same outline thickness and same level of detail
- [ ] Same palette feel (not one very saturated and one dull)
- [ ] Same light direction (top-left)
- [ ] Still readable when shrunk to in-game size
- [ ] Background is one flat magenta, with no gradient and no pink halo on the edges
- [ ] Nothing like a real flag or a copyrighted character
- [ ] The scale makes sense next to a 10x10 tile (the skiff is about 3 tiles long). Sprites are bigger than a tile on purpose: a tree or a ore rock overlaps its neighbors a little

If it fails, tell Gabriel which item failed. Typical fixes: add "fewer small details" or "bolder shapes" to the
style bible, add "flat colors, no gradients", or reduce the palette to 16 colors.

## Common problems and fixes

| Problem | Fix |
|---|---|
| Different style every time | Always paste the whole style bible. Don't paraphrase it |
| Background isn't solid magenta | Add: "background must be exactly #FF00FF, one flat color, no gradient" and retry |
| Pink halo around the edges | We will remove it in code. Also try "hard pixel edges, no anti-aliasing" |
| Too detailed to read when small | Add: "bold simple shapes, readable at 32 pixels wide" |
| Wrong angle | Say "viewed from above at 45 degrees, like a tabletop strategy game" |
| Text or a logo appears | Add "no text, no letters, no symbols other than the emblem" |
| Sprite sheet instead of one object | Say "ONE object only, not a sprite sheet, not multiple views" |

## From Gemini to the game (step by step)

1. **Generate one object per image.** Pick the version that matches the camera rule for that asset (see the style blocks below).
2. **Download the real image** with Gemini's download button. The rounded corners you see on screen are only Gemini's preview.
3. **Keep the original.** Put the untouched download in `art-source/` at the repo root (next to `docs/`, **not** inside `src/`). Never edit the original.
4. **Make the game copy.** Rename it to the **exact lowercase name** in the "Exact file names" table (for example `tuna.png`). Optionally shrink it to about 4 times
   the in-game size, because a huge image makes the game start slower (the game shrinks it either way).
5. **Copy it into the project:** `AngHulingTanod/src/atingdagat/res/<folder>/<name>.png` (for example `res/fish/tuna.png`).
   Create the folder if it does not exist. It **must be inside `src/`** so it is packed into the JAR.
6. **In NetBeans:** right-click the project, choose **Clean and Build**, then press **F6**. Look at the new art in the game.
   If the output window says `Art not found`, the folder or file name is wrong.
7. **Look at it at real size** (the Checklist above). If it fails, fix the prompt and regenerate. Do not keep a weak image.
8. **Write a row in the prompt log** below: the file, the tool, the date, the style version, whether you edited it by hand, and notes.
9. **Commit and push** from the Favorites tab (so `art-source/` and the docs are included). Suggested message: `Add fish art`.

The game turns the magenta into transparency when it loads the image (chroma key). You do not need to edit the background.

## Prompt log (keep this honest, we show it in CREDITS)

| Asset file | Tool | Date | Style bible version | Edited by hand? | Notes |
|---|---|---|---|---|---|
| ships/skiff.png | Gemini | | v1 | | |
| buildings/lighthouse.png | Gemini | | v1 | | |
| people/villager.png | Gemini | | v1 | | |
| fish/tuna.png | Gemini | 2026-11-19 | fish v1 | No | Chose the top-down version (the side view would swim upside down) |
| fish/tamban.png | Gemini | | fish v1 | | |
| fish/galunggong.png | Gemini | | fish v1 | | |
| fish/bangus.png | Gemini | | fish v1 | | |
| fish/lapulapu.png | Gemini | | fish v1 | | |
| nodes/ore.png | Gemini | | nodes v1 | | |
| nodes/bamboo.png | Gemini | | nodes v1 | | |

## Exact file names the game loads right now

Every file below is already wired up. Drop the PNG in `AngHulingTanod/src/atingdagat/res/` with this exact name and it appears
with no code change. Missing ones show simple shapes. Generate **larger** than the in-game size (4x is fine). The game shrinks it.

| File | In-game size | Notes |
|---|---|---|
| `ships/skiff.png` | 38 x 12 | Points **right**. **Done: Jaron's art is in the game** (the game scales it down, so a big original is fine) |
| `ships/cutter.png` | 40 x 12 | Points right. Slimmer and faster-looking than the skiff. **Done** |
| `ships/barge.png` | 58 x 25 | Points right. Wide, flat landing barge. **Done** |
| `ships/raider.png` | 40 x 14 | OPTIONAL. Points right. Dark red hull with a gun deck. The game draws it by code if the file is missing |
| `ships/ghost.png` | 38 x 12 | OPTIONAL. Points right. Pale, ghostly hull with cyan glowing eyes. Drawn by code if missing |
| `ships/flagship.png` | 100 x 50 | Points right. The Kordan flagship the Bakunawa swallows on night 7. **Done**. Tip: the magenta must be one flat colour. The first flagship had a darker magenta (228, 2, 204), so we flattened it to 255, 0, 255 |
| `buildings/lighthouse.png` | 32 x 48 | Base of the tower at the bottom of the image |
| `buildings/post.png` | 40 x 32 | Grounded ship with a cabin and a radio mast |
| `buildings/net.png` | 16 x 16 | One tile of net between two bamboo posts |
| `buildings/mine.png` | 16 x 16 | Floating spiked mine buoy |
| `player/down.png`, `up.png`, `left.png`, `right.png` | 18 x 22 | **The tanod** (already drawn by us as a placeholder: chibi, headband, teal vest, lantern). Standing pictures. Walking steps are `down2.png`, `down3.png`, `up2.png`, `up3.png`, `left2.png`, `left3.png`, `right2.png`, `right3.png`. Replace any of them with better art |
| `buildings/torch.png` | 12 x 16 | Bamboo torch with a flame, 3/4 view |
| `buildings/pana.png` | 14 x 20 | Bamboo arrow tower: four bamboo legs, a small platform and a bow on top. Optional (we draw one in code) |
| `survivors/kainday.png`, `survivors/migo.png`, `survivors/tonyo.png` | 18 x 22 each | The three survivors, chibi, same style as the tanod, standing pose, facing down. Ka Inday: elder Babaylan, white cloth, red headband. Migo: young, blue polo, headset. Tonyo: green jacket, red cap, rifle across the body. Optional (we draw them in code) |
| `items/crate.png` | 16 x 16 | Floating wooden supply crate with metal corners, 3/4 view. Optional (we draw one in code) |
| `nodes/tree.png` | 20 x 28 | **Narra** (taller than a tile): spreading dark-green crown with small yellow flowers, straight trunk, 3/4 view. The trunk's foot sits on the tile. Optional (we draw one in code) |
| `nodes/acacia.png` | 20 x 28 | **Acacia (rain tree)**: wide flat umbrella crown with pink puffs. Optional |
| `nodes/mango.png` | 20 x 28 | **Mango**: round dense crown with a few orange-yellow mangoes. Optional |
| `nodes/coconut.png` | 20 x 28 | **Coconut palm**: a leaning slim trunk and a fan of fronds with a couple of coconuts. Optional |
| `nodes/banana.png` | 20 x 28 | **Banana plant**: big broad leaves and a hanging bunch. Optional |
| `ui/fruit.png` | 10 x 10 | HUD icon: a small mango or banana. Optional |
| `nodes/bamboo.png` | 16 x 16 | Bamboo grove |
| `nodes/ore.png` | 16 x 16 | **One grey rock with metal glints. The game recolors it into nickel, copper, chromite and gold**, so you need only this one |
| `nodes/nickel.png`, `copper.png`, `chromite.png`, `gold.png` | 16 x 16 | **Optional.** Only if you want each ore to have its own picture. Otherwise the recolored rock is used |
| `fish/tamban.png` | 14 x 5 | Small silver sardine, points **right** |
| `fish/galunggong.png` | 16 x 7 | Round scad, blue-silver, points right |
| `fish/bangus.png` | 18 x 7 | Slim bright-silver milkfish, points right |
| `fish/tuna.png` | 22 x 9 | Big dark-blue torpedo-shaped tuna, points right |
| `fish/lapulapu.png` | 22 x 11 | Orange-red grouper (the rare, valuable one), points right |
| `ui/bamboo.png`, `ui/fish.png`, `ui/nickel.png`, `ui/copper.png`, `ui/chromite.png`, `ui/gold.png` | 10 x 10 | HUD icons. Must read clearly when tiny |

### The tanod (4 directions)

*In-game size: 18 x 22 (a chibi: big head, small body, like a 2D Pokémon-style trainer). Make four separate images of the SAME character so Jaron can name them `down`, `up`, `left`, `right`. We already have a drawn placeholder set; this prompt is only if Jaron wants to improve it.*

```
[STYLE BIBLE]
SUBJECT: a Filipino tanod (barangay watchman), young adult, CHIBI proportions (big head, small body), black hair with a
small ponytail, a cloth headband, an open teal vest over a bare chest, rolled-up khaki shorts, barefoot, holding a small glowing
lantern in one hand. Determined, friendly face. Clean pixel art with a dark outline.
VIEW: seen from a 3/4 top-down angle, FACING THE VIEWER (this is the "down" image).
Then repeat with the SAME character facing AWAY from the viewer (up), facing LEFT, and facing RIGHT.
COLOR RULE: the object itself must contain no magenta, hot pink, or purple.
```

### Tree

```
[STYLE BIBLE]
SUBJECT: a single round tropical island tree (mango or narra), thick short trunk, full rounded dark-green canopy with lighter
highlights on top, a few small orange fruits. 3/4 top-down angle, no ground, no shadow.
COLOR RULE: the object itself must contain no magenta, hot pink, or purple.
```
Ask for it 20 wide by 28 tall (about 3:4). Save as `nodes/tree.png`.

### Supply crate

```
[STYLE BIBLE]
SUBJECT: a small wooden supply crate with metal corner plates and a rope handle, floating, slightly weathered.
3/4 top-down angle, no water, no shadow.
COLOR RULE: the object itself must contain no magenta, hot pink, or purple.
```

### Torch

```
[STYLE BIBLE]
SUBJECT: a single bamboo torch planted in sand, tall thin bamboo pole with a wrapped cloth head burning with a small
orange flame. 3/4 top-down angle, no ground, no shadow.
COLOR RULE: the object itself must contain no magenta, hot pink, or purple.
```

## IMPORTANT: Gemini often saves WebP, not PNG

If you rename a `.webp` file to `.png`, the game **cannot read it** (the Output window says `Could not read image`) and shows
shapes instead. Always save as a real PNG: in the browser choose **Save image as** and pick PNG, or open the image in Paint and
choose **Save as > PNG**. To check: the file's Type in File Explorer must say "PNG", not "WEBP".
Fish should also be **cropped close** around the fish, with little empty background, because they are drawn very small.

## Full asset list (generate after the test passes)

**Must-have (needed first):**
skiff, cutter, landing barge, lighthouse, the post (grounded ship with a radio), villager, fish spot, bamboo grove,
ore rock, the 5 fish, net barrier, mine buoy, coastal gun, HUD icons (bamboo, fish, 4 ores, radio).

**Should-have:**
signal frigate, dreadnought (boss), infiltrator fishing bangka, babaylan, Bakunawa, watchtower, decoy light.

**Nice-to-have:**
raider, saboteur diver, recon drone, tikbalang, engkanto, Lapulapu's stone figure (a **stone silhouette**, not a
realistic portrait).

Folklore pieces should be portrayed with respect.


---

## Prompts for the rest of the assets

The first style bible (top of this file) is for **things seen at a 3/4 angle**: ships, buildings, people. Some assets need a different camera, so each group
below has its own style block. **Always paste the whole block, then one SUBJECT.** One object per image.

### Fish (pure top-down, points right)

Fish are rotated by the game, so they must be seen from **straight above**. Pick the top-down result, not the side view. (Style: fish v1)

```
STYLE: high-detail pixel art game sprite, viewed from DIRECTLY ABOVE (pure top-down view, like looking down
into the sea), the fish is pointing to the RIGHT, perfectly horizontal, crisp clean pixel edges, limited
palette of about 16 colors, bold simple shapes that stay readable when shrunk very small, thin dark outline,
light coming from the top-left with darker shading on the bottom-right.
RENDERING: ONE single fish, centered, filling most of the image, not a sprite sheet, not multiple views, on a
flat solid pure magenta background (#FF00FF), one flat color with no gradient. No water, no shadow, no text,
no letters, no border, no frame, no watermark, no glow or soft blur, hard edges only.
COLOR RULE: the fish itself must contain no magenta, hot pink, or purple.
```

| File | SUBJECT |
|---|---|
| `fish/tamban.png` | `SUBJECT: a small Philippine tamban (sardine), slim silver body, pale belly, tiny dark eye, small forked tail.` |
| `fish/galunggong.png` | `SUBJECT: a Philippine galunggong (round scad), plump torpedo body, blue-silver with a darker blue-green back, small forked tail.` |
| `fish/bangus.png` | `SUBJECT: a Philippine bangus (milkfish), slim elegant body, bright silver, large forked tail, small dark eye.` |
| `fish/tuna.png` | `SUBJECT: a large tuna, powerful torpedo-shaped body, deep dark-blue back with a pale silver belly, crescent tail, small pointed fins.` |
| `fish/lapulapu.png` | `SUBJECT: a Philippine lapu-lapu (grouper), thick heavy body, orange-red with a few darker spots, wide rounded tail, big mouth. The rare, valuable fish.` |

### Harvest spots and defenses (3/4 view, 16 x 16 in the game)

Use the **first style bible** (top of this file) and one of these subjects. They are tiny in the game, so add this line to the style: `very simple bold shapes, readable at 16 pixels`.

| File | SUBJECT |
|---|---|
| `nodes/bamboo.png` | `SUBJECT: a small clump of three green bamboo stalks with segment rings and a few leaves, growing from a little dirt mound.` |
| `nodes/ore.png` | `SUBJECT: one grey rocky ore outcrop with small bright metallic glints. Neutral grey (the game recolors it into nickel, copper, chromite and gold).` |
| `buildings/net.png` | `SUBJECT: a fishing net stretched between two short bamboo posts, seen from above at an angle, rope mesh in pale tan.` |
| `buildings/mine.png` | `SUBJECT: a small spiked naval mine buoy, round dark steel body with short spikes and a small red warning light on top.` |

### Ships and buildings (3/4 view, first style bible)

| File | SUBJECT |
|---|---|
| `ships/cutter.png` | `SUBJECT: a fast, slim enemy patrol boat of the Kordan Dominion, charcoal-black hull with a crimson stripe, pointed bow facing right, one small gun. About 2.3 times longer than wide.` |
| `ships/barge.png` | `SUBJECT: a wide, flat enemy landing barge of the Kordan Dominion, charcoal-black hull, a lowered front ramp, stacked crates, a small crimson marking, bow facing right. About 2.2 times longer than wide.` |
| `buildings/post.png` | `SUBJECT: a rusting ship hull grounded on a reef and used as a command post, a small cabin on deck, a tall radio mast with an antenna crossbar, sandbags at the bow. Seen from a 3/4 top-down angle.` |

### HUD icons (flat, tiny: 10 x 10 in the game)

```
STYLE: a tiny flat pixel art icon, front view, bold simple shape, thick dark outline, maximum 6 colors, readable at 10 by 10 pixels.
RENDERING: ONE icon, centered, filling most of the image, on a flat solid pure magenta background (#FF00FF). No text, no letters, no border, no watermark, no glow.
COLOR RULE: the icon itself must contain no magenta, hot pink, or purple.
```

| File | SUBJECT |
|---|---|
| `ui/bamboo.png` | `SUBJECT: a single green bamboo stalk with two segment rings.` |
| `ui/fish.png` | `SUBJECT: a simple silver fish, side view.` |
| `ui/nickel.png` | `SUBJECT: a small pale grey-green ore nugget.` |
| `ui/copper.png` | `SUBJECT: a small orange copper ore nugget.` |
| `ui/chromite.png` | `SUBJECT: a small dark steel-blue ore nugget.` |
| `ui/gold.png` | `SUBJECT: a small shiny yellow gold nugget.` |

## How we keep this file up to date

Every time a **new asset is wired into the game**, this file gets (1) its row in "Exact file names", (2) its prompt here, and (3) a row in the prompt log
after it is generated. Gabriel updates the first two when he adds code. The person who generates the art fills in the log.


## Folklore allies (optional art)

Both work without art because the code draws them. If you want to replace them, use flat magenta (255,0,255) behind them, no shadow, and save real PNGs (not WebP).

| File | Size | What |
|---|---|---|
| `res/monsters/lapulapu_stone.png` | drawn at 28x48 (**Done, in the game**) | A grey stone statue of Lapu-Lapu, a Visayan chieftain, standing on a small base: bare chest, sash, a spear in one hand, proud stance, moss on the feet. Pixel art, 3/4 view, same style as the other sprites |
| `res/monsters/punso.png` | 22x22 | A small anthill of red-brown earth with a dark round doorway and a few ants. Pixel art, 3/4 view. The duwende is drawn by code |
| `res/monsters/manananggal.png` | drawn at 36x40 (**Done, in the game; one still image, no flap animation**) | A pale woman, upper body only, with long black hair streaming and two big bat wings spread, flying. Not gory: she ends in a faint wisp. Pixel art |


## Full prompts for the optional art (copy after the style bible)

**Lapu-Lapu stone** (`res/monsters/lapulapu_stone.png`, drawn at 28x48, upright, 3/4 view. It is not a ship, so it does not point right)
```
SUBJECT: a weathered grey stone statue of a Visayan chieftain standing on a small round stone base. Bare chest,
a simple sash and loincloth carved in stone, a long spear held upright in one hand, proud straight stance,
carved headband. Cracks and a little green moss on the feet and base. It is clearly a STATUE (one grey stone
colour with lighter highlights), not a living person and not a realistic portrait. Peaceful and dignified.
Height about twice its width.
```

**Raider** (`res/ships/raider.png`, 40x14, points right)
```
SUBJECT: a small fast Kordan Dominion raiding boat seen from above at a 3/4 angle, pointing to the RIGHT.
Narrow dark red hull with an orange stripe, a raised gun deck near the front with one small cannon, angular
bow, charcoal-black trim, tiny three-triangle emblem. Looks aggressive and armed, smaller than a cutter.
```

**Ghost ship** (`res/ships/ghost.png`, 38x12, points right)
```
SUBJECT: a pale, ghostly Kordan Dominion ship seen from above at a 3/4 angle, pointing to the RIGHT. Thin
whitish-grey hull that looks half transparent, tattered sail, two small glowing cyan eyes near the bow, a
faint mist at the stern. Eerie but still readable as a boat. Keep the outline crisp, no soft glow into the
magenta background.
```

Save as real PNG (not WebP) on flat magenta (255,0,255). Add each to the prompt log when generated. The game draws all three by code if the files are missing.
