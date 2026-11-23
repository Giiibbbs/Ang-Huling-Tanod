package atingdagat.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HighScores.java
 *
 * The top runs, kept in a plain text file (highscores.txt, one ScoreEntry per line) next to the game.
 * File I/O: the file is read when the game starts and written every time a new score is added.
 * Data structure: an ArrayList of ScoreEntry, kept sorted (best first) and cut to MAX_SCORES.
 *
 * If the file is missing or a line is damaged, the game does not crash: it starts with what it could read.
 */
public class HighScores {

    private final List<ScoreEntry> entries = new ArrayList<>();
    private final String path;

    public HighScores(String path) {
        this.path = path;
        load();
    }

    private void load() {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = in.readLine()) != null) {
                ScoreEntry entry = ScoreEntry.fromLine(line);
                if (entry != null) {   // skip damaged lines
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            // No file yet (first time playing): the table just starts empty.
        }
        tidy();
    }

    private void save() {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            for (ScoreEntry entry : entries) {
                out.println(entry.toLine());
            }
        } catch (IOException e) {
            System.out.println("Could not save high scores: " + e.getMessage());   // the game keeps running
        }
    }

    /** Sorts best-first and keeps only the top MAX_SCORES. */
    private void tidy() {
        Collections.sort(entries);
        while (entries.size() > Constants.MAX_SCORES) {
            entries.remove(entries.size() - 1);
        }
    }

    /** Would this run make it onto the table? (A run that survived no night does not count.) */
    public boolean qualifies(ScoreEntry candidate) {
        if (candidate.getNights() < 1) {
            return false;
        }
        if (entries.size() < Constants.MAX_SCORES) {
            return true;
        }
        return candidate.compareTo(entries.get(entries.size() - 1)) < 0;   // sorts before the worst entry
    }

    public void add(ScoreEntry entry) {
        entries.add(entry);
        tidy();
        save();
    }

    /** The table, best first. A copy, so nobody outside can change the real list. */
    public List<ScoreEntry> getTop() {
        return new ArrayList<>(entries);
    }
}
