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
    public enum Kind { SKIFF, CUTTER, BARGE }

    private final int[][] count = new int[Constants.TOTAL_NIGHTS + 1][Kind.values().length]; // [night][kind]
    private final int[] interval = new int[Constants.TOTAL_NIGHTS + 1];                      // in frames

    public WavePlan(String path) {
        // Defaults first: they stay for any night the file does not mention.
        for (int n = 1; n <= Constants.TOTAL_NIGHTS; n++) {
            count[n][Kind.SKIFF.ordinal()] = 2 + n * 2;
            count[n][Kind.CUTTER.ordinal()] = n > 1 ? n : 0;
            count[n][Kind.BARGE.ordinal()] = n > 2 ? n - 2 : 0;
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
            if (night >= 1 && night <= Constants.TOTAL_NIGHTS
                    && skiffs >= 0 && cutters >= 0 && barges >= 0 && seconds > 0) {
                count[night][Kind.SKIFF.ordinal()] = skiffs;
                count[night][Kind.CUTTER.ordinal()] = cutters;
                count[night][Kind.BARGE.ordinal()] = barges;
                interval[night] = (int) (seconds * Constants.TARGET_FPS);
            }
        } catch (RuntimeException e) {   // bad number or missing column
            System.out.println("WavePlan: skipped a bad line: " + line);
        }
    }

    public int getCount(int night, Kind kind) {
        return count[clamp(night)][kind.ordinal()];
    }

    public int getInterval(int night) {
        return interval[clamp(night)];
    }

    private int clamp(int night) {
        return Math.max(1, Math.min(Constants.TOTAL_NIGHTS, night));
    }
}
