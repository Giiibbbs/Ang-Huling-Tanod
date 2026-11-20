package atingdagat.core;

import atingdagat.defense.Defense;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.defense.PanaTower;
import atingdagat.defense.Torch;
import atingdagat.entities.Bangka;
import atingdagat.entities.KaInday;
import atingdagat.entities.Migo;
import atingdagat.entities.Survivor;
import atingdagat.entities.Tonyo;
import atingdagat.economy.FishKind;
import atingdagat.economy.FishSchool;
import atingdagat.economy.Harvestable;
import atingdagat.economy.Inventory;
import atingdagat.economy.ResourceNode;
import atingdagat.economy.ResourceType;
import atingdagat.economy.SupplyCrate;
import atingdagat.entities.Bakunawa;
import atingdagat.entities.Barge;
import atingdagat.entities.Beam;
import atingdagat.entities.Cutter;
import atingdagat.entities.GlowSource;
import atingdagat.entities.Lantern;
import atingdagat.entities.Light;
import atingdagat.entities.Lighthouse;
import atingdagat.entities.Player;
import atingdagat.entities.Post;
import atingdagat.entities.Radio;
import atingdagat.entities.Skiff;
import atingdagat.entities.Warship;
import atingdagat.map.TileMap;
import atingdagat.ui.Explosion;
import atingdagat.ui.NightOverlay;
import atingdagat.ui.Popup;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * GameWorld.java
 *
 * Everything that exists in ONE run of the game: the clock, the ships, the
 * nets, the resources, the post and the radio. GamePanel used to hold all of
 * this. Moving it here means GamePanel only does windows, scaling, and keys,
 * and a brand new run is just "new GameWorld(map)".
 *
 * GamePanel forwards the mouse and keys here. The world updates itself once
 * per frame and can draw itself.
 */
public class GameWorld {

    public enum Outcome { PLAYING, WON, LOST }

    /** What a click does in the day. */
    public enum Tool { HARVEST, NET, MINE, TORCH, PANA }

    private static final int BANNER_FRAMES = 150;
    private static final int GUN_DAMAGE = 1;

    // The ground and the fixed things on it
    private final TileMap map;
    private final Lighthouse lighthouse = new Lighthouse(Constants.LIGHTHOUSE_COL, Constants.LIGHTHOUSE_ROW);
    private final Post post = new Post(Constants.POST_COL, Constants.POST_ROW);
    private final Radio radio = new Radio();
    private final Beam beam = new Beam(lighthouse.getLampX(), lighthouse.getLampY());
    private final NightOverlay overlay = new NightOverlay();
    private final Bakunawa dragon = new Bakunawa();   // the big event of the last night
    private double forcedDarkness = -1;               // the ending scene sets this (-1 = follow the day cycle)

    // The walking tanod, and the lights that make ships visible at night
    private final Player player = new Player(
            Constants.PLAYER_START_COL * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0,
            Constants.PLAYER_START_ROW * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0 + 4);
    private final Lantern lantern = new Lantern(player);
    private final Light anyLight = this::isLit;   // "is this spot lit by ANY light?" (see isLit)

    // Things that come and go
    private final ArrayList<Warship> ships = new ArrayList<>();
    private final ArrayList<Defense> defenses = new ArrayList<>();
    private final ArrayList<ResourceNode> nodes;
    private final ArrayList<FishSchool> fish = new ArrayList<>();
    private final ArrayList<SupplyCrate> crates = new ArrayList<>();
    private final ArrayList<Survivor> survivors = new ArrayList<>();   // people who joined the post (a mix of subclasses)
    private final ArrayList<Bangka> boats = new ArrayList<>();         // boats bringing them in
    private static final int[] ARRIVAL_NIGHTS = {2, 4, 6};              // the dawn after these nights, a bangka lands
    private String storyTitle = "";
    private String storyText = "";
    private int storyTimer = 0;
    private final ArrayList<Popup> popups = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private final ArrayList<WavePlan.Kind> queue = new ArrayList<>();   // tonight's ships, in the order they come

    // Rules and the clock
    private final DayCycle cycle = new DayCycle();
    private final WavePlan plan = new WavePlan(Constants.WAVES_FILE);
    private final Inventory inventory = new Inventory();
    private final Random random = new Random();

    private Outcome outcome = Outcome.PLAYING;
    private String endReason = "";

    private int frame = 0;
    private int mouseX = Constants.INTERNAL_WIDTH / 2;
    private int mouseY = 40;
    private int moveX = 0;               // the walking keys, set by GamePanel every frame
    private int moveY = 0;
    private boolean steering = false;   // SPACE held

    private Tool tool = Tool.HARVEST;
    private int spawned = 0;             // ships sent so far tonight
    private int spawnTimer = 0;
    private int gunCooldown = 0;
    private int shotTimer = 0;           // draws the shot line for a few frames
    private double shotX, shotY;

    // Holding the mouse on a spot or a fish fills a progress ring. Let go, and it drains.
    private boolean holding = false;
    private Harvestable holdTarget = null;
    private int holdProgress = 0;
    private int fishTimer = 0;
    private int crateTimer = Constants.CRATE_INTERVAL - 6 * Constants.TARGET_FPS;   // the first crate comes after about 6 seconds

    private String bannerText = "";
    private int bannerTimer = 0;

