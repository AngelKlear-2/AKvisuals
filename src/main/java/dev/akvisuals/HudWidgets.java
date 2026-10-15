package dev.akvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

// effects + cooldowns panels. drawn by the HUD element while the menu is closed,
// and drawn by the menu (draggable) while it is open
public final class HudWidgets {
    public static final String FX = "effects";
    public static final String CD = "cooldowns";

    static final int W = 190;
    static final int ROW = 16;
    static final int PAD = 10;
    static final int HEAD = 16;
    static final int MARGIN = 12;

    private static final int BG = 0xD80C0C11;
    private static final int BORDER = 0x4DFFFFFF;
    private static final int EDIT = 0xFFE5484D;
    private static final int TEXT = 0xFFF5F5F7;
    private static final int MUTED = 0xFF8D8D98;
    private static final int ACCENT = 0xFFE5484D;

    private HudWidgets() {}

    public static void render(DrawContext c, boolean editing) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        if (Config.fullBright && !editing) {
            c.fill(0, 0, c.getScaledWindowWidth(), c.getScaledWindowHeight(), 0x1EFFFFFF);
        }
        if (Config.hudEffects) drawPanel(c, mc, FX, "Effects", rows(FX), editing);
        if (Config.hudCooldowns) drawPanel(c, mc, CD, "Cooldowns", rows(CD), editing);
    }

    // rows as {left, right}
    public static List<String[]> rows(String key) {
        List<String[]> out = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null) return out;
        if (FX.equals(key)) {
            for (StatusEffectInstance e : p.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString();
                String lvl = e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "";
                out.add(new String[]{name + lvl, time(e.getDuration())});
            }
        } else if (CD.equals(key)) {
            out.add(cooldownRow(p, "Ender Pearl", Items.ENDER_PEARL));
            out.add(cooldownRow(p, "Golden Apple", Items.GOLDEN_APPLE));
        }
        return out;
    }

    private static String[] cooldownRow(ClientPlayerEntity p, String name, net.minecraft.item.Item item) {
        float progress = p.getItemCooldownManager().getCooldownProgress(item.getDefaultStack(), 0f);
        return new String[]{name, progress > 0 ? Math.round(progress * 100) + "%" : "ready"};
    }

    private static String time(int ticks) {
        int s = Math.max(0, ticks / 20);
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    // x/y resolved against the current window size; default is the top-right corner, stacked
    public static int[] rect(String key) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int rows = Math.max(1, rows(key).size());
        int h = PAD * 2 + HEAD + rows * ROW;
        int x, y;
        if (FX.equals(key)) {
            x = Config.hudEffX < 0 ? sw - W - MARGIN : Config.hudEffX;
            y = Config.hudEffY < 0 ? MARGIN : Config.hudEffY;
        } else {
            x = Config.hudCdX < 0 ? sw - W - MARGIN : Config.hudCdX;
            y = Config.hudCdY < 0 ? MARGIN + 120 : Config.hudCdY;
        }
        return new int[]{x, y, W, h};
    }

    private static void drawPanel(DrawContext c, MinecraftClient mc, String key, String title, List<String[]> rows, boolean editing) {
        int[] r = rect(key);
        int x = r[0], y = r[1], w = r[2], h = r[3];
        AKMenuScreen.rr(c, x, y, w, h, 12, BG);
        AKMenuScreen.rr(c, x, y, w, 1, 0, BORDER);
        c.drawTextWithShadow(mc.textRenderer, title, x + PAD, y + PAD, TEXT);
        if (rows.isEmpty()) {
            c.drawTextWithShadow(mc.textRenderer, "none", x + PAD, y + PAD + HEAD, MUTED);
        }
        int ry = y + PAD + HEAD;
        for (String[] row : rows) {
            c.drawTextWithShadow(mc.textRenderer, row[0], x + PAD, ry, TEXT);
            c.drawTextWithShadow(mc.textRenderer, row[1], x + w - PAD - mc.textRenderer.getWidth(row[1]), ry, MUTED);
            ry += ROW;
        }
        if (editing) {
            c.fill(x, y, x + w, y + 1, ACCENT);
            c.fill(x, y + h - 1, x + w, y + h, ACCENT);
            c.fill(x, y, x + 1, y + h, ACCENT);
            c.fill(x + w - 1, y, x + w, y + h, ACCENT);
        }
    }
}
