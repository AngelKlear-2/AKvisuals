package dev.akvisuals;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

// target airborne: find the ground under it, put obsidian there, put a crystal on the obsidian, then hit the crystal.
// one action per call; returns true when it did something this tick
public final class CrystalAura {
    private static final int MAX_DEPTH = 8;
    private static final double MAX_RANGE_SQ = 36.0; // 6 blocks

    public static boolean tick(MinecraftClient mc, ClientPlayerEntity me, LivingEntity target) {
        if (me.squaredDistanceTo(target) > MAX_RANGE_SQ) return false;

        BlockPos ground = findGround(mc, target.getBlockPos());
        if (ground == null) return false;
        BlockPos obsidianPos = ground.up();
        BlockPos crystalPos = obsidianPos.up();

        // 1. crystal already up: hit it
        EndCrystalEntity crystal = findCrystal(mc, crystalPos);
        if (crystal != null) {
            mc.interactionManager.attackEntity(me, crystal);
            me.swingHand(Hand.MAIN_HAND);
            return true;
        }

        BlockState obsidianState = mc.world.getBlockState(obsidianPos);

        // 2. obsidian in place: crystal on top
        if (obsidianState.isOf(Blocks.OBSIDIAN)) {
            return placeFromHotbar(mc, me, Items.END_CRYSTAL, obsidianPos);
        }

        // 3. free spot: obsidian on the ground block
        if (obsidianState.isAir()) {
            return placeFromHotbar(mc, me, Items.OBSIDIAN, ground);
        }

        return false;
    }

    // scan down from the target's feet for the first solid block
    private static BlockPos findGround(MinecraftClient mc, BlockPos feet) {
        BlockPos p = feet.down();
        for (int i = 0; i < MAX_DEPTH; i++) {
            if (mc.world.getBlockState(p).isSolidBlock(mc.world, p)) return p;
            p = p.down();
        }
        return null;
    }

    private static EndCrystalEntity findCrystal(MinecraftClient mc, BlockPos pos) {
        Box box = new Box(pos).expand(0.5, 1.0, 0.5);
        List<EndCrystalEntity> list = mc.world.getEntitiesByClass(EndCrystalEntity.class, box, e -> true);
        return list.isEmpty() ? null : list.get(0);
    }

    // swap to the item in the hotbar, place it on the support block's top face, swap back
    private static boolean placeFromHotbar(MinecraftClient mc, ClientPlayerEntity me, Item item, BlockPos support) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (me.getInventory().getStack(i).isOf(item)) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return false;

        int original = me.getInventory().getSelectedSlot();
        me.getInventory().setSelectedSlot(slot);
        Vec3d hitPos = Vec3d.ofCenter(support).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitPos, Direction.UP, support, false);
        mc.interactionManager.interactBlock(me, Hand.MAIN_HAND, hit);
        me.getInventory().setSelectedSlot(original);
        return true;
    }
}