    public GameWorld(TileMap map) {
        this.map = map;
        this.nodes = ResourceNode.load(Constants.NODES_FILE, map);
        inventory.add(ResourceType.BAMBOO, Constants.START_BAMBOO);
        for (int i = 0; i < 6; i++) {
            spawnFish();
        }
        showBanner("DAY 1");
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public void update(int mouseX, int mouseY) {
        if (outcome != Outcome.PLAYING) {
            return;
        }
        frame++;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        player.update(map, moveX, moveY);

        // Near the lighthouse, holding SPACE steers the beam by hand. Otherwise it sweeps by itself.
        boolean manual = steering && isNearLighthouse();
        if (manual) {
            beam.aimAt(mouseX, mouseY);
        }
        beam.update(manual);

        updateEclipse();

        Phase changed = cycle.update();
        if (changed != null) {
            onPhaseChange(changed);
        }

        if (cycle.isNight()) {
            spawnShips();
        }
        updateShips();
        updateDefenses();

        for (ResourceNode node : nodes) {
            node.update();
        }
        updateFish();
        updateCrates();
        updateSurvivors();
        updateHolding();
        for (int i = explosions.size() - 1; i >= 0; i--) {
            explosions.get(i).update();
            if (explosions.get(i).isDone()) {
                explosions.remove(i);
            }
        }
        for (int i = popups.size() - 1; i >= 0; i--) {
            popups.get(i).update();
            if (popups.get(i).isDone()) {
                popups.remove(i);
            }
        }
        if (gunCooldown > 0) {
            gunCooldown--;
        }
        if (shotTimer > 0) {
            shotTimer--;
        }
        if (bannerTimer > 0) {
            bannerTimer--;
        }
        if (storyTimer > 0) {
            storyTimer--;
        }

        checkNightOver();
        checkLoss();
    }

    private void spawnShips() {
        int night = cycle.getNight();
        spawnTimer++;
        if (spawned < queue.size() && spawnTimer >= plan.getInterval(night)) {
            spawnTimer = 0;
            WavePlan.Kind kind = queue.get(spawned);
            spawned++;
            spawnShip(kind);
        }
    }

    /** Builds tonight's list of ships from the wave plan, then shuffles it. */
    private void buildQueue() {
        queue.clear();
        for (WavePlan.Kind kind : WavePlan.Kind.values()) {
            for (int i = 0; i < plan.getCount(cycle.getNight(), kind); i++) {
                queue.add(kind);
            }
        }
        Collections.shuffle(queue, random);
    }

    /** A ship appears just outside the screen, in a lane that points at the post. */
    private void spawnShip(WavePlan.Kind kind) {
        double band = 110;   // how far from the post's line the skiff may start
        double x;
        double y;
        switch (random.nextInt(4)) {
            case 0:  // left
                x = -12;
                y = laneY(band);
                break;
            case 1:  // right
                x = Constants.INTERNAL_WIDTH + 12;
                y = laneY(band);
                break;
            case 2:  // top
                x = laneX(band);
                y = -12;
                break;
            default: // bottom
                x = laneX(band);
                y = Constants.INTERNAL_HEIGHT + 12;
                break;
        }
        switch (kind) {
            case CUTTER:
                ships.add(new Cutter(x, y, post.getX(), post.getY()));
                break;
            case BARGE:
                ships.add(new Barge(x, y, post.getX(), post.getY()));
                break;
            default:
                ships.add(new Skiff(x, y, post.getX(), post.getY()));
                break;
        }
    }

    private double laneX(double band) {
        double v = post.getX() + (random.nextDouble() * 2 - 1) * band;
        return Math.max(20, Math.min(Constants.INTERNAL_WIDTH - 20, v));
    }

    private double laneY(double band) {
        double v = post.getY() + (random.nextDouble() * 2 - 1) * band;
        return Math.max(20, Math.min(Constants.INTERNAL_HEIGHT - 20, v));
    }

    /** Moves ships. Goes backwards so removing one does not skip the next. */
    private void updateShips() {
        for (int i = ships.size() - 1; i >= 0; i--) {
            Warship ship = ships.get(i);
            ship.update(map, anyLight, defenses);
            if (ship.isDead()) {
                ships.remove(i);
            } else if (ship.hasArrived()) {
                ships.remove(i);
                int hurt = ship.getLandingDamage();
                post.damage(hurt);
                radio.weaken(hurt * Constants.RADIO_PER_POST_DAMAGE);
                popups.add(new Popup(post.getX(), post.getY() - 30, "-" + hurt + " Post", new Color(255, 110, 100)));
            }
        }
    }

    private void updateDefenses() {
        for (int i = defenses.size() - 1; i >= 0; i--) {
            Defense d = defenses.get(i);
            d.update(ships);
            if (d.isDestroyed()) {
                if (d instanceof Mine) {   // a mine is "destroyed" because it went off
                    Mine mine = (Mine) d;
                    explosions.add(new Explosion(mine.getX(), mine.getY()));
                }
                defenses.remove(i);
            }
        }
    }

    private void checkNightOver() {
        boolean eventDone = cycle.getNight() < Constants.TOTAL_NIGHTS || dragon.isDone();
        if (cycle.isNight() && spawned >= queue.size() && ships.isEmpty() && eventDone) {
            onPhaseChange(cycle.advance());
        }
    }

    private void checkLoss() {
        if (outcome != Outcome.PLAYING) {
            return;
        }
        if (post.isDestroyed()) {
            outcome = Outcome.LOST;
            endReason = "The post has fallen.";
        } else if (radio.isSilent()) {
            outcome = Outcome.LOST;
            endReason = "The radio went silent.";
        }
    }

    private void onPhaseChange(Phase phase) {
        switch (phase) {
            case DAY:
                showBanner("DAY " + cycle.getNight());
                break;
            case DUSK:
                showBanner("DUSK - get ready");
                break;
            case NIGHT:
                fish.clear();            // the fish go deep in the dark
                crates.clear();          // and any crate left floating drifts away
                holding = false;
                holdTarget = null;
                holdProgress = 0;
                buildQueue();
                spawned = 0;
                spawnTimer = plan.getInterval(cycle.getNight()) - 90;   // first skiff in about 1.5 seconds
                showBanner("NIGHT " + cycle.getNight());
                break;
            default: // DAWN
                dragon.finish();     // (matters only if the night was skipped in the middle of the event)
                beam.setOn(true);
                showBanner("DAWN - repair and rebuild");
                sendBangka(cycle.getNight());
                break;
        }
        if (cycle.isFinished() && outcome == Outcome.PLAYING) {
            outcome = Outcome.WON;
            endReason = "You kept the radio alive through all " + Constants.TOTAL_NIGHTS + " nights.";
        }
    }

    private void showBanner(String text) {
        bannerText = text;
        bannerTimer = BANNER_FRAMES;
    }

    // ------------------------------------------------------------------
    // THE BAKUNAWA (night 7)
    // ------------------------------------------------------------------

    /** Starts the event when the last ship of the last night has set sail, runs it, and handles the bite. */
    private void updateEclipse() {
        if (cycle.isNight() && cycle.getNight() == Constants.TOTAL_NIGHTS && spawned >= queue.size() && !queue.isEmpty()) {
            if (dragon.getStage() == Bakunawa.Stage.IDLE) {
                showBanner("THE MOON GOES DARK");
            }
            dragon.start();
        }
        dragon.update(frame);
        beam.setOn(!dragon.isEclipse());
        if (dragon.consumeBite()) {   // everything still afloat goes down with the flagship
            for (Warship ship : ships) {
                explosions.add(new Explosion(ship.getX(), ship.getY()));
            }
            ships.clear();
        }
    }

    /** SPACE: sound the gongs (only matters during the Bakunawa event). */
    public void gong() {
        dragon.gong();
    }

    /** Test key E: start the Bakunawa event right now (night only). */
    public void testEclipse() {
        if (cycle.isNight() && outcome == Outcome.PLAYING) {
            dragon.start();
        }
    }

    /** Test key L: jump to the start of the last night's day. */
    public void jumpToLastNight() {
        if (outcome == Outcome.PLAYING) {
            ships.clear();
            cycle.setNight(Constants.TOTAL_NIGHTS);
            showBanner("DAY " + Constants.TOTAL_NIGHTS);
        }
    }

    /** The ending scene uses this to bring the sun up (0 = day, 1 = night). A negative value gives control back. */
    public void setForcedDarkness(double value) {
        forcedDarkness = value;
    }

    public Bakunawa getDragon() {
        return dragon;
    }

    /** Test/debug key (N): jump to the next phase. Ends the night early and removes the ships. */
    public void skipPhase() {
        if (outcome != Outcome.PLAYING) {
            return;
        }
        if (cycle.isNight()) {
            ships.clear();
        }
        onPhaseChange(cycle.advance());
    }

    // ------------------------------------------------------------------
    // MOVING AND LIGHT
    // ------------------------------------------------------------------

    /** Called by GamePanel every frame: which way the walking keys point (-1, 0 or 1) and whether SPACE is held. */
    public void setInput(int moveX, int moveY, boolean steering) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.steering = steering;
    }

