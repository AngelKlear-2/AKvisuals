package dev.akvisuals.mixin;

import dev.akvisuals.Config;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

// particles: toggle + density. require=0 so a renamed method can't crash startup
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void akv$particles(Particle particle, CallbackInfo ci) {
        if (!Config.particles) {
            ci.cancel();
            return;
        }
        if (Config.particleDensity < 100 && ThreadLocalRandom.current().nextInt(100) >= Config.particleDensity) {
            ci.cancel();
        }
    }
}
