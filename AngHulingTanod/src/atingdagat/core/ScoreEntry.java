package atingdagat.core;

/**
 * ScoreEntry.java
 *
 * One line of the high-score table: who played, how many nights they survived, and how much
 * health the Post had left. Comparable, so a list of entries can be sorted with Collections.sort:
 * more nights first, and when two runs survived the same number of nights, more Post health first.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {

    private final String name;
    private final int nights;
    private final int postHealth;

    public ScoreEntry(String name, int nights, int postHealth) {
        this.name = name;
        this.nights = nights;
        this.postHealth = postHealth;
    }

    public String getName() {
        return name;
    }

    public int getNights() {
        return nights;
    }

    /** "5/7" for a normal run; just the number once the run went past night 7 (endless mode). */
    public String getNightsText() {
        return nights > Constants.TOTAL_NIGHTS ? nights + " (endless)" : nights + "/" + Constants.TOTAL_NIGHTS;
    }

    public int getPostHealth() {
        return postHealth;
    }

    /** Best score sorts FIRST (so the top of the table is index 0). */
    @Override
    public int compareTo(ScoreEntry other) {
        if (nights != other.nights) {
            return Integer.compare(other.nights, nights);
        }
        return Integer.compare(other.postHealth, postHealth);
    }

    /** The text-file format: name|nights|postHealth. */
    public String toLine() {
        return name + "|" + nights + "|" + postHealth;
    }

    /** Reads one line of the file. Returns null if the line is damaged, so the caller can skip it. */
    public static ScoreEntry fromLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3 || parts[0].trim().isEmpty()) {
            return null;
        }
        try {
            int nights = Integer.parseInt(parts[1].trim());
            int hp = Integer.parseInt(parts[2].trim());
            if (nights < 0 || nights > Constants.MAX_SCORE_NIGHTS || hp < 0) {
                return null;
            }
            return new ScoreEntry(parts[0].trim(), nights, hp);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
