package dev.akvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class KillAura {
    private static final Random RNG = new Random();
    private static final double AIM_TOLERANCE = 6.0; // degrees; attacks only once the turn has settled
    private static final float AIM_HEIGHT = 0.6f;    // fraction of target height: chest, not head

    public static void tick(MinecraftClient mc) {
        if (!Config.killAura || mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        PlayerEntity me = mc.player;
        if (me.isDead()) return;

        LivingEntity target = findTarget(me, mc);
        if (target == null) return;

        // crystal combo: target in the air, obsidian + crystal + spam
        if (Config.crystalAura && !target.isOnGround() && CrystalAura.tick(mc, me, target)) return;

        boolean aimed = !Config.auraRotate || aimAt(me, aimPoint(target));
        if (!aimed) return;
        if (!weaponAllowed(me.getMainHandStack())) return;
        if (me.getAttackCooldownProgress(0.5f) < 0.93f) return; // wait for vanilla cooldown, a bit of slack for natural timing

        mc.interactionManager.attackEntity(me, target);
        me.swingHand(Hand.MAIN_HAND);
    }

    private static LivingEntity findTarget(PlayerEntity me, MinecraftClient mc) {
        double r = Config.auraRange;
        Box box = me.getBoundingBox().expand(r);
        List<LivingEntity> list = mc.world.getEntitiesByClass(LivingEntity.class, box, e -> valid(me, e, r));
        return list.stream().min(Comparator.comparingDouble(me::squaredDistanceTo)).orElse(null);
    }

    private static Vec3d aimPoint(LivingEntity target) {
        return new Vec3d(target.getX(), target.getY() + target.getHeight() * AIM_HEIGHT, target.getZ());
    }

    // proportional turn with a speed cap and a little humanising jitter; returns true once on target
    private static boolean aimAt(PlayerEntity me, Vec3d p) {
        Vec3d eye = me.getEyePos();
        double dx = p.x - eye.x, dy = p.y - eye.y, dz = p.z - eye.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yawTarget = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitchTarget = (float) -Math.toDegrees(Math.atan2(dy, horiz));

        float yaw = me.getYaw();
        float pitch = me.getPitch();
        float dyaw = wrap(yawTarget - yaw);
        float dpitch = pitchTarget - pitch;

        float cap = Config.auraSpeed * (0.85f + RNG.nextFloat() * 0.3f);
        float stepYaw = clamp(dyaw * 0.35f, -cap, cap);
        float stepPitch = clamp(dpitch * 0.35f, -cap * 0.6f, cap * 0.6f);

        me.setYaw(yaw + stepYaw);
        me.setPitch(clamp(pitch + stepPitch, -90f, 90f));

        return Math.abs(wrap(yawTarget - me.getYaw())) < AIM_TOLERANCE
                && Math.abs(pitchTarget - me.getPitch()) < AIM_TOLERANCE;
    }

    private static boolean weaponAllowed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        if (path.endsWith("sword")) return Config.auraSword;
        if (path.endsWith("axe") && !path.endsWith("pickaxe")) return Config.auraAxe;
        if (path.endsWith("mace")) return Config.auraMace;
        if (path.endsWith("spear")) return Config.auraSpear;
        return false;
    }

    private static float wrap(float deg) {
        return ((deg + 540f) % 360f) - 180f;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
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
