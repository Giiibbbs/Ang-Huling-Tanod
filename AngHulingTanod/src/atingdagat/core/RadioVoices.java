package atingdagat.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * RadioVoices.java
 *
 * Reads data/voices.txt: the faint voices from other islands that the radio catches at dusk, one
 * per night. The ending answers each of them by name as their bangka arrives, so the whole game
 * pays off the title: you were never the last tanod.
 *
 * Each line is: night | island | speaker | what you hear | what they say at the end.
 * A missing or broken file gives an empty list, and the game simply tells no voice stories.
 */
public class RadioVoices {

    /** One voice. Fields are final: a voice never changes once it is read. */
    public static class Voice {
        public final int night;
        public final String island;
        public final String speaker;
        public final String heard;
        public final String arrival;

        Voice(int night, String island, String speaker, String heard, String arrival) {
            this.night = night;
            this.island = island;
            this.speaker = speaker;
            this.heard = heard;
            this.arrival = arrival;
        }
    }

    private final List<Voice> voices = new ArrayList<>();

    public RadioVoices(String path) {
        InputStream in = RadioVoices.class.getResourceAsStream(path);
        if (in == null) {
            System.out.println("RadioVoices: file not found (" + path + "), no voices.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    parseLine(line);
                }
            }
        } catch (IOException e) {
            System.out.println("RadioVoices: could not read " + path + ": " + e.getMessage());
        }
    }

    private void parseLine(String line) {
        String[] p = line.split("\\|");
        try {
            voices.add(new Voice(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim()));
        } catch (RuntimeException e) {   // bad number or missing column
            System.out.println("RadioVoices: skipped a bad line: " + line);
        }
    }

    /** The voice heard at the dusk of this night, or null if there is none. */
    public Voice forNight(int night) {
        for (Voice v : voices) {
            if (v.night == night) {
                return v;
            }
        }
        return null;
    }

    public List<Voice> getAll() {
        return voices;
    }
}
