package atingdagat.core;

/**
 * Constants.java
 *
 * One place for the numbers the whole game uses. If you need to change the
 * window size, the tile size, or how many nights the game lasts, change it
 * here and every class picks it up.
 */
public final class Constants {

    // Utility class: nobody should create an object of it.
    private Constants() {
    }

    public static final String TITLE = "Ang Huling Tanod";

    // The game is drawn on a small "internal" screen of 640 x 360 pixels,
    // then scaled up to fit the real window. This keeps the art sharp and
    // lets the window be resized without breaking the game.
    public static final int INTERNAL_WIDTH = 640;
    public static final int INTERNAL_HEIGHT = 360;

    // Size of one map tile (in internal pixels). The island map will be a grid of these.
    public static final int TILE_SIZE = 10;

    // Gameplay depth numbers ("nets only 3 deep") are measured in units of 16 pixels, so changing TILE_SIZE
    // never changes how far from the shore a net or a mine may go. TileMap.getDepth() converts for us.
    public static final int DEPTH_UNIT_PX = 16;

    // The map covers the whole internal screen: 64 columns x 36 rows of tiles.
    public static final int MAP_COLS = INTERNAL_WIDTH / TILE_SIZE;
    public static final int MAP_ROWS = (INTERNAL_HEIGHT + TILE_SIZE - 1) / TILE_SIZE;

    // Where the lighthouse stands on the island (tile column and row).
    public static final int LIGHTHOUSE_COL = 30;
    public static final int LIGHTHOUSE_ROW = 16;

    // The map file, stored inside the project so it also works from the JAR.
    public static final String MAP_FILE = "/atingdagat/data/island.txt";

    // How many times per second the game updates and redraws.
    public static final int TARGET_FPS = 60;

    // Data files the game reads.
    public static final String NODES_FILE = "/atingdagat/data/nodes.txt";
    public static final String WAVES_FILE = "/atingdagat/data/waves.txt";
    public static final String VOICES_FILE = "/atingdagat/data/voices.txt";

    // How long each part of the day lasts, in frames (60 frames = 1 second).
    public static final int DAY_FRAMES = 75 * TARGET_FPS;
    public static final int DUSK_FRAMES = 8 * TARGET_FPS;
    public static final int DAWN_FRAMES = 7 * TARGET_FPS;

    // The post (the island's command post) stands here.
    public static final int POST_COL = 40;
    public static final int POST_ROW = 18;

    // Starting stock, and the price of things.
    public static final int START_BAMBOO = 6;
    public static final int BAMBOO_CAP = 30;              // most bamboo the tanod can carry, so it cannot be hoarded
    public static final int MINE_COST_NICKEL = 2;
    public static final int REWIRE_COST_COPPER = 2;       // key C: radio +25
    public static final int REWIRE_RADIO_AMOUNT = 25;
    public static final int ARMOR_COST_CHROMITE = 2;      // key V: post +4 max health
    public static final int ARMOR_HEALTH = 4;
    public static final int ARMOR_MAX_LEVEL = 3;
    public static final int LAMP_COST_GOLD = 1;           // key G: wider, longer beam
    public static final int LAMP_MAX_LEVEL = 2;
    // Ore powers (the night side of ores): copper wiring widens torch light, chromite plates defenses, gold lengthens flares.
    public static final int WIRING_MAX_LEVEL = 2;
    public static final double WIRING_GLOW_BONUS = 0.25;   // per level: torch light radius
    public static final int PLATING_MAX_LEVEL = 2;          // per Armor level, up to this: +1 durability on every net, torch and Pana
    public static final double LAMP_FLARE_BONUS = 0.4;      // per lamp level: longer-burning flare
    public static final int MAX_FISH_SCHOOLS = 9;
    public static final int FISH_SPAWN_FRAMES = 3 * TARGET_FPS;
    public static final int NET_MAX_DEPTH = 3;      // nets only on shallow water (tiles from shore)
    public static final int MINE_MAX_DEPTH = 5;     // mines can go a little further out than nets
    public static final int REPAIR_FISH_COST = 2;
    public static final int REPAIR_POST_AMOUNT = 4;
    public static final int REPAIR_RADIO_AMOUNT = 15;
    public static final int RADIO_PER_POST_DAMAGE = 3;  // a landing weakens the radio by 3 x the post damage
    public static final int GUN_COOLDOWN = 30;      // frames between shots

