package atingdagat.economy;

/**
 * FishKind.java
 *
 * What makes each fish different: how fast it swims, how deep it lives, how common it is,
 * how many are in a school, and the size of its picture. The value of the fish
 * (hold time, meals) lives in ResourceType, so the numbers are not repeated.
 *
 * Depth is the distance from the shore in tiles: 1 = right by the beach, 6 = open sea.
 */
public enum FishKind {

    //          resource                   speed  minDepth maxDepth weight size  w   h
    TAMBAN     (ResourceType.TAMBAN,       0.50,  1, 3, 5.0, 4, 10, 5),
    GALUNGGONG (ResourceType.GALUNGGONG,   0.45,  2, 4, 4.0, 3, 12, 6),
    BANGUS     (ResourceType.BANGUS,       0.40,  1, 3, 3.0, 3, 14, 6),
    TUNA       (ResourceType.TUNA,         0.95,  5, 6, 1.6, 2, 20, 8),
    LAPULAPU   (ResourceType.LAPULAPU,     0.30,  4, 5, 1.0, 2, 20, 10);

    private final ResourceType resource;
    private final double speed;
    private final int minDepth;
    private final int maxDepth;
    private final double weight;
    private final int schoolSize;
    private final int spriteW;
    private final int spriteH;

    FishKind(ResourceType resource, double speed, int minDepth, int maxDepth, double weight,
             int schoolSize, int spriteW, int spriteH) {
        this.resource = resource;
        this.speed = speed;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.weight = weight;
        this.schoolSize = schoolSize;
        this.spriteW = spriteW;
        this.spriteH = spriteH;
    }

    public ResourceType getResource() {
        return resource;
    }

    public double getSpeed() {
        return speed;
    }

    public int getMinDepth() {
        return minDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    /** Bigger = more common. */
    public double getWeight() {
        return weight;
    }

    /** How many catches one school holds. */
    public int getSchoolSize() {
        return schoolSize;
    }

    public int getSpriteW() {
        return spriteW;
    }

    public int getSpriteH() {
        return spriteH;
    }
}
