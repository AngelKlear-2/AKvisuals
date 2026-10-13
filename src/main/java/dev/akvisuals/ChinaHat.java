package dev.akvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class ChinaHat {
    private static final int SEG = 24;
    private static final float RADIUS = 0.45f;
    private static final float HEIGHT = 0.42f;

    public static void render(WorldRenderContext ctx) {
        if (!Config.chinaHat) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        float td = mc.getRenderTickCounter().getTickProgress(true); // FILL: verify tick counter accessor
        float t = mc.world.getTime() + td;
        Vec3d cam = mc.gameRenderer.getCamera().getCameraPos();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayers.lines());

        float spin = Config.hatAnimate ? t * 0.08f * Config.hatSpeed : 0f;
        float bob = Config.hatAnimate ? (float) Math.sin(t * 0.1f * Config.hatSpeed) * 0.04f : 0f;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON) continue;
            if (p.isInvisible()) continue;

            Vec3d pos = p.getLerpedPos(td);
            MatrixStack ms = ctx.matrices();
            ms.push();
            ms.translate(pos.x - cam.x, pos.y - cam.y + p.getHeight() + 0.25 + bob, pos.z - cam.z);

            MatrixStack.Entry entry = ms.peek();
            Matrix4f m = entry.getPositionMatrix();
            int col = 0xFF000000 | java.awt.Color.HSBtoRGB((t * 0.01f) % 1f, 0.85f, 1f);

            float[] bx = new float[SEG];
            float[] bz = new float[SEG];
            for (int i = 0; i < SEG; i++) {
                float a = (float) (i * Math.PI * 2 / SEG) + spin;
                bx[i] = (float) Math.cos(a) * RADIUS;
                bz[i] = (float) Math.sin(a) * RADIUS;
            }

            for (int i = 0; i < SEG; i++) {
                int j = (i + 1) % SEG;
                line(vc, entry, m, 0, HEIGHT, 0, bx[i], 0, bz[i], col); // edges to apex
                line(vc, entry, m, bx[i], 0, bz[i], bx[j], 0, bz[j], col); // base ring
            }

            ms.pop();
        }
    }

    private static void line(VertexConsumer vc, MatrixStack.Entry e, Matrix4f m,
                             float ax, float ay, float az, float bx, float by, float bz, int col) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        dx /= len;
        dy /= len;
        dz /= len;
        vc.vertex(m, ax, ay, az).color(col).lineWidth(1.0f).normal(e, dx, dy, dz);
        vc.vertex(m, bx, by, bz).color(col).lineWidth(1.0f).normal(e, dx, dy, dz);
    }
}
