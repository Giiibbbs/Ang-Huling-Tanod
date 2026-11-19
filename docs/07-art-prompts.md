# Art Prompts (for Gemini)

*Owner: Jaron. Written by the art director role. Goal: every sprite looks like it belongs in the same game.*

The game draws on a 640x360 screen with 16x16 tiles, in a 3/4 top-down view. Sprites must match that.

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

*In-game size: about 32x48 pixels.*

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

Look at them **side by side**, then **shrunk to their in-game size** (32x16, 32x48, 16x24).

- [ ] Same outline thickness and same level of detail
- [ ] Same palette feel (not one very saturated and one dull)
- [ ] Same light direction (top-left)
- [ ] Still readable when shrunk to in-game size
- [ ] Background is one flat magenta, with no gradient and no pink halo on the edges
- [ ] Nothing like a real flag or a copyrighted character
- [ ] The scale makes sense next to a 16x16 tile (the skiff is about 2 tiles long)

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

## From Gemini to the game

1. Download the PNG. Keep the original, and don't edit the original.
2. Save it with a clear lowercase name in the project's art folder:

```
AngHulingTanod/src/atingdagat/res/
├── ships/       skiff.png, cutter.png, barge.png ...
├── buildings/   lighthouse.png, post.png ...
├── people/      villager.png, babaylan.png ...
└── ui/          icons
```

3. The game turns the magenta into transparency when it loads the image (chroma key), then scales it to its in-game size.
   You don't need to edit the background.
4. **The skiff is already wired up.** Save it as exactly `AngHulingTanod/src/atingdagat/res/ships/skiff.png`, with the
   ship pointing **right**, and it appears in the game with no code change. If the file is missing, a placeholder is
   drawn. (Other sprites get wired up as we build them, and Gabriel will tell you the exact file name.)
5. Commit it. Art lives in `res/`, so it is packed into the JAR automatically.
5. Write a row in the **prompt log** below.

## Prompt log (keep this honest, we show it in CREDITS)

| Asset file | Tool | Date | Style bible version | Edited by hand? | Notes |
|---|---|---|---|---|---|
| ships/skiff.png | Gemini | | v1 | | |
| buildings/lighthouse.png | Gemini | | v1 | | |
| people/villager.png | Gemini | | v1 | | |

## Exact file names the game loads right now

Every file below is already wired up. Drop the PNG in `AngHulingTanod/src/atingdagat/res/` with this exact name and it appears
with no code change. Missing ones show simple shapes. Generate **larger** than the in-game size (4x is fine). The game shrinks it.

| File | In-game size | Notes |
|---|---|---|
| `ships/skiff.png` | 32 x 16 | Points **right** |
| `ships/cutter.png` | 28 x 12 | Points right. Slimmer and faster-looking than the skiff |
| `ships/barge.png` | 44 x 20 | Points right. Wide, flat landing barge with a ramp |
| `buildings/lighthouse.png` | 32 x 48 | Base of the tower at the bottom of the image |
| `buildings/post.png` | 40 x 32 | Grounded ship with a cabin and a radio mast |
| `buildings/net.png` | 16 x 16 | One tile of net between two bamboo posts |
| `buildings/mine.png` | 16 x 16 | Floating spiked mine buoy |
| `nodes/bamboo.png` | 16 x 16 | Bamboo grove |
| `nodes/ore.png` | 16 x 16 | **One grey rock with metal glints. The game recolors it into nickel, copper, chromite and gold**, so you need only this one |
| `nodes/nickel.png`, `copper.png`, `chromite.png`, `gold.png` | 16 x 16 | **Optional.** Only if you want each ore to have its own picture. Otherwise the recolored rock is used |
| `fish/tamban.png` | 10 x 5 | Small silver sardine, points **right** |
| `fish/galunggong.png` | 12 x 6 | Round scad, blue-silver, points right |
| `fish/bangus.png` | 14 x 6 | Slim bright-silver milkfish, points right |
| `fish/tuna.png` | 20 x 8 | Big dark-blue torpedo-shaped tuna, points right |
| `fish/lapulapu.png` | 20 x 10 | Orange-red grouper (the rare, valuable one), points right |
| `ui/bamboo.png`, `ui/fish.png`, `ui/nickel.png`, `ui/copper.png`, `ui/chromite.png`, `ui/gold.png` | 10 x 10 | HUD icons. Must read clearly when tiny |

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