    // The game lasts 7 nights.
    public static final int TOTAL_NIGHTS = 7;

    // The size the window opens at (the player can resize it).
    public static final int WINDOW_START_WIDTH = 1280;
    public static final int WINDOW_START_HEIGHT = 720;

    // true = smooth scaling, false = crisp pixels. Try both and keep what looks better.
    public static final boolean SMOOTH_SCALING = false;

    // Supply crates (they float in from the sea and hold ore)
    public static final int CRATE_INTERVAL = 24 * TARGET_FPS;          // a new crate about every 24 seconds
    public static final int CRATE_INTERVAL_STRONG = 14 * TARGET_FPS;   // faster when the radio is strong
    public static final int CRATE_RADIO_STRONG = 70;
    public static final int MAX_CRATES = 2;

    // The walking tanod
    public static final int PLAYER_START_COL = 34;
    public static final int PLAYER_START_ROW = 20;
    public static final double PLAYER_SPEED = 1.5;          // pixels per frame
    public static final double LAND_REACH = 40;             // how far the tanod can harvest on land
    public static final double FISH_REACH = 120;            // how far a line can be cast for fish
    public static final double BUILD_REACH = 80;            // how far nets, mines and torches can be placed
    public static final double REPAIR_REACH = 56;           // how close you must be to mend a net, torch or tower (hold X)
    public static final int REPAIR_FRAMES = 24;             // frames of holding X for each point mended
    public static final int FLARE_COOLDOWN = 20 * 60;       // 20 seconds between flares
    public static final int FLARE_LIFE = 6 * 60;            // a flare burns for 6 seconds
    public static final double FLARE_RADIUS = 70;           // how far a flare lights
    public static final double FLARE_RANGE = 260;           // how far from you it can be fired
    public static final double FIRE_RANGE = 200;            // how far the gun shoots
    public static final double STEER_RANGE = 30;            // stand this close to the lighthouse to steer the beam

    // Light at night
    public static final double LANTERN_RADIUS = 38;
    public static final double TORCH_RADIUS = 50;
    public static final int TORCH_COST_BAMBOO = 2;
    public static final int TORCH_MAX = 20;

    // Pana tower (bamboo arrow tower)
    public static final int PANA_COST_BAMBOO = 4;
    public static final int PANA_MAX = 10;
    public static final double PANA_RANGE = 80;
    public static final int PANA_COOLDOWN = 100;     // frames between arrows

    // Survivors (they arrive by bangka at dawn)
    public static final double BANGKA_SPEED = 1.3;
    public static final double SURVIVOR_WALK_SPEED = 0.8;
    public static final int INDAY_HEAL_FRAMES = 6 * TARGET_FPS;    // Ka Inday mends the post this often
    public static final int MIGO_SIGNAL_FRAMES = 4 * TARGET_FPS;   // Migo boosts the radio this often
    public static final double TONYO_RANGE = 95;
    // Day work: each survivor is paid in meals, then chops trees and bamboo for the post
    public static final int WORKER_WAGE_MEALS = 1;                 // per survivor, per day
    public static final int WORKER_QUOTA = 3;                      // loads carried per day
    public static final int WORKER_CHOP_FRAMES = 4 * TARGET_FPS;   // slower than the tanod (about 1.8 s)
    public static final double WORKER_SEARCH = 170;                // how far from the post they will go
    public static final int TONYO_COOLDOWN = 80;

    // Folklore allies (story events at dawn)
    public static final int STATUE_NIGHT = 3;                // after this night the story tells of the Lapu-Lapu stone
    public static final int STATUE_COL = 52;                 // the stone statue on the northeast islet
    public static final int STATUE_ROW = 8;
    public static final int MANANANGGAL_NIGHT = 5;           // after this night she comes
    public static final double MANANANGGAL_RANGE = 190;
    public static final int MANANANGGAL_COOLDOWN = 240;
    public static final int MANANANGGAL_DAMAGE = 1;
    public static final int MANANANGGAL_SWOOPS = 4;           // she tires after this many swoops a night
    public static final double MANANANGGAL_SPEED = 1.7;
    public static final int DUWENDE_NIGHT = 1;               // after this night the anthill stirs
    public static final int PUNSO_COL = 15;                  // the anthill on the west shore
    public static final int PUNSO_ROW = 28;
    public static final int DUWENDE_GIFT_BAMBOO = 3;         // what he asks for
}
