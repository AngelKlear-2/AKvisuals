package dev.akvisuals.mixin;

import dev.akvisuals.Stretch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true) // FILL: verify method name on 1.21.11
    private void akv$stretch(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float s = Stretch.factor(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
        if (s == 1f) return;
        cir.setReturnValue(new Matrix4f().scale(s, 1f, 1f).mul(cir.getReturnValue()));
    }
}
