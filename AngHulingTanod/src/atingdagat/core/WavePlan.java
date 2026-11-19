package atingdagat.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * WavePlan.java
 *
 * Reads data/waves.txt: for each night, how many skiffs attack and how many
 * seconds pass between them. Arrays hold the numbers, and a missing or broken
 * file falls back to safe defaults so the game never crashes.
 */
public class WavePlan {

    private final int[] count = new int[Constants.TOTAL_NIGHTS + 1];      // index = night number (1 to 7)
    private final int[] interval = new int[Constants.TOTAL_NIGHTS + 1];   // in frames

    public WavePlan(String path) {
        // Defaults first: they stay for any night the file does not mention.
        for (int n = 1; n <= Constants.TOTAL_NIGHTS; n++) {
            count[n] = 2 + n * 2;
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

    private void parseLine(String line) {
        String[] parts = line.split("\\s+");
        try {
            int night = Integer.parseInt(parts[0]);
            int skiffs = Integer.parseInt(parts[1]);
            double seconds = Double.parseDouble(parts[2]);
            if (night >= 1 && night <= Constants.TOTAL_NIGHTS && skiffs >= 0 && seconds > 0) {
                count[night] = skiffs;
                interval[night] = (int) (seconds * Constants.TARGET_FPS);
            }
        } catch (RuntimeException e) {   // bad number or missing column
            System.out.println("WavePlan: skipped a bad line: " + line);
        }
    }

    public int getCount(int night) {
        return count[clamp(night)];
    }

    public int getInterval(int night) {
        return interval[clamp(night)];
    }

    private int clamp(int night) {
        return Math.max(1, Math.min(Constants.TOTAL_NIGHTS, night));
    }
}
