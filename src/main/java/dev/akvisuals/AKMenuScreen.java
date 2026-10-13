package dev.akvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public final class AKMenuScreen extends Screen {
    private static final int BG_TOP = 0xFF0B0F1E;
    private static final int BG_BOT = 0xFF1C0F2E;
    private static final int ACCENT = 0xFFFF4D8D;

    private static final String[] TABS = {"COMBAT", "VISUALS", "CONFIG"};
    private static final String[] STRETCH = {"OFF", "4:3", "16:9", "CUSTOM"};
    private int tab = 0;

    public AKMenuScreen() {
        super(Text.literal("AKvisuals"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int tabW = 90;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(TABS[i]), b -> {
                tab = idx;
                clearAndInit();
            }).dimensions(cx - (tabW * 3 + 8) / 2 + i * (tabW + 4), 30, tabW, 20).build());
        }

        int x = cx - 110, y = 60, w = 220, step = 24;
        switch (tab) {
            case 0 -> {
                row(x, y, w, () -> "Kill Aura: " + onOff(Config.killAura), () -> Config.killAura = !Config.killAura);
                y += step;
                row(x, y, w, () -> "Range: " + String.format("%.1f", Config.auraRange),
                        () -> Config.auraRange = Config.auraRange >= 6 ? 3 : Config.auraRange + 0.5);
                y += step;
                row(x, y, w, () -> "Rotate: " + onOff(Config.auraRotate), () -> Config.auraRotate = !Config.auraRotate);
                y += step;
                row(x, y, w, () -> "Players: " + onOff(Config.auraPlayers), () -> Config.auraPlayers = !Config.auraPlayers);
                y += step;
                row(x, y, w, () -> "Mobs: " + onOff(Config.auraMobs), () -> Config.auraMobs = !Config.auraMobs);
            }
            case 1 -> {
                row(x, y, w, () -> "China Hat: " + onOff(Config.chinaHat), () -> Config.chinaHat = !Config.chinaHat);
                y += step;
                row(x, y, w, () -> "Hat Animate: " + onOff(Config.hatAnimate), () -> Config.hatAnimate = !Config.hatAnimate);
                y += step;
                row(x, y, w, () -> "Hat Speed: " + String.format("%.1f", Config.hatSpeed),
                        () -> Config.hatSpeed = Config.hatSpeed >= 3 ? 0.5f : Config.hatSpeed + 0.5f);
                y += step;
                row(x, y, w, () -> "Stretch: " + STRETCH[Config.stretchMode],
                        () -> Config.stretchMode = (Config.stretchMode + 1) % 4);
                y += step;
                row(x, y, w, () -> "Custom W: " + Config.customW,
                        () -> Config.customW = Config.customW >= 32 ? 1 : Config.customW + 1);
                y += step;
                row(x, y, w, () -> "Custom H: " + Config.customH,
                        () -> Config.customH = Config.customH >= 32 ? 1 : Config.customH + 1);
            }
            default -> {
                row(x, y, w, () -> "Save Config", () -> {});
                y += step;
                addDrawableChild(ButtonWidget.builder(Text.literal("Change menu key: Options > Controls"), b -> {})
                        .dimensions(x, y, w, 20).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(cx - 50, height - 34, 100, 20).build());
    }

    private void row(int x, int y, int w, Supplier<String> label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label.get()), b -> {
            action.run();
            Config.save();
            clearAndInit();
        }).dimensions(x, y, w, 20).build());
    }

    private static String onOff(boolean b) {
        return b ? "ON" : "OFF";
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fillGradient(0, 0, width, height, BG_TOP, BG_BOT);
        long t = System.currentTimeMillis();
        for (int i = 0; i < 80; i++) {
            int x = (int) ((i * 197L + t / 40 * (1 + i % 3)) % Math.max(1, width));
            int y = (int) ((i * 113L + t / 90) % Math.max(1, height));
            c.fill(x, y, x + 2, y + 2, 0xAAFF4D8D);
        }
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        super.render(c, mouseX, mouseY, delta);
        c.drawCenteredTextWithShadow(textRenderer, "AKvisuals", width / 2, 12, ACCENT);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        Config.save();
        super.close();
    }
}
