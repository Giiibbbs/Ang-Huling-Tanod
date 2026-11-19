package atingdagat.core;

import atingdagat.defense.Defense;
import atingdagat.defense.Net;
import atingdagat.economy.Inventory;
import atingdagat.economy.ResourceNode;
import atingdagat.economy.ResourceType;
import atingdagat.entities.Beam;
import atingdagat.entities.Lighthouse;
import atingdagat.entities.Post;
import atingdagat.entities.Radio;
import atingdagat.entities.Skiff;
import atingdagat.entities.Warship;
import atingdagat.map.TileMap;
import atingdagat.ui.NightOverlay;
import atingdagat.ui.Popup;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayList;
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

    private static final int BANNER_FRAMES = 150;
    private static final int GUN_DAMAGE = 1;

    // The ground and the fixed things on it
    private final TileMap map;
    private final Lighthouse lighthouse = new Lighthouse(Constants.LIGHTHOUSE_COL, Constants.LIGHTHOUSE_ROW);
    private final Post post = new Post(Constants.POST_COL, Constants.POST_ROW);
    private final Radio radio = new Radio();
    private final Beam beam = new Beam(lighthouse.getLampX(), lighthouse.getLampY());
    private final NightOverlay overlay = new NightOverlay();

    // Things that come and go
    private final ArrayList<Warship> ships = new ArrayList<>();
    private final ArrayList<Defense> defenses = new ArrayList<>();
    private final ArrayList<ResourceNode> nodes;
    private final ArrayList<Popup> popups = new ArrayList<>();

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

    private boolean netMode = false;     // true = clicking builds a net, false = clicking harvests
    private int spawned = 0;             // skiffs sent so far tonight
    private int spawnTimer = 0;
    private int gunCooldown = 0;
    private int shotTimer = 0;           // draws the shot line for a few frames
    private double shotX, shotY;

    private String bannerText = "";
    private int bannerTimer = 0;

    public GameWorld(TileMap map) {
        this.map = map;
        this.nodes = ResourceNode.load(Constants.NODES_FILE, map);
        inventory.add(ResourceType.BAMBOO, Constants.START_BAMBOO);
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

        beam.aimAt(mouseX, mouseY);
        beam.update();

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

        checkNightOver();
        checkLoss();
    }

    private void spawnShips() {
        int night = cycle.getNight();
        spawnTimer++;
        if (spawned < plan.getCount(night) && spawnTimer >= plan.getInterval(night)) {
            spawnTimer = 0;
            spawned++;
            spawnSkiff();
        }
    }

    /** A skiff appears just outside the screen, in a lane that points at the post. */
    private void spawnSkiff() {
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
        ships.add(new Skiff(x, y, post.getX(), post.getY()));
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
            ship.update(map, beam, defenses);
            if (ship.isDead()) {
                ships.remove(i);
            } else if (ship.hasArrived()) {
                ships.remove(i);
                post.damage(Constants.LANDING_POST_DAMAGE);
                radio.weaken(Constants.LANDING_RADIO_DAMAGE);
                popups.add(new Popup(post.getX(), post.getY() - 30, "-" + Constants.LANDING_POST_DAMAGE + " Post", new Color(255, 110, 100)));
            }
        }
    }

    private void updateDefenses() {
        for (int i = defenses.size() - 1; i >= 0; i--) {
            if (defenses.get(i).isDestroyed()) {
                defenses.remove(i);
            }
        }
    }

    private void checkNightOver() {
        if (cycle.isNight() && spawned >= plan.getCount(cycle.getNight()) && ships.isEmpty()) {
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
                spawned = 0;
                spawnTimer = plan.getInterval(cycle.getNight()) - 90;   // first skiff in about 1.5 seconds
                showBanner("NIGHT " + cycle.getNight());
                break;
            default: // DAWN
                for (ResourceNode node : nodes) {
                    node.refill();
                }
                showBanner("DAWN - R repairs");
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
    // PLAYER ACTIONS
    // ------------------------------------------------------------------

    /** x and y are in game coordinates. button: 1 = left, 3 = right. */
    public void click(int x, int y, int button) {
        if (outcome != Outcome.PLAYING) {
            return;
        }
        if (button == 3) {
            netMode = false;   // right click puts the net tool away
            return;
        }
        if (cycle.isNight()) {
            fire(x, y);
        } else if (netMode) {
            placeNet(x, y);
        } else {
            harvest(x, y);
        }
    }

    /** Key 1: switch between the net tool and harvesting. */
    public void toggleNetMode() {
        if (!cycle.isNight()) {
            netMode = !netMode;
        }
    }

    private void harvest(int x, int y) {
        for (ResourceNode node : nodes) {
            if (!node.isNear(x, y)) {
                continue;
            }
            if (node.harvest()) {
                int gain = node.getType() == ResourceType.BAMBOO ? 2 : 1;
                inventory.add(node.getType(), gain);
                popups.add(new Popup(node.getX(), node.getY() - 12, "+" + gain + " " + node.getType().getLabel(), node.getType().getColor()));
            } else if (node.getRemaining() <= 0) {
                popups.add(new Popup(node.getX(), node.getY() - 12, "Empty until dawn", Color.LIGHT_GRAY));
            }
            return;
        }
    }

    /** Can a net go on this tile? */
    public boolean canPlaceNet(int col, int row) {
        if (col < 0 || row < 0 || col >= Constants.MAP_COLS || row >= Constants.MAP_ROWS) {
            return false;
        }
        if (!map.isWater(row, col) || map.getDepth(row, col) > Constants.NET_MAX_DEPTH) {
            return false;
        }
        for (Defense d : defenses) {
            if (d.occupies(col, row)) {
                return false;
            }
        }
        for (ResourceNode node : nodes) {   // do not bury a fish spot
            if (node.getType() == ResourceType.FISH
                    && (int) (node.getX() / Constants.TILE_SIZE) == col
                    && (int) (node.getY() / Constants.TILE_SIZE) == row) {
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
        if (!inventory.spend(ResourceType.BAMBOO, Net.COST_BAMBOO)) {
            popups.add(new Popup(x, y - 6, "Need " + Net.COST_BAMBOO + " bamboo", new Color(255, 150, 120)));
            return;
        }
        defenses.add(new Net(col, row));
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
        gunCooldown = Constants.GUN_COOLDOWN;
        target.hit(GUN_DAMAGE);
        shotX = target.getX();
        shotY = target.getY();
        shotTimer = 6;
        if (target.isDead()) {
            popups.add(new Popup(shotX, shotY - 10, "Sunk!", new Color(255, 230, 120)));
        }
    }

    /** Key R: spend fish to mend the post and boost the radio. */
    public void repair() {
        if (cycle.isNight() || outcome != Outcome.PLAYING) {
            return;
        }
        if (post.getHealth() >= Post.MAX_HEALTH && radio.getSignal() >= Radio.MAX_SIGNAL) {
            popups.add(new Popup(post.getX(), post.getY() - 30, "Nothing to repair", Color.LIGHT_GRAY));
            return;
        }
        if (!inventory.spend(ResourceType.FISH, Constants.REPAIR_FISH_COST)) {
            popups.add(new Popup(post.getX(), post.getY() - 30, "Need " + Constants.REPAIR_FISH_COST + " fish", new Color(255, 150, 120)));
            return;
        }
        post.repair(Constants.REPAIR_POST_AMOUNT);
        radio.restore(Constants.REPAIR_RADIO_AMOUNT);
        popups.add(new Popup(post.getX(), post.getY() - 30, "Repaired", new Color(140, 235, 150)));
    }

    // ------------------------------------------------------------------
    // DRAW (the world only; the HUD is drawn on top by Hud)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frameCount) {
        double darkness = cycle.getDarkness();

        map.draw(g, frameCount);

        for (ResourceNode node : nodes) {
            node.draw(g, frameCount);
        }
        for (Defense d : defenses) {
            d.draw(g, frameCount);
        }
        post.draw(g, frameCount, !radio.isSilent());
        lighthouse.draw(g, darkness > 0.5);
        for (Warship ship : ships) {
            ship.draw(g, darkness);
        }

        // Where a net would go (day only)
        if (netMode && !cycle.isNight()) {
            drawNetGhost(g);
        }

        // The darkness and the light inside it
        overlay.draw(g, beam, darkness);
        if (darkness > 0.01) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) darkness));
            beam.drawLight(g);
            g.setComposite(old);
        }

        // The gun's shot (on top of the darkness so it is seen)
        if (shotTimer > 0) {
            g.setColor(new Color(255, 235, 150, 60 + shotTimer * 25));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine((int) lighthouse.getLampX(), (int) lighthouse.getLampY(), (int) shotX, (int) shotY);
            g.setStroke(new BasicStroke(1f));
        }

        for (Popup p : popups) {
            p.draw(g);
        }
    }

    private void drawNetGhost(Graphics2D g) {
        int col = mouseX / Constants.TILE_SIZE;
        int row = mouseY / Constants.TILE_SIZE;
        if (mouseX < 0 || mouseY < 0) {
            return;
        }
        boolean ok = canPlaceNet(col, row) && inventory.has(ResourceType.BAMBOO, Net.COST_BAMBOO);
        g.setColor(ok ? new Color(120, 255, 140, 90) : new Color(255, 90, 80, 90));
        g.fillRect(col * Constants.TILE_SIZE, row * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);
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

    public boolean isNetMode() {
        return netMode;
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
        return Math.max(0, plan.getCount(cycle.getNight()) - spawned) + ships.size();
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

    public int getGunCooldown() {
        return gunCooldown;
    }
}
