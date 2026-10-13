package dev.akvisuals;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class AKMenuScreen extends Screen {
    // palette: translucent panel over the game, red accent
    private static final int PANEL = 0xC00A0A0E;
    private static final int RED = 0xFFE5484D;
    private static final int RED_SOFT = 0x66E5484D;
    private static final int ROW = 0x26FFFFFF;
    private static final int ROW_HOVER = 0x40FFFFFF;
    private static final int TEXT = 0xFFF2F2F5;
    private static final int MUTED = 0xFF8E8E98;
    private static final int PILL_OFF = 0x59FFFFFF;
    private static final int KNOB = 0xFFFFFFFF;

    private static final int PANEL_W = 300;
    private static final int PANEL_Y = 30;
    private static final int ROW_H = 26;
    private static final int GAP = 4;
    private static final int RADIUS = 10;

    private static final String[] STRETCH = {"OFF", "4:3", "16:9", "CUSTOM"};
    private static final int[] CROSS_COLORS = {0xFFE5484D, 0xFFFFFFFF, 0xFF4DD6FF, 0xFF7CFF6B};

    private enum Tab { COMBAT, VISUALS, HUD, UTIL }

    private enum Kind { TOGGLE, VALUE, BUTTON }

    private record Item(Tab tab, String label, Kind kind, Supplier<Boolean> on, Supplier<String> value, Runnable action) {}

    private final List<Item> items = buildItems();
    private final Map<String, Float> anim = new HashMap<>();
    private Tab tab = Tab.VISUALS;
    private String search = "";
    private boolean searchFocused = false;

    public AKMenuScreen() {
        super(Text.literal("AKvisuals"));
    }

    // ---- items ----

    private static List<Item> buildItems() {
        List<Item> l = new ArrayList<>();

        // combat
        addToggle(l, Tab.COMBAT, "akvisuals.killaura", () -> Config.killAura, () -> Config.killAura = !Config.killAura);
        addValue(l, Tab.COMBAT, "akvisuals.range", () -> String.format(Locale.ROOT, "%.1f", Config.auraRange),
                () -> Config.auraRange = Config.auraRange >= 6 ? 3 : Config.auraRange + 0.5);
        addToggle(l, Tab.COMBAT, "akvisuals.rotate", () -> Config.auraRotate, () -> Config.auraRotate = !Config.auraRotate);
        addToggle(l, Tab.COMBAT, "akvisuals.players", () -> Config.auraPlayers, () -> Config.auraPlayers = !Config.auraPlayers);
        addToggle(l, Tab.COMBAT, "akvisuals.mobs", () -> Config.auraMobs, () -> Config.auraMobs = !Config.auraMobs);
        addToggle(l, Tab.COMBAT, "akvisuals.weapon.sword", () -> Config.auraSword, () -> Config.auraSword = !Config.auraSword);
        addToggle(l, Tab.COMBAT, "akvisuals.weapon.axe", () -> Config.auraAxe, () -> Config.auraAxe = !Config.auraAxe);
        addToggle(l, Tab.COMBAT, "akvisuals.weapon.mace", () -> Config.auraMace, () -> Config.auraMace = !Config.auraMace);
        addToggle(l, Tab.COMBAT, "akvisuals.weapon.spear", () -> Config.auraSpear, () -> Config.auraSpear = !Config.auraSpear);
        addValue(l, Tab.COMBAT, "akvisuals.speed", () -> String.format(Locale.ROOT, "%.0f", Config.auraSpeed),
                () -> Config.auraSpeed = Config.auraSpeed >= 50 ? 10 : Config.auraSpeed + 5);
        addToggle(l, Tab.COMBAT, "akvisuals.crystal", () -> Config.crystalAura, () -> Config.crystalAura = !Config.crystalAura);

        // visuals
        addToggle(l, Tab.VISUALS, "akvisuals.chinahat", () -> Config.chinaHat, () -> Config.chinaHat = !Config.chinaHat);
        addToggle(l, Tab.VISUALS, "akvisuals.hatanimate", () -> Config.hatAnimate, () -> Config.hatAnimate = !Config.hatAnimate);
        addValue(l, Tab.VISUALS, "akvisuals.hatspeed", () -> String.format(Locale.ROOT, "%.1f", Config.hatSpeed),
                () -> Config.hatSpeed = Config.hatSpeed >= 3 ? 0.5f : Config.hatSpeed + 0.5f);
        addValue(l, Tab.VISUALS, "akvisuals.aspect", () -> STRETCH[Config.stretchMode],
                () -> Config.stretchMode = (Config.stretchMode + 1) % STRETCH.length);
        addValue(l, Tab.VISUALS, "akvisuals.customw", () -> String.valueOf(Config.customW),
                () -> Config.customW = Config.customW >= 32 ? 1 : Config.customW + 1);
        addValue(l, Tab.VISUALS, "akvisuals.customh", () -> String.valueOf(Config.customH),
                () -> Config.customH = Config.customH >= 32 ? 1 : Config.customH + 1);
        addToggle(l, Tab.VISUALS, "akvisuals.fullbright", () -> Config.fullBright, () -> Config.fullBright = !Config.fullBright);
        addToggle(l, Tab.VISUALS, "akvisuals.particles", () -> Config.particles, () -> Config.particles = !Config.particles);

        // hud
        addToggle(l, Tab.HUD, "akvisuals.crosshair", () -> Config.customCrosshair, () -> Config.customCrosshair = !Config.customCrosshair);
        addValue(l, Tab.HUD, "akvisuals.crosshair.size", () -> String.valueOf(Config.crosshairSize),
                () -> Config.crosshairSize = Config.crosshairSize >= 14 ? 2 : Config.crosshairSize + 1);
        addValue(l, Tab.HUD, "akvisuals.crosshair.gap", () -> String.valueOf(Config.crosshairGap),
                () -> Config.crosshairGap = Config.crosshairGap >= 10 ? 0 : Config.crosshairGap + 1);
        addValue(l, Tab.HUD, "akvisuals.crosshair.color", () -> "#" + String.format(Locale.ROOT, "%06X", Config.crosshairColor & 0xFFFFFF),
                () -> Config.crosshairColor = CROSS_COLORS[(indexOf(CROSS_COLORS, Config.crosshairColor) + 1) % CROSS_COLORS.length]);

        // utilities
        l.add(new Item(Tab.UTIL, "akvisuals.save", Kind.BUTTON, null, null, Config::save));

        return l;
    }

    private static void addToggle(List<Item> l, Tab tab, String label, Supplier<Boolean> on, Runnable flip) {
        l.add(new Item(tab, label, Kind.TOGGLE, on, null, flip));
    }

    private static void addValue(List<Item> l, Tab tab, String label, Supplier<String> value, Runnable cycle) {
        l.add(new Item(tab, label, Kind.VALUE, null, value, cycle));
    }

    private static int indexOf(int[] arr, int v) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == v) return i;
        return 0;
    }

    private static String tabKey(Tab t) {
        return switch (t) {
            case COMBAT -> "akvisuals.tab.combat";
            case VISUALS -> "akvisuals.tab.visuals";
            case HUD -> "akvisuals.tab.hud";
            case UTIL -> "akvisuals.tab.utilities";
        };
    }

    private List<Item> visibleItems() {
        String q = search.trim().toLowerCase(Locale.ROOT);
        List<Item> out = new ArrayList<>();
        for (Item it : items) {
            if (q.isEmpty()) {
                if (it.tab() == tab) out.add(it);
            } else if (t(it.label()).toLowerCase(Locale.ROOT).contains(q)) {
                out.add(it);
            }
        }
        return out;
    }

    private static String t(String key) {
        return Text.translatable(key).getString();
    }

    // ---- drawing ----

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        // no darkening: the game stays visible behind the panel
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        int px = (width - PANEL_W) / 2;
        int py = PANEL_Y;
        List<Item> visible = visibleItems();
        int panelH = 74 + visible.size() * (ROW_H + GAP) + 6;

        rr(c, px, py, PANEL_W, panelH, RADIUS, PANEL);
        c.drawTextWithShadow(textRenderer, "AKvisuals", px + 14, py + 12, RED);

        int sx = px + PANEL_W - 128, sy = py + 9;
        rr(c, sx, sy, 114, 16, 8, searchFocused ? RED_SOFT : ROW);
        c.drawTextWithShadow(textRenderer, search.isEmpty() ? t("akvisuals.search") : search,
                sx + 8, sy + 4, search.isEmpty() ? MUTED : TEXT);

        int tw = tabWidth();
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tb = Tab.values()[i];
            int tx = px + 12 + i * (tw + 4), ty = py + 34;
            boolean sel = tb == tab;
            rr(c, tx, ty, tw, 22, 11, sel ? RED_SOFT : ROW);
            String label = t(tabKey(tb));
            c.drawTextWithShadow(textRenderer, label, tx + (tw - textRenderer.getWidth(label)) / 2, ty + 7, sel ? TEXT : MUTED);
        }

        int ry = py + 64;
        for (Item it : visible) {
            int rx = px + 10, rw = PANEL_W - 20;
            boolean hover = inside(mouseX, mouseY, rx, ry, rw, ROW_H);
            rr(c, rx, ry, rw, ROW_H, 7, hover ? ROW_HOVER : ROW);
            c.drawTextWithShadow(textRenderer, t(it.label()), rx + 10, ry + (ROW_H - 8) / 2, TEXT);
            if (it.kind() == Kind.TOGGLE) {
                drawPill(c, it, rx + rw - 38, ry + (ROW_H - 12) / 2);
            } else if (it.kind() == Kind.VALUE) {
                String v = it.value().get();
                c.drawTextWithShadow(textRenderer, v, rx + rw - 10 - textRenderer.getWidth(v), ry + (ROW_H - 8) / 2, RED);
            }
            ry += ROW_H + GAP;
        }

        // hot keys plate, bottom-right
        int hx = width - 150, hy = height - 96, hw = 136, hh = 72;
        rr(c, hx, hy, hw, hh, RADIUS, PANEL);
        c.drawTextWithShadow(textRenderer, t("akvisuals.hotkeys"), hx + 10, hy + 10, TEXT);
        c.fill(hx + 10, hy + 24, hx + hw - 10, hy + 25, RED_SOFT);
        String key = AKvisuals.MENU_KEY.getBoundKeyLocalizedText().getString();
        c.drawTextWithShadow(textRenderer, t("akvisuals.menu"), hx + 10, hy + 34, MUTED);
        c.drawTextWithShadow(textRenderer, key, hx + hw - 10 - textRenderer.getWidth(key), hy + 34, RED);
    }

    private int tabWidth() {
        return (PANEL_W - 24 - 3 * 4) / 4;
    }

    private void drawPill(DrawContext c, Item it, int x, int y) {
        float target = it.on().get() ? 1f : 0f;
        float cur = anim.getOrDefault(it.label(), target);
        cur += (target - cur) * 0.25f;
        anim.put(it.label(), cur);
        rr(c, x, y, 26, 12, 6, mix(PILL_OFF, RED, cur));
        int kx = x + 2 + Math.round(cur * 14);
        rr(c, kx, y + 2, 8, 8, 4, KNOB);
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();
        int px = (width - PANEL_W) / 2;
        int py = PANEL_Y;

        if (inside(mx, my, px + PANEL_W - 128, py + 9, 114, 16)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int tw = tabWidth();
        for (int i = 0; i < Tab.values().length; i++) {
            int tx = px + 12 + i * (tw + 4), ty = py + 34;
            if (inside(mx, my, tx, ty, tw, 22)) {
                tab = Tab.values()[i];
                return true;
            }
        }

        int ry = py + 64;
        for (Item it : visibleItems()) {
            if (inside(mx, my, px + 10, ry, PANEL_W - 20, ROW_H)) {
                it.action().run();
                Config.save();
                return true;
            }
            ry += ROW_H + GAP;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (searchFocused && input.key() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.codepoint() >= 32 && search.length() < 24) {
            search += Character.toString(input.codepoint());
            return true;
        }
        return super.charTyped(input);
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

    // ---- helpers ----

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // rounded rectangle drawn as 1px rows with per-row horizontal inset
    static void rr(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int dy = 0; dy < h; dy++) {
            double yc = dy + 0.5;
            double dc = yc < r ? r - yc : yc > h - r ? yc - (h - r) : 0;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0, r * r - dc * dc)));
            c.fill(x + inset, y + dy, x + w - inset, y + dy + 1, col);
        }
    }

    private static int mix(int a, int b, float t) {
        int aa = ch(a, 24) + Math.round((ch(b, 24) - ch(a, 24)) * t);
        int rr = ch(a, 16) + Math.round((ch(b, 16) - ch(a, 16)) * t);
        int gg = ch(a, 8) + Math.round((ch(b, 8) - ch(a, 8)) * t);
        int bb = ch(a, 0) + Math.round((ch(b, 0) - ch(a, 0)) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }

    private static int ch(int argb, int shift) {
        return (argb >>> shift) & 0xFF;
    }
}
