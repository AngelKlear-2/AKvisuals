package dev.akvisuals;

import net.minecraft.client.MinecraftClient;

// full bright via the gamma option: raised to max while enabled, restored on disable
public final class FullBright {
    private static Double savedGamma = null;

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        var gamma = mc.options.getGamma();
        if (Config.fullBright) {
            if (savedGamma == null) savedGamma = gamma.getValue();
            if (gamma.getValue() < 1.0) gamma.setValue(1.0);
        } else if (savedGamma != null) {
            gamma.setValue(savedGamma);
            savedGamma = null;
        }
    }
}
