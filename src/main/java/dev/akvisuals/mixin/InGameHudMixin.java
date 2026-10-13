package dev.akvisuals.mixin;

import dev.akvisuals.Config;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// hides the vanilla crosshair while the custom one is on; require=0 so a renamed method can't crash startup
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void akv$hideVanillaCrosshair(CallbackInfo ci) {
        if (Config.customCrosshair) ci.cancel();
    }
}
