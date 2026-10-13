package dev.akvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    public static boolean killAura = false;
    public static double auraRange = 4.2;
    public static boolean auraRotate = true;
    public static boolean auraPlayers = true;
    public static boolean auraMobs = true;

    public static boolean chinaHat = false;
    public static boolean hatAnimate = true;
    public static float hatSpeed = 1.0f;

    public static int stretchMode = 0; // 0 off, 1 4:3, 2 16:9, 3 custom
    public static int customW = 21;
    public static int customH = 9;

    public static boolean fullBright = false;
    public static boolean particles = true;

    public static boolean customCrosshair = false;
    public static int crosshairSize = 6;
    public static int crosshairGap = 3;
    public static int crosshairColor = 0xFFE5484D;

    // kill aura: which held weapons it attacks with, smooth turn speed (deg/tick), crystal combo
    public static boolean auraSword = true;
    public static boolean auraAxe = true;
    public static boolean auraMace = true;
    public static boolean auraSpear = true;
    public static float auraSpeed = 25f;
    public static boolean crystalAura = false;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("akvisuals.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                GSON.fromJson(Files.readString(FILE), Snap.class).apply();
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new Snap()));
        } catch (Exception ignored) {
        }
    }

    static final class Snap {
        boolean killAura = Config.killAura;
        double auraRange = Config.auraRange;
        boolean auraRotate = Config.auraRotate;
        boolean auraPlayers = Config.auraPlayers;
        boolean auraMobs = Config.auraMobs;
        boolean chinaHat = Config.chinaHat;
        boolean hatAnimate = Config.hatAnimate;
        float hatSpeed = Config.hatSpeed;
        int stretchMode = Config.stretchMode;
        int customW = Config.customW;
        int customH = Config.customH;
        boolean fullBright = Config.fullBright;
        boolean particles = Config.particles;
        boolean customCrosshair = Config.customCrosshair;
        int crosshairSize = Config.crosshairSize;
        int crosshairGap = Config.crosshairGap;
        int crosshairColor = Config.crosshairColor;
        boolean auraSword = Config.auraSword;
        boolean auraAxe = Config.auraAxe;
        boolean auraMace = Config.auraMace;
        boolean auraSpear = Config.auraSpear;
        float auraSpeed = Config.auraSpeed;
        boolean crystalAura = Config.crystalAura;

        void apply() {
            Config.killAura = killAura;
            Config.auraRange = auraRange;
            Config.auraRotate = auraRotate;
            Config.auraPlayers = auraPlayers;
            Config.auraMobs = auraMobs;
            Config.chinaHat = chinaHat;
            Config.hatAnimate = hatAnimate;
            Config.hatSpeed = hatSpeed;
            Config.stretchMode = stretchMode;
            Config.customW = customW;
            Config.customH = customH;
            Config.fullBright = fullBright;
            Config.particles = particles;
            Config.customCrosshair = customCrosshair;
            Config.crosshairSize = crosshairSize;
            Config.crosshairGap = crosshairGap;
            Config.crosshairColor = crosshairColor;
            Config.auraSword = auraSword;
            Config.auraAxe = auraAxe;
            Config.auraMace = auraMace;
            Config.auraSpear = auraSpear;
            Config.auraSpeed = auraSpeed;
            Config.crystalAura = crystalAura;
        }
    }
}
