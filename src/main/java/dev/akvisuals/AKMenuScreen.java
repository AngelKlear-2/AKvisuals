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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class AKMenuScreen extends Screen {
    // palette
    private static final int ACCENT = 0xFFE5484D;
    private static final int ACCENT_DIM = 0x73E5484D;
    private static final int ACCENT_GLOW = 0x20E5484D;
    private static final int PANEL_TOP = 0xE00E0E13;
    private static final int PANEL_BOT = 0xD4050508;
    private static final int HEADER_TOP = 0xF01C1218;
    private static final int ROW = 0x2AFFFFFF;
    private static final int ROW_HOVER = 0x42FFFFFF;
    private static final int FIELD = 0x3DFFFFFF;
    private static final int TEXT = 0xFFF5F5F7;
    private static final int MUTED = 0xFF8D8D98;
    private static final int TRACK = 0x40FFFFFF;
    private static final int KNOB = 0xFFFFFFFF;
    private static final int PILL_OFF = 0x59FFFFFF;

    private static final int MIN_W = 280, MIN_H = 220;
    private static final int HEADER_H = 40, TABS_H = 30, PAD = 10;
    private static final int ROW_H = 26, SLIDER_H = 36, GAP = 4, R = 12;

    private static final String[] ASPECT = {"OFF", "4:3", "16:9", "CUSTOM", "21:9"};
    private static final int[] CROSS_COLORS = {0xFFE5484D, 0xFFFFFFFF, 0xFF4DD6FF, 0xFF7CFF6B};
    private static final String[] CROSS_COLOR_KEYS = {"akvisuals.color.red", "akvisuals.color.white", "akvisuals.color.cyan", "akvisuals.color.green"};

    private enum Tab { COMBAT, VISUALS, HUD, UTIL }

    private enum Kind { TOGGLE, SLIDER, CHOICE, TEXT, BUTTON }

    private static final class Item {
        final Tab tab;
        final String label;
        final Kind kind;
        BooleanSupplier on;
        Runnable flip;
        DoubleSupplier get;
        DoubleConsumer set;
        double min, max, step;
        int decimals;
        String[] options;
        IntSupplier index;
        IntConsumer setIndex;
        Supplier<String> text;
        Consumer<String> setText;
        Runnable action;
        BooleanSupplier show;

        Item(Tab tab, String label, Kind kind) {
            this.tab = tab;
            this.label = label;
            this.kind = kind;
        }
    }

    private record Hit(Item item, int option, int x, int y, int w, int h) {}

    private final List<Item> items = buildItems();
    private final Map<String, Float> anim = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();

    private int px, py, pw, ph;
    private Tab tab = Tab.VISUALS;
    private String search = "";
    private boolean searchFocus = false;
    private Item focusItem = null;
    private String editBuf = "";
    private Item openChoice = null;
    private Item dragSlider = null;
    private int dragX, dragW;
    private boolean moving = false, resizing = false;
    private int grabX, grabY;
    private int scroll = 0;
    private int contentHeight = 0;
    private float lineX = -1;

    public AKMenuScreen() {
        super(Text.literal("AKvisuals"));
    }

    // ---- items ----

    private static List<Item> buildItems() {
        List<Item> l = new ArrayList<>();

        // combat
        l.add(toggle(Tab.COMBAT, "akvisuals.killaura", () -> Config.killAura, () -> Config.killAura = !Config.killAura));
        l.add(slider(Tab.COMBAT, "akvisuals.range", 1.5, 6, 0.5, 1, () -> Config.auraRange, v -> Config.auraRange = v));
        l.add(slider(Tab.COMBAT, "akvisuals.speed", 5, 60, 5, 0, () -> Config.auraSpeed, v -> Config.auraSpeed = (float) v));
        l.add(toggle(Tab.COMBAT, "akvisuals.rotate", () -> Config.auraRotate, () -> Config.auraRotate = !Config.auraRotate));
        l.add(toggle(Tab.COMBAT, "akvisuals.players", () -> Config.auraPlayers, () -> Config.auraPlayers = !Config.auraPlayers));
        l.add(toggle(Tab.COMBAT, "akvisuals.mobs", () -> Config.auraMobs, () -> Config.auraMobs = !Config.auraMobs));
        l.add(toggle(Tab.COMBAT, "akvisuals.weapon.sword", () -> Config.auraSword, () -> Config.auraSword = !Config.auraSword));
        l.add(toggle(Tab.COMBAT, "akvisuals.weapon.axe", () -> Config.auraAxe, () -> Config.auraAxe = !Config.auraAxe));
        l.add(toggle(Tab.COMBAT, "akvisuals.weapon.mace", () -> Config.auraMace, () -> Config.auraMace = !Config.auraMace));
        l.add(toggle(Tab.COMBAT, "akvisuals.weapon.spear", () -> Config.auraSpear, () -> Config.auraSpear = !Config.auraSpear));
        l.add(toggle(Tab.COMBAT, "akvisuals.crystal", () -> Config.crystalAura, () -> Config.crystalAura = !Config.crystalAura));

        // visuals
        l.add(toggle(Tab.VISUALS, "akvisuals.chinahat", () -> Config.chinaHat, () -> Config.chinaHat = !Config.chinaHat));
        l.add(toggle(Tab.VISUALS, "akvisuals.hatanimate", () -> Config.hatAnimate, () -> Config.hatAnimate = !Config.hatAnimate));
        l.add(slider(Tab.VISUALS, "akvisuals.hatspeed", 0.5, 3, 0.5, 1, () -> Config.hatSpeed, v -> Config.hatSpeed = (float) v));
        l.add(choice(Tab.VISUALS, "akvisuals.aspect", ASPECT, () -> Config.stretchMode, i -> Config.stretchMode = i));
        Item ratio = text(Tab.VISUALS, "akvisuals.customratio",
                () -> Config.customW + ":" + Config.customH, AKMenuScreen::parseRatio);
        ratio.show = () -> Config.stretchMode == 3;
        l.add(ratio);
        l.add(toggle(Tab.VISUALS, "akvisuals.fullbright", () -> Config.fullBright, () -> Config.fullBright = !Config.fullBright));
        l.add(toggle(Tab.VISUALS, "akvisuals.particles", () -> Config.particles, () -> Config.particles = !Config.particles));

        // hud
        l.add(toggle(Tab.HUD, "akvisuals.crosshair", () -> Config.customCrosshair, () -> Config.customCrosshair = !Config.customCrosshair));
        l.add(slider(Tab.HUD, "akvisuals.crosshair.size", 2, 16, 1, 0, () -> Config.crosshairSize, v -> Config.crosshairSize = (int) v));
        l.add(slider(Tab.HUD, "akvisuals.crosshair.gap", 0, 12, 1, 0, () -> Config.crosshairGap, v -> Config.crosshairGap = (int) v));
        String[] colorNames = new String[CROSS_COLOR_KEYS.length];
        for (int i = 0; i < colorNames.length; i++) colorNames[i] = t(CROSS_COLOR_KEYS[i]);
        l.add(choice(Tab.HUD, "akvisuals.crosshair.color", colorNames,
                () -> indexOf(CROSS_COLORS, Config.crosshairColor), i -> Config.crosshairColor = CROSS_COLORS[i]));

        // utilities
        l.add(button(Tab.UTIL, "akvisuals.save", Config::save));
        l.add(button(Tab.UTIL, "akvisuals.reset", () -> {
            Config.menuX = -1;
            Config.menuY = -1;
            Config.menuW = 340;
            Config.menuH = 420;
        }));

        return l;
    }

    private static Item toggle(Tab tab, String label, BooleanSupplier on, Runnable flip) {
        Item it = new Item(tab, label, Kind.TOGGLE);
        it.on = on;
        it.flip = flip;
        return it;
    }

    private static Item slider(Tab tab, String label, double min, double max, double step, int decimals,
                               DoubleSupplier get, DoubleConsumer set) {
        Item it = new Item(tab, label, Kind.SLIDER);
        it.min = min;
        it.max = max;
        it.step = step;
        it.decimals = decimals;
        it.get = get;
        it.set = set;
        return it;
    }

    private static Item choice(Tab tab, String label, String[] options, IntSupplier index, IntConsumer setIndex) {
        Item it = new Item(tab, label, Kind.CHOICE);
        it.options = options;
        it.index = index;
        it.setIndex = setIndex;
        return it;
    }

    private static Item text(Tab tab, String label, Supplier<String> get, Consumer<String> set) {
        Item it = new Item(tab, label, Kind.TEXT);
        it.text = get;
        it.setText = set;
        return it;
    }

    private static Item button(Tab tab, String label, Runnable action) {
        Item it = new Item(tab, label, Kind.BUTTON);
        it.action = action;
        return it;
    }

    // "21:9" -> customW/customH; ignores anything that doesn't parse
    private static void parseRatio(String s) {
        String[] parts = s.split(":");
        if (parts.length != 2) return;
        try {
            int w = Integer.parseInt(parts[0].trim());
            int h = Integer.parseInt(parts[1].trim());
            if (w < 1 || h < 1 || w > 64 || h > 64) return;
            Config.customW = w;
            Config.customH = h;
        } catch (NumberFormatException ignored) {
        }
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

    private static String t(String key) {
        return Text.translatable(key).getString();
    }

    private List<Item> visibleItems() {
        String q = search.trim().toLowerCase(Locale.ROOT);
        List<Item> out = new ArrayList<>();
        for (Item it : items) {
            if (it.show != null && !it.show.getAsBoolean()) continue;
            if (q.isEmpty()) {
                if (it.tab == tab) out.add(it);
            } else if (t(it.label).toLowerCase(Locale.ROOT).contains(q)) {
                out.add(it);
            }
        }
        return out;
    }

    // ---- geometry ----

    private void updateGeom() {
        pw = Math.max(MIN_W, Math.min(Config.menuW, width));
        ph = Math.max(MIN_H, Math.min(Config.menuH, height));
        px = Config.menuX < 0 ? (width - pw) / 2 : Math.max(0, Math.min(Config.menuX, width - pw));
        py = Config.menuY < 0 ? Math.max(4, (height - ph) / 2) : Math.max(0, Math.min(Config.menuY, height - ph));
    }

    private int viewTop() {
        return py + HEADER_H + TABS_H;
    }

    private int viewBottom() {
        return py + ph - 2;
    }

    private int viewportH() {
        return viewBottom() - viewTop();
    }

    // lays out visible rows from the given scroll offset; returns total content height
    private int buildRows(int offset, List<Hit> out) {
        int top = viewTop() + PAD;
        int y = top - offset;
        int rx = px + PAD, rw = pw - PAD * 2;
        for (Item it : visibleItems()) {
            int h = it.kind == Kind.SLIDER ? SLIDER_H : ROW_H;
            out.add(new Hit(it, -1, rx, y, rw, h));
            y += h + GAP;
            if (it.kind == Kind.CHOICE && it == openChoice) {
                for (int o = 0; o < it.options.length; o++) {
                    out.add(new Hit(it, o, rx + 14, y, rw - 14, ROW_H));
                    y += ROW_H + 2;
                }
                y += GAP;
            }
        }
        return y + offset - top;
    }

    private void layoutRows() {
        hits.clear();
        int total = buildRows(0, new ArrayList<>());
        contentHeight = total;
        int max = Math.max(0, total - viewportH() + PAD);
        scroll = Math.max(0, Math.min(scroll, max));
        buildRows(scroll, hits);
    }

    private boolean inViewport(int y, int h) {
        return y + h > viewTop() && y < viewBottom();
    }

    // ---- drawing ----

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        // no darkening: the game stays visible behind the panel
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        updateGeom();
        layoutRows();

        // outer glow, border, body
        rrGrad(c, px - 2, py - 2, pw + 4, ph + 4, R + 2, ACCENT_GLOW, ACCENT_GLOW);
        rrGrad(c, px, py, pw, ph, R, ACCENT_DIM, ACCENT_DIM);
        rrGrad(c, px + 1, py + 1, pw - 2, ph - 2, R - 1, PANEL_TOP, PANEL_BOT);

        // header
        rrGrad(c, px + 1, py + 1, pw - 2, HEADER_H - 1, R - 1, HEADER_TOP, PANEL_TOP);
        rr(c, px + 14, py + 15, 8, 8, 4, ACCENT);
        c.drawTextWithShadow(textRenderer, "AKvisuals", px + 28, py + 14, TEXT);
        int vx = px + 28 + textRenderer.getWidth("AKvisuals") + 6;
        c.drawTextWithShadow(textRenderer, "v0.2.0", vx, py + 14, MUTED);

        int sx = px + pw - 136, sy = py + 11;
        rr(c, sx, sy, 120, 18, 9, searchFocus ? ACCENT_DIM : FIELD);
        String sText = search.isEmpty() && !searchFocus ? t("akvisuals.search") : search + (searchFocus ? "_" : "");
        c.drawTextWithShadow(textRenderer, sText, sx + 9, sy + 5, search.isEmpty() && !searchFocus ? MUTED : TEXT);

        // tabs with animated underline
        int tw = (pw - PAD * 2) / 4;
        int ty = py + HEADER_H;
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tb = Tab.values()[i];
            int tx = px + PAD + i * tw;
            boolean sel = tb == tab;
            boolean hover = inside(mouseX, mouseY, tx, ty, tw, TABS_H);
            if (hover && !sel) rr(c, tx + 4, ty + 4, tw - 8, TABS_H - 8, 7, ROW);
            String label = t(tabKey(tb));
            c.drawTextWithShadow(textRenderer, label, tx + (tw - textRenderer.getWidth(label)) / 2, ty + 11, sel ? TEXT : MUTED);
        }
        float target = px + PAD + indexOf(Tab.values(), tab) * tw;
        if (lineX < 0) lineX = target;
        lineX += (target - lineX) * 0.3f;
        rr(c, Math.round(lineX) + 14, ty + TABS_H - 3, tw - 28, 2, 1, ACCENT);

        // content
        c.enableScissor(px + 1, viewTop(), px + pw - 1, viewBottom());
        for (Hit h : hits) {
            if (!inViewport(h.y, h.h)) continue;
            drawHit(c, h, mouseX, mouseY);
        }
        c.disableScissor();

        // scrollbar
        int view = viewportH();
        if (contentHeight > view + PAD) {
            int thumb = Math.max(24, view * view / Math.max(1, contentHeight));
            int travel = view - thumb;
            int maxScroll = Math.max(1, contentHeight - view + PAD);
            int thumbY = viewTop() + (int) ((long) scroll * travel / maxScroll);
            rr(c, px + pw - 6, viewTop() + 4, 3, view - 8, 1, ROW);
            rr(c, px + pw - 6, thumbY, 3, thumb, 1, ACCENT_DIM);
        }

        // resize grip
        for (int i = 0; i < 3; i++) {
            rr(c, px + pw - 6 - i * 4, py + ph - 6 - i * 4, 2, 2, 1, MUTED);
        }

        // hot keys plate, bottom-right of the screen
        int hx = width - 150, hy = height - 96, hw = 136, hh = 72;
        rrGrad(c, hx, hy, hw, hh, R, ACCENT_DIM, ACCENT_DIM);
        rrGrad(c, hx + 1, hy + 1, hw - 2, hh - 2, R - 1, PANEL_TOP, PANEL_BOT);
        c.drawTextWithShadow(textRenderer, t("akvisuals.hotkeys"), hx + 12, hy + 12, TEXT);
        c.fill(hx + 12, hy + 26, hx + hw - 12, hy + 27, ROW);
        String key = AKvisuals.MENU_KEY.getBoundKeyLocalizedText().getString();
        c.drawTextWithShadow(textRenderer, t("akvisuals.menu"), hx + 12, hy + 38, MUTED);
        c.drawTextWithShadow(textRenderer, key, hx + hw - 12 - textRenderer.getWidth(key), hy + 38, ACCENT);
    }

    private void drawHit(DrawContext c, Hit h, int mx, int my) {
        Item it = h.item;
        boolean hover = inside(mx, my, h.x, h.y, h.w, h.h);

        if (h.option >= 0) {
            boolean sel = it.index.getAsInt() == h.option;
            rr(c, h.x, h.y, h.w, h.h, 7, sel ? ACCENT_DIM : (hover ? ROW_HOVER : ROW));
            c.drawTextWithShadow(textRenderer, optionLabel(it, h.option), h.x + 10, h.y + 9, sel ? TEXT : MUTED);
            return;
        }

        rr(c, h.x, h.y, h.w, h.h, 8, hover ? ROW_HOVER : ROW);
        int cy = h.y + (ROW_H - 8) / 2;
        String label = t(it.label);

        switch (it.kind) {
            case TOGGLE -> {
                c.drawTextWithShadow(textRenderer, label, h.x + 10, cy, TEXT);
                drawPill(c, it, h.x + h.w - 36, h.y + (ROW_H - 12) / 2);
            }
            case CHOICE -> {
                c.drawTextWithShadow(textRenderer, label, h.x + 10, cy, TEXT);
                String v = optionLabel(it, it.index.getAsInt());
                String sign = it == openChoice ? "-" : "+";
                int vw = textRenderer.getWidth(v);
                c.drawTextWithShadow(textRenderer, sign, h.x + h.w - 12, cy, MUTED);
                c.drawTextWithShadow(textRenderer, v, h.x + h.w - 22 - vw, cy, ACCENT);
            }
            case TEXT -> {
                c.drawTextWithShadow(textRenderer, label, h.x + 10, cy, TEXT);
                boolean foc = focusItem == it;
                String v = foc ? editBuf + "_" : it.text.get();
                int bw = 72;
                int bx = h.x + h.w - bw - 6;
                rr(c, bx, h.y + 4, bw, ROW_H - 8, 6, foc ? ACCENT_DIM : FIELD);
                c.drawTextWithShadow(textRenderer, v, bx + 7, cy, TEXT);
            }
            case BUTTON -> {
                int lw = textRenderer.getWidth(label);
                c.drawTextWithShadow(textRenderer, label, h.x + (h.w - lw) / 2, cy, ACCENT);
            }
            case SLIDER -> {
                double v = it.get.getAsDouble();
                String vs = String.format(Locale.ROOT, "%." + it.decimals + "f", v);
                c.drawTextWithShadow(textRenderer, label, h.x + 10, h.y + 6, TEXT);
                c.drawTextWithShadow(textRenderer, vs, h.x + h.w - 10 - textRenderer.getWidth(vs), h.y + 6, ACCENT);
                int trackX = h.x + 10, trackW = h.w - 20, trackY = h.y + SLIDER_H - 11;
                float frac = (float) ((v - it.min) / (it.max - it.min));
                int fill = Math.round(Math.max(0, Math.min(1, frac)) * trackW);
                rr(c, trackX, trackY, trackW, 4, 2, TRACK);
                rr(c, trackX, trackY, Math.max(4, fill), 4, 2, ACCENT);
                rr(c, trackX + fill - 5, trackY - 3, 10, 10, 5, KNOB);
            }
        }
    }

    private static String optionLabel(Item it, int i) {
        if (it.label.equals("akvisuals.crosshair.color")) return it.options[i];
        return it.options[i];
    }

    private void drawPill(DrawContext c, Item it, int x, int y) {
        float target = it.on.getAsBoolean() ? 1f : 0f;
        float cur = anim.getOrDefault(it.label, target);
        cur += (target - cur) * 0.25f;
        anim.put(it.label, cur);
        rr(c, x, y, 26, 12, 6, mix(PILL_OFF, ACCENT, cur));
        int kx = x + 2 + Math.round(cur * 14);
        rr(c, kx, y + 2, 8, 8, 4, KNOB);
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        updateGeom();
        layoutRows();
        double mx = click.x(), my = click.y();

        if (inside(mx, my, px + pw - 14, py + ph - 14, 14, 14)) {
            resizing = true;
            return true;
        }
        if (inside(mx, my, px + pw - 136, py + 11, 120, 18)) {
            searchFocus = true;
            focusItem = null;
            return true;
        }
        if (inside(mx, my, px, py, pw, HEADER_H)) {
            moving = true;
            grabX = (int) (mx - px);
            grabY = (int) (my - py);
            return true;
        }
        commitText();
        searchFocus = false;

        int tw = (pw - PAD * 2) / 4;
        for (int i = 0; i < Tab.values().length; i++) {
            int tx = px + PAD + i * tw;
            if (inside(mx, my, tx, py + HEADER_H, tw, TABS_H)) {
                tab = Tab.values()[i];
                scroll = 0;
                openChoice = null;
                return true;
            }
        }

        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!inViewport(h.y, h.h) || !inside(mx, my, h.x, h.y, h.w, h.h)) continue;
            Item it = h.item;
            if (h.option >= 0) {
                it.setIndex.accept(h.option);
                openChoice = null;
                Config.save();
                return true;
            }
            switch (it.kind) {
                case TOGGLE -> {
                    it.flip.run();
                    Config.save();
                }
                case CHOICE -> openChoice = it == openChoice ? null : it;
                case TEXT -> {
                    focusItem = it;
                    editBuf = it.text.get();
                }
                case BUTTON -> {
                    it.action.run();
                    Config.save();
                }
                case SLIDER -> {
                    dragSlider = it;
                    dragX = h.x;
                    dragW = h.w;
                    setSliderFromX(it, h.x, h.w, mx);
                }
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        double mx = click.x(), my = click.y();
        if (moving) {
            Config.menuX = (int) Math.max(0, Math.min(mx - grabX, width - pw));
            Config.menuY = (int) Math.max(0, Math.min(my - grabY, height - ph));
            return true;
        }
        if (resizing) {
            Config.menuW = (int) Math.max(MIN_W, Math.min(mx - px, width - px));
            Config.menuH = (int) Math.max(MIN_H, Math.min(my - py, height - py));
            return true;
        }
        if (dragSlider != null) {
            setSliderFromX(dragSlider, dragX, dragW, mx);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (moving || resizing || dragSlider != null) {
            moving = false;
            resizing = false;
            dragSlider = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (inside(mx, my, px, viewTop(), pw, viewportH())) {
            scroll -= (int) (vertical * 14);
            return true;
        }
        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    private void setSliderFromX(Item it, int x, int w, double mx) {
        double t = (mx - (x + 10)) / Math.max(1, w - 20);
        t = Math.max(0, Math.min(1, t));
        double v = it.min + t * (it.max - it.min);
        v = Math.round(v / it.step) * it.step;
        v = Math.max(it.min, Math.min(it.max, v));
        it.set.accept(v);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int k = input.key();
        if (focusItem != null) {
            if (k == GLFW.GLFW_KEY_ESCAPE) {
                focusItem = null;
            } else if (k == GLFW.GLFW_KEY_ENTER || k == GLFW.GLFW_KEY_KP_ENTER) {
                commitText();
            } else if (k == GLFW.GLFW_KEY_BACKSPACE && !editBuf.isEmpty()) {
                editBuf = editBuf.substring(0, editBuf.length() - 1);
            }
            return true;
        }
        if (searchFocus) {
            if (k == GLFW.GLFW_KEY_ESCAPE || k == GLFW.GLFW_KEY_ENTER) {
                searchFocus = false;
            } else if (k == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        int cp = input.codepoint();
        if (focusItem != null) {
            if (cp >= 32 && editBuf.length() < 12) editBuf += Character.toString(cp);
            return true;
        }
        if (searchFocus) {
            if (cp >= 32 && search.length() < 24) search += Character.toString(cp);
            return true;
        }
        return super.charTyped(input);
    }

    private void commitText() {
        if (focusItem == null) return;
        focusItem.setText.accept(editBuf);
        focusItem = null;
        Config.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        commitText();
        Config.save();
        super.close();
    }

    // ---- helpers ----

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    static void rr(DrawContext c, int x, int y, int w, int h, int r, int col) {
        rrGrad(c, x, y, w, h, r, col, col);
    }

    // rounded rectangle as 1px rows with per-row inset, colour blended top to bottom
    static void rrGrad(DrawContext c, int x, int y, int w, int h, int r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int dy = 0; dy < h; dy++) {
            double yc = dy + 0.5;
            double dc = yc < r ? r - yc : yc > h - r ? yc - (h - r) : 0;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0, r * r - dc * dc)));
            int col = top == bottom ? top : mix(top, bottom, dy / (float) h);
            c.fill(x + inset, y + dy, x + w - inset, y + dy + 1, col);
        }
    }

    private static int indexOf(Tab[] arr, Tab v) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == v) return i;
        return 0;
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
