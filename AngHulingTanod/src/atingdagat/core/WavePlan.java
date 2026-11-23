package atingdagat.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * WavePlan.java
 *
 * Reads data/waves.txt: for each night, how many skiffs, cutters and barges attack,
 * and how many seconds pass between ships. Arrays hold the numbers, and a missing or
 * broken file falls back to safe defaults so the game never crashes.
 */
public class WavePlan {

    /** The kinds of enemy ship. The number in each array slot below matches this order. */
    public enum Kind { SKIFF, CUTTER, BARGE, RAIDER, GHOST }

    private final int[][] count = new int[Constants.TOTAL_NIGHTS + 1][Kind.values().length]; // [night][kind]
    private final int[] interval = new int[Constants.TOTAL_NIGHTS + 1];                      // in frames

    public WavePlan(String path) {
        // Defaults first: they stay for any night the file does not mention.
        for (int n = 1; n <= Constants.TOTAL_NIGHTS; n++) {
            count[n][Kind.SKIFF.ordinal()] = 2 + n * 2;
            count[n][Kind.CUTTER.ordinal()] = n > 1 ? n : 0;
            count[n][Kind.BARGE.ordinal()] = n > 2 ? n - 2 : 0;
            count[n][Kind.RAIDER.ordinal()] = 0;   // the newer ships come only from the file
            count[n][Kind.GHOST.ordinal()] = 0;
            interval[n] = (int) ((5.0 - n * 0.3) * Constants.TARGET_FPS);
        }

        InputStream in = WavePlan.class.getResourceAsStream(path);
        if (in == null) {
            System.out.println("WavePlan: file not found (" + path + "), using default waves.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                parseLine(line);
            }
        } catch (IOException e) {
            System.out.println("WavePlan: could not read " + path + ": " + e.getMessage());
        }
    }

    /** One line: night skiffs cutters barges seconds. */
    private void parseLine(String line) {
        String[] parts = line.split("\\s+");
        try {
            int night = Integer.parseInt(parts[0]);
            int skiffs = Integer.parseInt(parts[1]);
            int cutters = Integer.parseInt(parts[2]);
            int barges = Integer.parseInt(parts[3]);
            double seconds = Double.parseDouble(parts[4]);
            int raiders = parts.length > 5 ? Integer.parseInt(parts[5]) : 0;   // optional columns, so old files still work
            int ghosts = parts.length > 6 ? Integer.parseInt(parts[6]) : 0;
            if (night >= 1 && night <= Constants.TOTAL_NIGHTS
                    && skiffs >= 0 && cutters >= 0 && barges >= 0 && raiders >= 0 && ghosts >= 0 && seconds > 0) {
                count[night][Kind.SKIFF.ordinal()] = skiffs;
                count[night][Kind.CUTTER.ordinal()] = cutters;
                count[night][Kind.BARGE.ordinal()] = barges;
                count[night][Kind.RAIDER.ordinal()] = raiders;
                count[night][Kind.GHOST.ordinal()] = ghosts;
                interval[night] = (int) (seconds * Constants.TARGET_FPS);
            }
        } catch (RuntimeException e) {   // bad number or missing column
            System.out.println("WavePlan: skipped a bad line: " + line);
        }
    }

    public int getCount(int night, Kind kind) {
        int base = count[clamp(night)][kind.ordinal()];
        if (night <= Constants.TOTAL_NIGHTS) {
            return base;
        }
        // Endless mode: night 7's fleet, 15% bigger for every night past it.
        return (int) Math.round(base * (1.0 + 0.15 * (night - Constants.TOTAL_NIGHTS)));
    }

    public int getInterval(int night) {
        int base = interval[clamp(night)];
        if (night <= Constants.TOTAL_NIGHTS) {
            return base;
        }
        // ...and 4% faster each night, but never faster than 60% of night 7's pace.
        return (int) (base * Math.max(0.6, 1.0 - 0.04 * (night - Constants.TOTAL_NIGHTS)));
    }

    private int clamp(int night) {
        return Math.max(1, Math.min(Constants.TOTAL_NIGHTS, night));
    }
}