    /** True if the tanod stands close enough to the lamp to turn it. */
    public boolean isNearLighthouse() {
        return Math.hypot(player.getX() - lighthouse.getBaseX(), player.getY() - lighthouse.getBaseY())
                <= Constants.STEER_RANGE;
    }

    /** Is this point lit by the beam, the lantern or any torch? Ships ask this every frame. */
    private boolean isLit(double x, double y) {
        if (beam.lights(x, y) || lantern.lights(x, y)) {
            return true;
        }
        for (Defense d : defenses) {
            if (d instanceof Torch && ((Torch) d).lights(x, y)) {
                return true;
            }
        }
        return false;
    }

    /** The lantern and all torches, for the night drawing. */
    private ArrayList<GlowSource> glowSources() {
        ArrayList<GlowSource> list = new ArrayList<>();
        list.add(lantern);
        for (Survivor sv : survivors) {
            if (sv instanceof GlowSource) {
                list.add((GlowSource) sv);
            }
        }
        for (Defense d : defenses) {
            if (d instanceof Torch) {
                list.add((Torch) d);
            }
        }
        return list;
    }

    /** Is the point (x, y) within "radius" pixels of the tanod's feet? */
    private boolean inReach(double x, double y, double radius) {
        return Math.hypot(x - player.getX(), y - player.getY()) <= radius;
    }

    // ------------------------------------------------------------------
    // PLAYER ACTIONS
    // ------------------------------------------------------------------

    /** x and y are in game coordinates. button: 1 = left, 3 = right. */
    public void click(int x, int y, int button) {
        if (outcome != Outcome.PLAYING) {
            return;
        }
        if (button == 3) {
            tool = Tool.HARVEST;   // right click puts the tool away
            return;
        }
        if (cycle.isNight()) {
            fire(x, y);
        } else if (tool == Tool.NET) {
            placeNet(x, y);
        } else if (tool == Tool.MINE) {
            placeMine(x, y);
        } else if (tool == Tool.TORCH) {
            placeTorch(x, y);
        } else if (tool == Tool.PANA) {
            placePana(x, y);
        } else {
            holding = true;   // harvesting is HOLDING the button (see updateHolding)
        }
    }

    /** Keys 1 and 2: pick a tool. Pressing the same key again puts it away. */
    public void toggleTool(Tool wanted) {
        if (cycle.isNight()) {
            return;
        }
        tool = (tool == wanted) ? Tool.HARVEST : wanted;
    }

