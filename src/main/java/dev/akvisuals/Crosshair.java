package dev.akvisuals;

import net.minecraft.client.gui.DrawContext;

public final class Crosshair {
    public static void render(DrawContext c) {
        if (!Config.customCrosshair) return;
        int cx = c.getScaledWindowWidth() / 2;
        int cy = c.getScaledWindowHeight() / 2;
        int g = Config.crosshairGap;
        int s = Config.crosshairSize;
        int col = Config.crosshairColor;
        // four arms, 2px thick, with a gap around the centre
        c.fill(cx - g - s, cy - 1, cx - g, cy + 1, col);
        c.fill(cx + g, cy - 1, cx + g + s, cy + 1, col);
        c.fill(cx - 1, cy - g - s, cx + 1, cy - g, col);
        c.fill(cx - 1, cy + g, cx + 1, cy + g + s, col);
    }
}
