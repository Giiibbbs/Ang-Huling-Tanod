package atingdagat.core;

/**
 * Twist.java
 *
 * Each night adds ONE new rule, so the game keeps changing (like the nights of a horror game getting
 * meaner one by one). The world looks the twist up by the night number and applies it:
 * to the ships as they spawn, to the beam, or to the radio.
 */
public enum Twist {
    //          title                  what the player is told
    NONE        ("",                    ""),
    TWO_FRONTS  ("TWO FRONTS",          "Ships now come in pairs, from different sides at the same time. Watch both."),
    FOG         ("FOG",                 "A fog rolls in. The beam reaches only about 70% as far."),
    STATIC      ("STATIC",              "Interference on the radio. The signal slowly drains while ships are out there."),
    SURGE       ("SURGE",               "The tide pulls them in. Every ship is 25% faster tonight."),
    HARDENED    ("HARDENED HULLS",      "The Kordans bolted armor on. Every ship has one more health."),
    ASSAULT     ("ALL-OUT ASSAULT",     "Their last push: bigger groups, faster, and the Bakunawa is watching.");

    private final String title;
    private final String text;

    Twist(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    /** Night 1 is calm. Nights 2 to 7 each have their own rule. */
    public static Twist forNight(int night) {
        Twist[] all = values();
        if (night > Constants.TOTAL_NIGHTS) {   // endless mode: the seven rules come round again
            return all[1 + (night - 2) % (all.length - 1)];
        }
        return all[Math.max(0, Math.min(all.length - 1, night - 1))];
    }
}
