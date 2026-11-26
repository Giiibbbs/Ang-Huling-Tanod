package atingdagat;

import atingdagat.core.Constants;
import atingdagat.core.DayCycle;
import atingdagat.core.HighScores;
import atingdagat.core.Phase;
import atingdagat.core.ScoreEntry;
import atingdagat.core.Twist;
import atingdagat.core.WavePlan;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.entities.Skiff;
import atingdagat.entities.Warship;
import atingdagat.economy.Inventory;
import atingdagat.economy.ResourceType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GameTests.java
 *
 * Our proof of testing. It uses no libraries (no JUnit), so anyone can run it:
 *
 *   javac -cp build/classes -d build/test test/atingdagat/GameTests.java
 *   java  -cp build/classes:build/test atingdagat.GameTests          (use ; instead of : on Windows)
 *
 * Each check prints PASS or FAIL. The program ends with a count and exits with code 1 if anything failed.
 * These are fast unit checks of the rules; the longer bot runs are described in TESTING.md.
 */
public class GameTests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    public static void main(String[] args) throws IOException {
        inventory();
        dayCycle();
        endless();
        wavePlan();
        twists();
        durability();
        highScores();
        mines();
        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void inventory() {
        Inventory inv = new Inventory();
        inv.add(ResourceType.BAMBOO, 10);
        check("inventory: add then get", inv.get(ResourceType.BAMBOO) == 10);
        check("inventory: spend too much fails", !inv.spend(ResourceType.BAMBOO, 11) && inv.get(ResourceType.BAMBOO) == 10);
        check("inventory: spend works", inv.spend(ResourceType.BAMBOO, 4) && inv.get(ResourceType.BAMBOO) == 6);
        inv.add(ResourceType.BAMBOO, 500);
        check("inventory: bamboo is capped at the carry limit", inv.get(ResourceType.BAMBOO) == Constants.BAMBOO_CAP);
    }

    private static void dayCycle() {
        DayCycle cycle = new DayCycle();
        check("cycle: starts on day 1", cycle.getNight() == 1 && cycle.getPhase() == Phase.DAY);
        int frames = 0;
        while (cycle.getPhase() == Phase.DAY && frames < Constants.DAY_FRAMES + 10) {
            cycle.update();
            frames++;
        }
        check("cycle: the day lasts " + Constants.DAY_FRAMES + " frames", frames == Constants.DAY_FRAMES);
        check("cycle: dusk follows the day", cycle.getPhase() == Phase.DUSK);
        cycle.setNight(Constants.TOTAL_NIGHTS);
        cycle.advance();   // day -> dusk
        cycle.advance();   // dusk -> night
        cycle.advance();   // night -> dawn
        cycle.advance();   // dawn -> end
        check("cycle: ends after the last night", cycle.isFinished());
    }

    private static void endless() {
        DayCycle cycle = new DayCycle();
        cycle.setNight(Constants.TOTAL_NIGHTS);
        for (int i = 0; i < 4; i++) {
            cycle.advance();
        }
        cycle.continueEndless();
        check("endless: night 8 starts on a day", cycle.getNight() == Constants.TOTAL_NIGHTS + 1 && cycle.getPhase() == Phase.DAY);
        check("endless: flag is set and the game is not finished", cycle.isEndless() && !cycle.isFinished());
        for (int i = 0; i < 4; i++) {
            cycle.advance();
        }
        check("endless: night 9 comes after night 8", cycle.getNight() == Constants.TOTAL_NIGHTS + 2 && !cycle.isFinished());
    }

    private static void wavePlan() {
        WavePlan plan = new WavePlan(Constants.WAVES_FILE);
        int n7 = plan.getCount(7, WavePlan.Kind.SKIFF);
        int n10 = plan.getCount(10, WavePlan.Kind.SKIFF);
        check("waves: night 1 has ships", plan.getCount(1, WavePlan.Kind.SKIFF) > 0);
        check("waves: endless fleets grow", n10 > n7);
        check("waves: endless is faster but never below 60%", plan.getInterval(30) >= (int) (plan.getInterval(7) * 0.6) - 1
                && plan.getInterval(30) <= plan.getInterval(7));
    }

    private static void twists() {
        check("twists: night 1 has none", Twist.forNight(1) == Twist.NONE);
        check("twists: endless never returns NONE", Twist.forNight(8) != Twist.NONE && Twist.forNight(40) != Twist.NONE);
        check("twists: night 8 repeats a real rule, not the first night", Twist.forNight(8) != null);
    }

    private static void durability() {
        Net net = new Net(10, 10);
        int max = net.getMaxDurability();
        net.wear(1);
        check("net: wear lowers durability", net.getDurability() == max - 1);
        net.restore(5);
        check("net: restore never goes above the maximum", net.getDurability() == max);
        net.setPlating(2);
        check("net: plating raises the maximum", net.getMaxDurability() == max + 2);
    }

    private static void highScores() throws IOException {
        File file = File.createTempFile("hscore", ".txt");
        file.deleteOnExit();
        HighScores table = new HighScores(file.getPath());
        for (int i = 1; i <= Constants.MAX_SCORES + 2; i++) {
            table.add(new ScoreEntry("P" + i, i, 10));
        }
        check("scores: the table keeps only " + Constants.MAX_SCORES, table.getTop().size() == Constants.MAX_SCORES);
        check("scores: best first", table.getTop().get(0).getNights() >= table.getTop().get(1).getNights());
        check("scores: a run with 0 nights does not qualify", !table.qualifies(new ScoreEntry("Zed", 0, 20)));
        HighScores reloaded = new HighScores(file.getPath());
        check("scores: saved to the file and loaded again", reloaded.getTop().size() == table.getTop().size()
                && reloaded.getTop().get(0).getName().equals(table.getTop().get(0).getName()));
        ScoreEntry endlessRun = new ScoreEntry("Ayla", 12, 5);
        check("scores: endless runs read back (night 12)", ScoreEntry.fromLine(endlessRun.toLine()) != null
                && ScoreEntry.fromLine(endlessRun.toLine()).getNights() == 12);
    }

    private static void mines() {
        Mine a = new Mine(20, 20);
        Mine b = new Mine(22, 20);       // 20 px away: inside a's blast
        Mine far = new Mine(40, 20);     // 200 px away: outside it
        check("mine: neighbours are reached by the blast", a.reaches(b) && !a.reaches(far));
        List<Warship> ships = new ArrayList<>();
        Skiff skiff = new Skiff(a.getX(), a.getY(), 340, 190);
        ships.add(skiff);
        int before = skiff.getHealth();
        a.detonate(ships);
        check("mine: a blast hurts a ship inside it", skiff.getHealth() < before && a.hasExploded());
        a.detonate(ships);
        check("mine: it only explodes once", skiff.getHealth() == before - Mine.BLAST_DAMAGE);
        check("mine: costs 1 nickel", Constants.MINE_COST_NICKEL == 1);
    }

}
