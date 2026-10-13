package dev.akvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.List;

public final class KillAura {
    public static void tick(MinecraftClient mc) {
        if (!Config.killAura || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        PlayerEntity me = mc.player;
        if (me.isDead() || me.getAttackCooldownProgress(0.5f) < 1.0f) return; // vanilla cooldown, no extra spam

        double r = Config.auraRange;
        Box box = me.getBoundingBox().expand(r);
        List<LivingEntity> list = mc.world.getEntitiesByClass(LivingEntity.class, box, e -> valid(me, e, r));

        LivingEntity target = list.stream()
                .min(Comparator.comparingDouble(me::squaredDistanceTo))
                .orElse(null);
        if (target == null) return;

        if (Config.auraRotate) {
            // client-side only: servers don't see this head turn
            me.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
        }
        mc.interactionManager.attackEntity(me, target);
        me.swingHand(Hand.MAIN_HAND);
    }

    private static boolean valid(PlayerEntity me, LivingEntity e, double r) {
        if (e == me || !e.isAlive() || e.isSpectator()) return false;
        if (e instanceof ArmorStandEntity) return false;
        if (e instanceof PlayerEntity) {
            if (!Config.auraPlayers) return false;
        } else if (!Config.auraMobs) {
            return false;
        }
        return me.distanceTo(e) <= r;
    }
}