    /** The mouse button was let go. */
    public void release() {
        holding = false;
    }

    /** Finds the first thing under the mouse that can be harvested: fish first, then spots on land. */
    private Harvestable findHarvestable(double x, double y) {
        for (FishSchool school : fish) {
            if (school.isAvailable() && school.isNear(x, y)) {
                return school;
            }
        }
        for (SupplyCrate crate : crates) {
            if (crate.isAvailable() && crate.isNear(x, y)) {
                return crate;
            }
        }
        for (ResourceNode node : nodes) {
            if (node.isNear(x, y)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Called every frame. While the button is held on something, the ring fills. When it is full the player
     * gets one portion. If the mouse slips off (a fish swims away), the ring drains, so you must keep up.
     */
    private void updateHolding() {
        boolean canHarvest = holding && tool == Tool.HARVEST && !cycle.isNight();
        Harvestable under = canHarvest ? findHarvestable(mouseX, mouseY) : null;

        // The tanod must be close enough. Fish can be reached further (a cast line) than things on land.
        if (under != null) {
            double reach = (under instanceof FishSchool || under instanceof SupplyCrate) ? Constants.FISH_REACH : Constants.LAND_REACH;
            if (!inReach(under.getX(), under.getY(), reach)) {
                if (holdTarget != under) {   // one reminder per spot, not one every frame
                    popups.add(new Popup(under.getX(), under.getY() - 12, "Too far - walk closer", Color.LIGHT_GRAY));
                }
                holdTarget = under;
                holdProgress = 0;
                return;
            }
        }

        if (under != null && !under.isAvailable()) {
            if (holdTarget != under) {   // one reminder per spot, not one every frame
                popups.add(new Popup(under.getX(), under.getY() - 12, "Nothing left", Color.LIGHT_GRAY));
            }
            holdTarget = under;
            holdProgress = 0;
            return;
        }
        if (under != null) {
            if (under != holdTarget) {
                holdTarget = under;
                holdProgress = 0;
            }
            holdProgress++;
            if (holdProgress >= under.getHoldFrames()) {
                collect(under);
                holdProgress = 0;
            }
        } else {
            holdProgress = Math.max(0, holdProgress - 3);
            if (holdProgress == 0) {
                holdTarget = null;
            }
        }
    }

    private void collect(Harvestable target) {
        int gained = target.harvest();
        if (gained <= 0) {
            return;
        }
        ResourceType type = target.getType().getStock();   // a tree gives bamboo
        inventory.add(type, gained);
        String text = "+" + gained + " " + type.getLabel();
        if (type.getMeals() > 1) {
            text += " (" + type.getMeals() + " meals)";
        }
        popups.add(new Popup(target.getX(), target.getY() - 14, text, type.getColor()));
    }

    // ------------------------------------------------------------------
    // FISH: schools swim around the sea in the day
    // ------------------------------------------------------------------

    private void updateFish() {
        if (cycle.isNight()) {
            return;
        }
        fishTimer++;
        if (fishTimer >= Constants.FISH_SPAWN_FRAMES && fish.size() < Constants.MAX_FISH_SCHOOLS) {
            fishTimer = 0;
            spawnFish();
        }
        for (int i = fish.size() - 1; i >= 0; i--) {
            fish.get(i).update(map);
            if (fish.get(i).isGone()) {
                if (fish.get(i) == holdTarget) {
                    holdTarget = null;
                    holdProgress = 0;
                }
                fish.remove(i);
            }
        }
    }

    // ------------------------------------------------------------------
    // SUPPLY CRATES: ore that floats in, so the island never runs completely dry
    // ------------------------------------------------------------------

    private void updateCrates() {
        if (cycle.isNight()) {
            return;
        }
        crateTimer++;
        int interval = radio.getSignal() >= Constants.CRATE_RADIO_STRONG || crateBoosted()
                ? Constants.CRATE_INTERVAL_STRONG : Constants.CRATE_INTERVAL;
        if (crateTimer >= interval && crates.size() < Constants.MAX_CRATES) {
            crateTimer = 0;
            spawnCrate();
        }
        for (int i = crates.size() - 1; i >= 0; i--) {
            crates.get(i).update(map);
            if (crates.get(i).isGone()) {
                if (crates.get(i) == holdTarget) {
                    holdTarget = null;
                    holdProgress = 0;
                }
                crates.remove(i);
            }
        }
    }

    /** A crate appears at sea. What is inside favors the ores that have run out on the island. */
    private void spawnCrate() {
        ResourceType contents = pickCrateContents();
        int units = (contents == ResourceType.GOLD) ? 1 : 2;
        for (int attempt = 0; attempt < 80; attempt++) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            if (!map.isWater(row, col) || map.getDepth(row, col) < 3 || map.getDepth(row, col) > 6) {
                continue;
            }
            double x = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            double y = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            if (y < 40 || y > Constants.INTERNAL_HEIGHT - 24) {
                continue;
            }
            crates.add(new SupplyCrate(contents, units, x, y));
            popups.add(new Popup(x, y - 14, "Supply crate!", new Color(255, 225, 120)));
            return;
        }
    }

    /** Nickel and copper are common, gold is rare. An ore that is gone from the map is 3 times as likely, so you are never stuck. */
    private ResourceType pickCrateContents() {
        ResourceType[] ores = {ResourceType.NICKEL, ResourceType.COPPER, ResourceType.CHROMITE, ResourceType.GOLD};
        double[] weight = {4, 4, 2, 1};
        double total = 0;
        for (int i = 0; i < ores.length; i++) {
            if (oreLeftOnMap(ores[i]) == 0) {
                weight[i] *= 3;
            }
            total += weight[i];
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < ores.length; i++) {
            roll -= weight[i];
            if (roll <= 0) {
                return ores[i];
            }
        }
        return ResourceType.NICKEL;
    }

    /** How many units of this ore are still in the ground on the island. */
    private int oreLeftOnMap(ResourceType type) {
        int left = 0;
        for (ResourceNode node : nodes) {
            if (node.getType() == type) {
                left += node.getRemaining();
            }
        }
        return left;
    }

    /** Test key: drop a crate near the shore now. */
    public void testCrate() {
        if (!cycle.isNight()) {
            spawnCrate();
        }
    }

    public int getCrateCount() {
        return crates.size();
    }

    /** Picks a kind (rare ones less often), then looks for a spot at the right depth. */
    private void spawnFish() {
        FishKind kind = pickFishKind();
        for (int attempt = 0; attempt < 60; attempt++) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            if (!map.isWater(row, col)) {
                continue;
            }
            int depth = map.getDepth(row, col);
            if (depth < kind.getMinDepth() || depth > kind.getMaxDepth()) {
                continue;
            }
            double x = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            double y = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            fish.add(new FishSchool(kind, x, y, random));
            return;
        }
    }

    /** A weighted random pick: tamban is common, lapu-lapu is rare. */
    private FishKind pickFishKind() {
        double total = 0;
        for (FishKind kind : FishKind.values()) {
            total += kind.getWeight();
        }
        double roll = random.nextDouble() * total;
        for (FishKind kind : FishKind.values()) {
            roll -= kind.getWeight();
            if (roll <= 0) {
                return kind;
            }
        }
        return FishKind.TAMBAN;
    }

    /** Can a net go on this tile? (shallow water only) */
    public boolean canPlaceNet(int col, int row) {
        return canPlaceOnWater(col, row, Constants.NET_MAX_DEPTH);
    }

    /** Can a mine go on this tile? (any water that is not too deep) */
    public boolean canPlaceMine(int col, int row) {
        return canPlaceOnWater(col, row, Constants.MINE_MAX_DEPTH);
    }

    private boolean canPlaceOnWater(int col, int row, int maxDepth) {
        if (col < 0 || row < 0 || col >= Constants.MAP_COLS || row >= Constants.MAP_ROWS) {
            return false;
        }
        if (!map.isWater(row, col) || map.getDepth(row, col) > maxDepth) {
            return false;
        }
        for (Defense d : defenses) {
            if (d.occupies(col, row)) {
                return false;
            }
        }
        return true;
    }

    private void placeNet(int x, int y) {
        int col = x / Constants.TILE_SIZE;
        int row = y / Constants.TILE_SIZE;
        if (x < 0 || y < 0 || !canPlaceNet(col, row)) {
            popups.add(new Popup(x, y - 6, "Shallow water only", Color.LIGHT_GRAY));
            return;
        }
        if (!tileInBuildReach(col, row)) {
            popups.add(new Popup(x, y - 6, "Too far - walk closer", Color.LIGHT_GRAY));
            return;
        }
        if (!inventory.spend(ResourceType.BAMBOO, Net.COST_BAMBOO)) {
            popups.add(new Popup(x, y - 6, "Need " + Net.COST_BAMBOO + " bamboo", new Color(255, 150, 120)));
            return;
        }
        defenses.add(new Net(col, row));
    }

    private void placeMine(int x, int y) {
        int col = x / Constants.TILE_SIZE;
        int row = y / Constants.TILE_SIZE;
        if (x < 0 || y < 0 || !canPlaceMine(col, row)) {
            popups.add(new Popup(x, y - 6, "Water near the island only", Color.LIGHT_GRAY));
            return;
        }
        if (!tileInBuildReach(col, row)) {
            popups.add(new Popup(x, y - 6, "Too far - walk closer", Color.LIGHT_GRAY));
            return;
        }
        if (!inventory.spend(ResourceType.NICKEL, Mine.COST_NICKEL)) {
            popups.add(new Popup(x, y - 6, "Need " + Mine.COST_NICKEL + " nickel", new Color(255, 150, 120)));
            return;
        }
        defenses.add(new Mine(col, row));
    }

    private boolean tileInBuildReach(int col, int row) {
        return inReach(col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0,
                row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0, Constants.BUILD_REACH);
    }

    /** Can a torch go on this tile? (land only, not under the post or lighthouse, and not too many) */
    public boolean canPlaceTorch(int col, int row) {
        if (col < 0 || row < 0 || col >= Constants.MAP_COLS || row >= Constants.MAP_ROWS || map.isWater(row, col)) {
            return false;
        }
        if ((col == Constants.POST_COL && row == Constants.POST_ROW)
                || (col == Constants.LIGHTHOUSE_COL && row == Constants.LIGHTHOUSE_ROW)) {
            return false;
        }
        for (Defense d : defenses) {
            if (d.occupies(col, row)) {
                return false;
            }
        }
        return true;
    }

    public int getTorchCount() {
        return torchCount();
    }

    public int getLampLevel() {
        return beam.getUpgradeLevel();
    }

    private int torchCount() {
        int n = 0;
        for (Defense d : defenses) {
            if (d instanceof Torch) {
                n++;
            }
        }
        return n;
    }

    private void placeTorch(int x, int y) {
        int col = x / Constants.TILE_SIZE;
        int row = y / Constants.TILE_SIZE;
        if (x < 0 || y < 0 || !canPlaceTorch(col, row)) {
            popups.add(new Popup(x, y - 6, "Free land only", Color.LIGHT_GRAY));
            return;
        }
        if (torchCount() >= Constants.TORCH_MAX) {
            popups.add(new Popup(x, y - 6, "Only " + Constants.TORCH_MAX + " torches", Color.LIGHT_GRAY));
            return;
        }
        if (!tileInBuildReach(col, row)) {
            popups.add(new Popup(x, y - 6, "Too far - walk closer", Color.LIGHT_GRAY));
            return;
        }
        if (!inventory.spend(ResourceType.BAMBOO, Constants.TORCH_COST_BAMBOO)) {
            popups.add(new Popup(x, y - 6, "Need " + Constants.TORCH_COST_BAMBOO + " bamboo", new Color(255, 150, 120)));
            return;
        }
        defenses.add(new Torch(col, row));
    }

    // ------------------------------------------------------------------
    // PANA TOWERS
    // ------------------------------------------------------------------

    public int getPanaCount() {
        return panaCount();
    }

    private int panaCount() {
        int n = 0;
        for (Defense d : defenses) {
            if (d instanceof PanaTower) {
                n++;
            }
        }
        return n;
    }

    private void placePana(int x, int y) {
        int col = x / Constants.TILE_SIZE;
        int row = y / Constants.TILE_SIZE;
        if (x < 0 || y < 0 || !canPlaceTorch(col, row)) {   // same rule as a torch: free land
            return;
        }
        if (!tileInBuildReach(col, row)) {
            popups.add(new Popup(x, y - 6, "Too far", Color.LIGHT_GRAY));
            return;
        }
        if (panaCount() >= Constants.PANA_MAX) {
            popups.add(new Popup(x, y - 6, "Only " + Constants.PANA_MAX + " towers", Color.LIGHT_GRAY));
            return;
        }
        if (!inventory.spend(ResourceType.BAMBOO, Constants.PANA_COST_BAMBOO)) {
            popups.add(new Popup(x, y - 6, "Need " + Constants.PANA_COST_BAMBOO + " bamboo", new Color(255, 150, 120)));
            return;
        }
        defenses.add(new PanaTower(col, row));
    }

    // ------------------------------------------------------------------
    // SURVIVORS (they come by bangka at dawn after nights 2, 4 and 6)
    // ------------------------------------------------------------------

    private void updateSurvivors() {
        for (int i = boats.size() - 1; i >= 0; i--) {
            Bangka boat = boats.get(i);
            boat.update();
            Survivor passenger = boat.releaseCargo();
            if (passenger != null) {
                survivors.add(passenger);
                storyTitle = passenger.getArrivalTitle();
                storyText = passenger.getArrivalLine();
                storyTimer = 8 * Constants.TARGET_FPS;
            }
            if (boat.isGone()) {
                boats.remove(i);
            }
        }
        for (Survivor sv : survivors) {
            sv.update(map);
            sv.act(post, radio, ships, frame);
        }
    }

    private boolean crateBoosted() {
        for (Survivor sv : survivors) {
            if (sv.boostsCrates()) {
                return true;
            }
        }
        return false;
    }

    /** Which survivor comes after this night? 0, 1 or 2, or -1 if none. */
    private int arrivalIndex(int night) {
        for (int i = 0; i < ARRIVAL_NIGHTS.length; i++) {
            if (ARRIVAL_NIGHTS[i] == night) {
                return i;
            }
        }
        return -1;
    }

    /** A new survivor, standing "index" (0, 1, 2): where they land and where they will stand near the post. */
    private Survivor makeSurvivor(int index, double startX, double startY) {
        double px = post.getX();
        double py = post.getY();
        switch (index) {
            case 0:
                return new KaInday(startX, startY, px - 22, py + 10);
            case 1:
                return new Migo(startX, startY, px + 24, py + 12);
            default:
                return new Tonyo(startX, startY, px - 10, py - 20);
        }
    }

    /** Finds the shore at the post's row on the west (even index) or east (odd index) side. */
    private double[] shoreAt(boolean west) {
        int row = Constants.POST_ROW;
        if (west) {
            int c = 0;
            while (c < Constants.MAP_COLS && map.isWater(row, c)) {
                c++;
            }
            return new double[]{(c - 1) * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0, c * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0};   // water x, first land x
        }
        int c = Constants.MAP_COLS - 1;
        while (c >= 0 && map.isWater(row, c)) {
            c--;
        }
        return new double[]{(c + 1) * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0, c * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0};
    }

    private void sendBangka(int night) {
        int index = arrivalIndex(night);
        if (index < 0) {
            return;
        }
        boolean west = index % 2 == 0;
        double[] shore = shoreAt(west);
        double y = Constants.POST_ROW * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
        double startX = west ? -24 : Constants.INTERNAL_WIDTH + 24;
        Survivor passenger = makeSurvivor(index, shore[1], y + 4);
        boats.add(new Bangka(startX, y, shore[0], passenger));
    }

    /** Test/demo key (J): all three survivors are at the post right now. */
    public void testSurvivors() {
        if (!survivors.isEmpty()) {
            return;
        }
        for (int i = 0; i < ARRIVAL_NIGHTS.length; i++) {
            survivors.add(makeSurvivor(i, post.getX(), post.getY() + 6));   // they step out from the post to their spots
        }
        storyTitle = "Everyone made it";
        storyText = "Ka Inday, Migo and Tonyo are at the post.";
        storyTimer = 4 * Constants.TARGET_FPS;
    }

    public ArrayList<Survivor> getSurvivors() {
        return survivors;
    }

    public String getStoryTitle() {
        return storyTimer > 0 ? storyTitle : "";
    }

    public String getStoryText() {
        return storyTimer > 0 ? storyText : "";
    }

    public double getStoryFade() {
        return Math.min(1.0, storyTimer / 40.0);
    }

    /** Click a LIT ship to shoot it. You cannot hit what the beam has not found. */
    private void fire(int x, int y) {
        if (gunCooldown > 0) {
            return;
        }
        Warship target = null;
        for (Warship ship : ships) {
            if (ship.isLit() && ship.contains(x, y)) {
                target = ship;
            }
        }
        if (target == null) {
            return;
        }
        if (!inReach(target.getX(), target.getY(), Constants.FIRE_RANGE)) {
            popups.add(new Popup(target.getX(), target.getY() - 10, "Out of range", Color.LIGHT_GRAY));
            return;
        }
        gunCooldown = Constants.GUN_COOLDOWN;
        target.hit(GUN_DAMAGE);
        shotX = target.getX();
        shotY = target.getY();
        shotTimer = 6;
        if (target.isDead()) {
            popups.add(new Popup(shotX, shotY - 10, "Sunk!", new Color(255, 230, 120)));
        }
    }

    /** Key R: eat fish (2 meals) to mend the post and boost the radio a little. */
    public void repair() {
        if (cycle.isNight() || outcome != Outcome.PLAYING) {
            return;
        }
        if (post.getHealth() >= post.getMaxHealth() && radio.getSignal() >= Radio.MAX_SIGNAL) {
            say("Nothing to repair", Color.LIGHT_GRAY);
            return;
        }
        if (!inventory.spendMeals(Constants.REPAIR_FISH_COST)) {
            say("Need " + Constants.REPAIR_FISH_COST + " meals of fish", new Color(255, 150, 120));
            return;
        }
        post.repair(Constants.REPAIR_POST_AMOUNT);
        radio.restore(Constants.REPAIR_RADIO_AMOUNT);
        say("Repaired", new Color(140, 235, 150));
    }

    /** Key C: spend copper to rewire the radio (a bigger boost than fish). */
    public void rewire() {
        if (cycle.isNight() || outcome != Outcome.PLAYING) {
            return;
        }
        if (radio.getSignal() >= Radio.MAX_SIGNAL) {
            say("Radio is already strong", Color.LIGHT_GRAY);
        } else if (!inventory.spend(ResourceType.COPPER, Constants.REWIRE_COST_COPPER)) {
            say("Need " + Constants.REWIRE_COST_COPPER + " copper", new Color(255, 150, 120));
        } else {
            radio.restore(Constants.REWIRE_RADIO_AMOUNT);
            say("Radio rewired", ResourceType.COPPER.getColor());
        }
    }

    /** Key V: spend chromite to armor the post (more maximum health). */
    public void armorPost() {
        if (cycle.isNight() || outcome != Outcome.PLAYING) {
            return;
        }
        if (post.getArmorLevel() >= Constants.ARMOR_MAX_LEVEL) {
            say("Post is fully armored", Color.LIGHT_GRAY);
        } else if (!inventory.spend(ResourceType.CHROMITE, Constants.ARMOR_COST_CHROMITE)) {
            say("Need " + Constants.ARMOR_COST_CHROMITE + " chromite", new Color(255, 150, 120));
        } else {
            post.armor(Constants.ARMOR_HEALTH);
            say("Post armored +" + Constants.ARMOR_HEALTH, ResourceType.CHROMITE.getColor());
        }
    }

    /** Key G: spend gold to upgrade the lamp (a wider and longer beam). */
    public void upgradeLamp() {
        if (cycle.isNight() || outcome != Outcome.PLAYING) {
            return;
        }
        if (beam.getUpgradeLevel() >= Constants.LAMP_MAX_LEVEL) {
            say("Lamp is fully upgraded", Color.LIGHT_GRAY);
        } else if (!inventory.spend(ResourceType.GOLD, Constants.LAMP_COST_GOLD)) {
            say("Need " + Constants.LAMP_COST_GOLD + " gold", new Color(255, 150, 120));
        } else {
            beam.upgrade(Constants.LAMP_MAX_LEVEL);
            say("Lamp upgraded", ResourceType.GOLD.getColor());
        }
    }

    /** A short message over the post. */
    private void say(String text, Color color) {
        popups.add(new Popup(post.getX(), post.getY() - 30, text, color));
    }

    // ------------------------------------------------------------------
    // DRAW (the world only; the HUD is drawn on top by Hud)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frameCount) {
        double darkness = forcedDarkness >= 0 ? forcedDarkness : cycle.getDarkness();

        map.draw(g, frameCount);

        for (ResourceNode node : nodes) {
            node.draw(g, frameCount);
        }
        for (FishSchool school : fish) {
            school.draw(g, frameCount);
        }
        for (SupplyCrate crate : crates) {
            crate.draw(g, frameCount);
        }
        for (Defense d : defenses) {
            d.draw(g, frameCount);
        }
        for (Bangka boat : boats) {
            boat.draw(g, frameCount);
        }
        post.draw(g, frameCount, !radio.isSilent());
        for (Survivor sv : survivors) {
            sv.draw(g, frameCount);
        }
        lighthouse.draw(g, darkness > 0.5);
        player.draw(g, frameCount);
        for (Warship ship : ships) {
            ship.draw(g, darkness);
        }

        // Where a net would go (day only)
        if (tool != Tool.HARVEST && !cycle.isNight()) {
            drawBuildGhost(g);
        }

        // The darkness and the light inside it
        ArrayList<GlowSource> glows = glowSources();
        overlay.draw(g, beam, darkness, glows);
        if (darkness > 0.01) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) darkness));
            beam.drawLight(g);
            for (GlowSource glow : glows) {
                NightOverlay.drawWarmth(g, glow);
            }
            g.setComposite(old);
        }

        dragon.draw(g, frameCount);   // the Bakunawa and its moon, on top of the darkness

        // The gun's shot (on top of the darkness so it is seen)
        if (shotTimer > 0) {
            g.setColor(new Color(255, 235, 150, 60 + shotTimer * 25));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine((int) player.getX(), (int) player.getY() - 6, (int) shotX, (int) shotY);
            g.setStroke(new BasicStroke(1f));
        }

        drawHoldRing(g);
        for (Explosion e : explosions) {
            e.draw(g);
        }
        for (Popup p : popups) {
            p.draw(g);
        }
    }

    /** The ring that fills while you hold the mouse on something. */
    private void drawHoldRing(Graphics2D g) {
        if (holdTarget == null || holdProgress <= 0) {
            return;
        }
        double fraction = holdProgress / (double) holdTarget.getHoldFrames();
        if (holdTarget instanceof FishSchool || holdTarget instanceof SupplyCrate) {   // the fishing line
            g.setColor(new Color(255, 255, 255, 120));
            g.drawLine((int) player.getX(), (int) player.getY() - 8, (int) holdTarget.getX(), (int) holdTarget.getY());
        }
        int r = 11;
        int x = (int) holdTarget.getX() - r;
        int y = (int) holdTarget.getY() - r;
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(0, 0, 0, 110));
        g.drawOval(x, y, r * 2, r * 2);
        g.setColor(holdTarget.getType().getColor().brighter());
        g.drawArc(x, y, r * 2, r * 2, 90, (int) (-360 * Math.min(1.0, fraction)));
        g.setStroke(new BasicStroke(1f));
    }

    private void drawBuildGhost(Graphics2D g) {
        int col = mouseX / Constants.TILE_SIZE;
        int row = mouseY / Constants.TILE_SIZE;
        if (mouseX < 0 || mouseY < 0) {
            return;
        }
        boolean ok;
        if (tool == Tool.NET) {
            ok = canPlaceNet(col, row) && inventory.has(ResourceType.BAMBOO, Net.COST_BAMBOO);
        } else if (tool == Tool.MINE) {
            ok = canPlaceMine(col, row) && inventory.has(ResourceType.NICKEL, Mine.COST_NICKEL);
        } else if (tool == Tool.PANA) {
            ok = canPlaceTorch(col, row) && panaCount() < Constants.PANA_MAX
                    && inventory.has(ResourceType.BAMBOO, Constants.PANA_COST_BAMBOO);
        } else {
            ok = canPlaceTorch(col, row) && torchCount() < Constants.TORCH_MAX
                    && inventory.has(ResourceType.BAMBOO, Constants.TORCH_COST_BAMBOO);
        }
        ok = ok && tileInBuildReach(col, row);
        // A faint circle shows how far the tanod can build.
        g.setColor(new Color(255, 255, 255, 40));
        int r = (int) Constants.BUILD_REACH;
        g.drawOval((int) player.getX() - r, (int) player.getY() - r, r * 2, r * 2);
        g.setColor(ok ? new Color(120, 255, 140, 90) : new Color(255, 90, 80, 90));
        g.fillRect(col * Constants.TILE_SIZE, row * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);

        // What it costs and what you have, next to the cursor
        String label;
        if (tool == Tool.NET) {
            label = "Net " + Net.COST_BAMBOO + " bamboo (you have " + inventory.get(ResourceType.BAMBOO) + ")";
        } else if (tool == Tool.MINE) {
            label = "Mine " + Mine.COST_NICKEL + " nickel (you have " + inventory.get(ResourceType.NICKEL) + ")";
        } else if (tool == Tool.PANA) {
            label = "Pana tower " + Constants.PANA_COST_BAMBOO + " bamboo (you have " + inventory.get(ResourceType.BAMBOO) + ")";
        } else {
            label = "Torch " + Constants.TORCH_COST_BAMBOO + " bamboo (you have " + inventory.get(ResourceType.BAMBOO) + ")";
        }
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
        int lx = Math.min(mouseX + 12, Constants.INTERNAL_WIDTH - g.getFontMetrics().stringWidth(label) - 6);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRoundRect(lx - 3, mouseY - 16, g.getFontMetrics().stringWidth(label) + 6, 12, 4, 4);
        g.setColor(ok ? new Color(190, 255, 200) : new Color(255, 190, 170));
        g.drawString(label, lx, mouseY - 7);
    }

    // ------------------------------------------------------------------
    // GETTERS (the HUD reads these)
    // ------------------------------------------------------------------

    public DayCycle getCycle() {
        return cycle;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Post getPost() {
        return post;
    }

    public Radio getRadio() {
        return radio;
    }

    public Tool getTool() {
        return tool;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public String getEndReason() {
        return endReason;
    }

    public String getBannerText() {
        return bannerText;
    }

    /** 0 to 1: how visible the banner is (fades out at the end). */
    public double getBannerFade() {
        return Math.min(1.0, bannerTimer / 40.0);
    }

    public int getShipsLeftTonight() {
        return Math.max(0, queue.size() - spawned) + ships.size();
    }

    public int getShipsTotalTonight() {
        return queue.size();
    }

    public int getShipCount() {
        return ships.size();
    }

    public int getLitCount() {
        int n = 0;
        for (Warship s : ships) {
            if (s.isLit()) {
                n++;
            }
        }
        return n;
    }

    public Player getPlayer() {
        return player;
    }

    public Beam getBeam() {
        return beam;
    }

    public int getGunCooldown() {
        return gunCooldown;
    }
}
