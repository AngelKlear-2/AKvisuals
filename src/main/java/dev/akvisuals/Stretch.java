package dev.akvisuals;

public final class Stretch {
    // returns X-axis scale for the projection matrix; 1.0 = no change
    public static float factor(int winW, int winH) {
        float target = switch (Config.stretchMode) {
            case 1 -> 4f / 3f;
            case 2 -> 16f / 9f;
            case 3 -> (float) Config.customW / Math.max(1, Config.customH);
            case 4 -> 21f / 9f;
            default -> -1f;
        };
        if (target <= 0) return 1f;
        float actual = (float) winW / Math.max(1, winH);
        return target / actual;
    }
}
