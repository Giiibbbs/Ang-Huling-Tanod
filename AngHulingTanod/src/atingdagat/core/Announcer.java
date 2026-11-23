package atingdagat.core;

/**
 * Announcer.java
 *
 * The two kinds of message the player sees: the big BANNER ("NIGHT 3: Fog") and the STORY card (a title and a
 * few lines at the bottom: radio voices, survivors arriving, folklore). Each is just some text and a countdown.
 * It was part of GameWorld until Nov 23; the HUD still asks GameWorld, which passes the question on to this class.
 */
public class Announcer {

    private static final int BANNER_FRAMES = 150;
    private static final int FADE_FRAMES = 40;

    private String bannerText = "";
    private int bannerTimer = 0;
    private String storyTitle = "";
    private String storyText = "";
    private int storyTimer = 0;

    /** Call once per frame: both messages fade out by themselves. */
    public void tick() {
        if (bannerTimer > 0) {
            bannerTimer--;
        }
        if (storyTimer > 0) {
            storyTimer--;
        }
    }

    public void banner(String text) {
        bannerText = text;
        bannerTimer = BANNER_FRAMES;
    }

    /** Shows a story card for the given number of seconds. */
    public void story(String title, String text, int seconds) {
        storyTitle = title;
        storyText = text;
        storyTimer = seconds * Constants.TARGET_FPS;
    }

    public String getBannerText() {
        return bannerText;
    }

    /** 0 to 1: how visible the banner is (fades out at the end). */
    public double getBannerFade() {
        return Math.min(1.0, bannerTimer / (double) FADE_FRAMES);
    }

    public String getStoryTitle() {
        return storyTimer > 0 ? storyTitle : "";
    }

    public String getStoryText() {
        return storyTimer > 0 ? storyText : "";
    }

    public double getStoryFade() {
        return Math.min(1.0, storyTimer / (double) FADE_FRAMES);
    }
}
