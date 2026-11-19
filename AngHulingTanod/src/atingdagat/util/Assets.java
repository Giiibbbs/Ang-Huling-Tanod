package atingdagat.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Assets.java
 *
 * Loads art from inside the project, so it also works from the JAR.
 *
 * For each image it does three things:
 *  1. Reads the PNG.
 *  2. Scales it down to the size the game needs (in steps, so it stays clean).
 *  3. Turns the solid magenta (#FF00FF) background into transparency ("chroma key").
 *
 * If a file does not exist, get() returns null and the caller draws a placeholder.
 * Loaded images are remembered (cached), so the same file is never loaded twice.
 */
public final class Assets {

    // How magenta a pixel must be before we start removing it (0 to 1).
    private static final double KEY_START = 0.20;
    // At this much magenta, the pixel is fully removed.
    private static final double KEY_FULL = 0.85;

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private Assets() {
    }

    /**
     * Returns a recolored copy of an image: each pixel is blended toward the color by "amount" (0 to 1).
     * We use it so ONE rock picture can make nickel, copper, chromite and gold.
     */
    public static BufferedImage tint(BufferedImage source, java.awt.Color color, double amount) {
        if (source == null) {
            return null;
        }
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                int r = (int) (((argb >> 16) & 0xFF) * (1 - amount) + color.getRed() * amount);
                int g = (int) (((argb >> 8) & 0xFF) * (1 - amount) + color.getGreen() * amount);
                int b = (int) ((argb & 0xFF) * (1 - amount) + color.getBlue() * amount);
                out.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    /** Returns the image scaled to width x height with a transparent background, or null if missing. */
    public static BufferedImage get(String path, int width, int height) {
        String key = path + "@" + width + "x" + height;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        BufferedImage image = load(path, width, height);
        cache.put(key, image); // null is remembered too, so we only warn once
        return image;
    }

    private static BufferedImage load(String path, int width, int height) {
        try (InputStream in = Assets.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("Art not found (using placeholder): " + path);
                return null;
            }
            BufferedImage source = ImageIO.read(in);
            if (source == null) {
                System.err.println("Could not read image: " + path);
                return null;
            }
            // Scale first, THEN remove the magenta, so the blended edge pixels can be cleaned up.
            BufferedImage scaled = scaleDown(source, width, height);
            removeMagenta(scaled);
            return scaled;
        } catch (IOException e) {
            System.err.println("Could not load image " + path + ": " + e.getMessage());
            return null;
        }
    }

    /** Halves the image again and again until it is close to the target, then does the last step. */
    private static BufferedImage scaleDown(BufferedImage source, int width, int height) {
        BufferedImage current = source;
        int w = source.getWidth();
        int h = source.getHeight();
        while (w / 2 >= width && h / 2 >= height) {
            w /= 2;
            h /= 2;
            current = resize(current, w, h);
        }
        return resize(current, width, height);
    }

    private static BufferedImage resize(BufferedImage source, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    /**
     * Removes the magenta background, including the pink blur on the edges.
     *
     * An edge pixel is a MIX of the object colour and magenta. How magenta it is
     * can be guessed from min(red, blue) - green (pure magenta gives 255, a grey gives 0).
     * With that guess we make the pixel partly see-through and subtract the
     * magenta from its colour, so no pink halo is left.
     */
    private static void removeMagenta(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                double magenta = (Math.min(red, blue) - green) / 255.0;
                if (magenta < KEY_START) {
                    continue; // a normal pixel: leave it alone
                }
                if (magenta >= KEY_FULL) {
                    image.setRGB(x, y, 0); // pure background: fully transparent
                    continue;
                }
                // Edge pixel: remove the magenta share from the colour and from the opacity.
                double keep = 1.0 - magenta;
                int r = clamp((red - 255 * magenta) / keep);
                int g = clamp(green / keep);
                int b = clamp((blue - 255 * magenta) / keep);
                int a = (int) Math.round(alpha * keep);
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }
}
