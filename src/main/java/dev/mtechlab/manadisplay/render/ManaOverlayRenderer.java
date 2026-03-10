package dev.mtechlab.manadisplay.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.mtechlab.manadisplay.configs.ManaDisplayConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ManaOverlayRenderer {
    private static final Map<BlockPos, Integer> LAST_MANA = new HashMap<>();
    private static final Map<BlockPos, Long> LAST_TIME = new HashMap<>();
    private static final Map<BlockPos, Float> LAST_RATE = new HashMap<>();
    private static final Map<BlockPos, Long> LAST_CHANGE_TIME = new HashMap<>();
    private static final long HOLD_TIME_NS = 2_000_000_000L;
    private static final float ANIMATION_SPEED = 0.5f;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!ManaDisplayConfig.OVERLAY_ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        int radius = 16;
        BlockPos playerPos = mc.player.blockPosition();

        List<BlockEntity> manaEntities = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius)
        )) {
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be == null) continue;

            ManaKind kind = ManaKind.of(be);
            if (kind != null && kind.isEnabledByConfig()) {
                manaEntities.add(be);
            }
        }

        manaEntities.sort((a, b) -> Double.compare(
                b.getBlockPos().distToCenterSqr(camPos),
                a.getBlockPos().distToCenterSqr(camPos)
        ));

        for (BlockEntity be : manaEntities) {
            renderManaBlock(be, pose, buffers, camPos);
        }

        buffers.endBatch();
    }

    private static void renderManaBlock(
            BlockEntity be,
            PoseStack pose,
            MultiBufferSource buffers,
            Vec3 camPos
    ) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        BlockPos pos = be.getBlockPos();

        ManaInfo info = tryGetManaInfo(be);
        if (info == null) return;

        int mana = info.mana;
        int max = info.max;

        pose.pushPose();

        float yOffset = 1.8f;
        String cn = be.getClass().getName().toLowerCase();
        if (cn.contains("flower")) yOffset = 1.35f;
        else if (cn.contains("spreader")) yOffset = 1.55f;

        pose.translate(
                pos.getX() + 0.5 - camPos.x,
                pos.getY() + yOffset - camPos.y,
                pos.getZ() + 0.5 - camPos.z
        );

        Camera cam = mc.gameRenderer.getMainCamera();
        pose.mulPose(Axis.YP.rotationDegrees(-cam.getYRot()));
        pose.translate(0, 0, -0.02);

        float scale = 0.015f;
        pose.scale(-scale, -scale, scale);

        BlockPos key = pos.immutable();
        long now = System.nanoTime();

        if (!LAST_MANA.containsKey(key)) {
            LAST_MANA.put(key, mana);
            LAST_TIME.put(key, now);
            LAST_RATE.put(key, 0f);
            LAST_CHANGE_TIME.put(key, now);
        }

        int lastMana = LAST_MANA.get(key);
        long lastTime = LAST_TIME.get(key);

        float displayRate = LAST_RATE.getOrDefault(key, 0f);
        long lastChange = LAST_CHANGE_TIME.getOrDefault(key, now);

        if (mana != lastMana) {
            float deltaMana = mana - lastMana;

            float deltaSeconds = (now - lastTime) / 1_000_000_000f;

            if (deltaSeconds > 0.0001f) {
                float rate = deltaMana / deltaSeconds;

                float prev = LAST_RATE.getOrDefault(key, 0f);
                float smoothed = prev * 0.65f + rate * 0.35f;

                LAST_RATE.put(key, smoothed);
                LAST_CHANGE_TIME.put(key, now);
                displayRate = smoothed;
            }

            LAST_MANA.put(key, mana);
            LAST_TIME.put(key, now);
        }

        if (now - lastChange > HOLD_TIME_NS) {
            displayRate = 0f;
        }

        String text = mana + " / " + max;

        float textWidth = font.width(text);
        float padding = 6f;
        float width = textWidth + padding * 2;
        float height = 26f;

        float x1 = -width / 2f;
        float y1 = 10;
        float x2 = width / 2f;
        float y2 = height;

        float progress = max > 0 ? mana / (float) max : 0f;

        float partialTick = mc.getFrameTimeNs() * 1e-9f * 20f;
        float time = (mc.level.getGameTime() + partialTick) / 20f * ANIMATION_SPEED;

        drawNotchedRect(
                pose, buffers,
                x1 - 4f,
                y1 - 4f,
                x2 + 4f,
                y2 + 13f,
                0.0005f,
                4f,
                0f, 0f, 0f, 0.6f
        );

        float barHeight = 3f;
        float barWidth = width * 0.85f;

        float barX1 = -barWidth / 2f;
        float barX2 = barWidth / 2f;

        float fill = barWidth * progress;

        float barY2 = y2 - 1f;
        float barY1 = barY2 - barHeight;

        drawRect(
                pose, buffers,
                barX1,
                barY1,
                barX2,
                barY2,
                -0.002f,
                0.45f, 0.45f, 0.48f, 0.75f
        );

        drawBlueManaBar(
                pose, buffers,
                barX1,
                barY1,
                barX1 + fill,
                barY2,
                -0.005f,
                time
        );

        font.drawInBatch(
                text,
                -textWidth / 2f,
                13,
                0xFF9FE8FF,
                false,
                pose.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                0xF000F0
        );

        Component rateComponent;
        int rateColor;

        String value = String.format("%.0f", displayRate);

        Component unit = Component.translatable("manadisplay.unit.mana_per_sec");

        if (displayRate > 0) {
            rateComponent = Component.translatable(
                    "manadisplay.rate.positive",
                    value,
                    unit
            );
            rateColor = 0xFF7CFF7C;
        }
        else if (displayRate < 0) {
            rateComponent = Component.translatable(
                    "manadisplay.rate.negative",
                    value,
                    unit
            );
            rateColor = 0xFFFF7C7C;
        }
        else {
            rateComponent = Component.translatable(
                    "manadisplay.rate.zero",
                    unit
            );
            rateColor = 0xFF8FD8FF;
        }

        String rateText = rateComponent.getString();

        float rateWidth = font.width(rateText);
        float rateY = barY2 + 2f;

        font.drawInBatch(
                rateText,
                -rateWidth / 2f,
                rateY,
                rateColor,
                false,
                pose.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                0xF000F0
        );

        pose.popPose();
    }

    private record ManaInfo(int mana, int max) {}

    private static ManaInfo tryGetManaInfo(BlockEntity be) {
        if (be instanceof ManaPoolBlockEntity pool) {
            return new ManaInfo(pool.getCurrentMana(), pool.getMaxMana());
        }

        Integer mana = tryCallInt(be,
                "getCurrentMana",
                "getMana",
                "mana"
        );

        Integer max = tryCallInt(be,
                "getMaxMana",
                "getMaxManaCapacity",
                "getManaCapacity",
                "maxMana",
                "getMax"
        );

        if (mana == null || max == null) return null;
        if (max <= 0) return null;

        return new ManaInfo(mana, max);
    }

    private static Integer tryCallInt(Object target, String... methodNames) {
        Class<?> c = target.getClass();
        for (String name : methodNames) {
            try {
                var m = c.getMethod(name);
                Object v = m.invoke(target);
                if (v instanceof Integer i) return i;
                if (v instanceof Number n) return n.intValue();
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static void drawRect(
            PoseStack pose,
            MultiBufferSource buffers,
            float x1, float y1,
            float x2, float y2,
            float z,
            float r, float g, float b, float a
    ) {
        Matrix4f m = pose.last().pose();
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());

        vc.vertex(m, x1, y1, z).color(r, g, b, a).endVertex();
        vc.vertex(m, x1, y2, z).color(r, g, b, a).endVertex();
        vc.vertex(m, x2, y2, z).color(r, g, b, a).endVertex();
        vc.vertex(m, x2, y1, z).color(r, g, b, a).endVertex();
    }

    private static void drawNotchedRect(
            PoseStack pose,
            MultiBufferSource buffers,
            float x1, float y1,
            float x2, float y2,
            float z,
            float cut,
            float r, float g, float b, float a
    ) {
        drawRect(pose, buffers, x1 + cut, y1 + cut, x2 - cut, y2 - cut, z, r, g, b, a);
        drawRect(pose, buffers, x1 + cut, y1, x2 - cut, y1 + cut, z, r, g, b, a);
        drawRect(pose, buffers, x1 + cut, y2 - cut, x2 - cut, y2, z, r, g, b, a);
        drawRect(pose, buffers, x1, y1 + cut, x1 + cut, y2 - cut, z, r, g, b, a);
        drawRect(pose, buffers, x2 - cut, y1 + cut, x2, y2 - cut, z, r, g, b, a);
    }

    private static void drawBlueManaBar(
            PoseStack pose,
            MultiBufferSource buffers,
            float x1, float y1,
            float x2, float y2,
            float z,
            float time
    ) {
        Matrix4f m = pose.last().pose();
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());

        float width = x2 - x1;
        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            float px1 = x1 + width * t1;
            float px2 = x1 + width * t2;

            int col1 = blueManaColor(t1, time);
            int col2 = blueManaColor(t2, time);

            float r1 = ((col1 >> 16) & 255) / 255f;
            float g1 = ((col1 >> 8) & 255) / 255f;
            float b1 = (col1 & 255) / 255f;

            float r2 = ((col2 >> 16) & 255) / 255f;
            float g2 = ((col2 >> 8) & 255) / 255f;
            float b2 = (col2 & 255) / 255f;

            vc.vertex(m, px1, y1, z).color(r1, g1, b1, 1f).endVertex();
            vc.vertex(m, px1, y2, z).color(r1, g1, b1, 1f).endVertex();
            vc.vertex(m, px2, y2, z).color(r2, g2, b2, 1f).endVertex();
            vc.vertex(m, px2, y1, z).color(r2, g2, b2, 1f).endVertex();
        }
    }

    private static int blueManaColor(float x, float time) {
        float wave = (float) Math.sin((x * 2.2f) + time) * 0.7f + (float) Math.sin((x * 4.5f) - time * 0.6f) * 0.3f;

        float t = (wave + 1f) * 0.5f;

        float r = lerp(0.15f, 0.35f, t);
        float g = lerp(0.65f, 0.95f, t);
        float b = lerp(0.9f, 1.0f, t);

        return rgb(r, g, b);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static int rgb(float r, float g, float b) {
        int ri = (int)(r * 255);
        int gi = (int)(g * 255);
        int bi = (int)(b * 255);

        return (ri << 16) | (gi << 8) | bi;
    }

    private enum ManaKind {
        POOL,
        SPREADER,
        FLOWER;

        static ManaKind of(BlockEntity be) {
            if (be instanceof ManaPoolBlockEntity) return POOL;
            if (be instanceof ManaSpreaderBlockEntity) return SPREADER;
            if (be instanceof GeneratingFlowerBlockEntity) return FLOWER;

            return null;
        }

        boolean isEnabledByConfig() {
            return switch (this) {
                case POOL -> ManaDisplayConfig.SHOW_POOLS.get();
                case SPREADER -> ManaDisplayConfig.SHOW_SPREADERS.get();
                case FLOWER -> ManaDisplayConfig.SHOW_FLOWERS.get();
            };
        }
    }
}