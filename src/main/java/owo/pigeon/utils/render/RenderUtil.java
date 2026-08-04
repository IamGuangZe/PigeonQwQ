package owo.pigeon.utils.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import owo.pigeon.utils.ColorUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class RenderUtil {
    public enum ESPMode {
        OUTLINE, FILL, BOTH
    }

    public static final Matrix4f projection = new Matrix4f();

    public static Vec3 getInterpolatedPos(Entity entity) {
        float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        double x = Mth.lerp(delta, entity.xOld, entity.getX());
        double y = Mth.lerp(delta, entity.yOld, entity.getY());
        double z = Mth.lerp(delta, entity.zOld, entity.getZ());
        return new Vec3(x, y, z);
    }

    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    public static void drawBorder(GuiGraphics context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);

        context.fill(x, y + height - 1, x + width, y + height, color);

        context.fill(x, y, x + 1, y + height, color);

        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawGradientBorder(GuiGraphics context, int x, int y, int width, int height, int[] gradient) {
        if (gradient == null) {
            drawBorder(context, x, y, width, height, Color.WHITE.getRGB());
            return;
        }
        int stepsH = Math.max(2, width / 20);
        int stepsV = Math.max(2, height / 20);

        // top: left(0.0) → right(0.5)
        for (int i = 0; i < width; i += stepsH) {
            int w = Math.min(stepsH, width - i);
            float r = (float) i / (2.0f * width);
            context.fill(x + i, y, x + i + w, y + 1, ColorUtil.interpolateGradient(gradient, r));
        }
        // right: top(0.5) → bottom(1.0)
        for (int i = 0; i < height; i += stepsV) {
            int h = Math.min(stepsV, height - i);
            float r = 0.5f + (float) i / (2.0f * height);
            context.fill(x + width - 1, y + i, x + width, y + i + h, ColorUtil.interpolateGradient(gradient, r));
        }
        // bottom: right(1.0) → left(0.5)
        for (int i = 0; i < width; i += stepsH) {
            int w = Math.min(stepsH, width - i);
            float r = 1.0f - (float) i / (2.0f * width);
            context.fill(x + width - i - w, y + height - 1, x + width - i, y + height, ColorUtil.interpolateGradient(gradient, r));
        }
        // left: bottom(0.5) → top(0.0)
        for (int i = 0; i < height; i += stepsV) {
            int h = Math.min(stepsV, height - i);
            float r = 0.5f - (float) i / (2.0f * height);
            context.fill(x, y + height - i - h, x + 1, y + height - i, ColorUtil.interpolateGradient(gradient, r));
        }
    }

    public static void draw3DLine(PoseStack stack, Vec3 start, Vec3 end, Color c, double pixelWidth) {
        Vec3 camPos = mc.getEntityRenderDispatcher().camera.position();

        float x1 = (float) (start.x - camPos.x);
        float y1 = (float) (start.y - camPos.y);
        float z1 = (float) (start.z - camPos.z);
        float x2 = (float) (end.x - camPos.x);
        float y2 = (float) (end.y - camPos.y);
        float z2 = (float) (end.z - camPos.z);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6) return;
        float nx = (float) (dx / len);
        float ny = (float) (dy / len);
        float nz = (float) (dz / len);

        PoseStack.Pose entry = stack.last();
        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        bufferBuilder.addVertex(entry, x1, y1, z1).setColor(c.getRGB()).setNormal(entry, nx, ny, nz).setLineWidth((float) pixelWidth);
        bufferBuilder.addVertex(entry, x2, y2, z2).setColor(c.getRGB()).setNormal(entry, nx, ny, nz).setLineWidth((float) pixelWidth);

        Layer.getGlobalLines(pixelWidth).draw(bufferBuilder.buildOrThrow());
    }

    protected static void drawHorizontalLine(PoseStack matrices, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected static void drawVerticalLine(PoseStack matrices, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + 1, x + 1, y2, color);
    }

    protected static void drawHorizontalLine(PoseStack matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    protected static void drawVerticalLine(PoseStack matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(PoseStack matrix, float x1, float y1, float x2, float y2, int color) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float j = (float) (color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y1, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y1, 0.0F).setColor(g, h, j, f);

        Layer.getGlobalQuads().draw(bufferBuilder.buildOrThrow());
    }

    public static void drawBox(PoseStack stack, AABB box, Color c, double lineWidth) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.position().x());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.position().y());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.position().z());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.position().x());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.position().y());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.position().z());

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        VoxelShape shape = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
        ShapeRenderer.renderShape(stack, bufferBuilder, shape, 0, 0, 0,
                c.getRGB(), (float) lineWidth);

        Layer.getGlobalLines(lineWidth).draw(bufferBuilder.buildOrThrow());
    }

    public static void drawBox(PoseStack stack, Vec3 vec, Color c, double lineWidth) {
        drawBox(stack, AABB.unitCubeFromLowerCorner(vec), c, lineWidth);
    }

    public static void drawBox(PoseStack stack, BlockPos pos, Color c, double lineWidth) {
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape(mc.level, pos);
        VoxelShape renderShape = shape.isEmpty() ? Shapes.block() : shape;

        renderShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawBox(stack, new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c, lineWidth);
        });
    }

    public static void drawBox(PoseStack stack, Entity entity, Color c, double lineWidth) {
        Vec3 interpPos = getInterpolatedPos(entity);
        AABB box = entity.getBoundingBox().move(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawBox(stack, box, c, lineWidth);
    }

    public static void drawBox(PoseStack stack, Entity entity, AABB box, Color c, double lineWidth) {
        Vec3 vec = getInterpolatedPos(entity);
        AABB renderedBox = box.move(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawBox(stack, renderedBox, c, lineWidth);
    }

    public static void drawBoxFilled(PoseStack stack, AABB box, Color c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.position().x());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.position().y());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.position().z());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.position().x());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.position().y());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.position().z());

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(c.getRGB());

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(c.getRGB());

        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(c.getRGB());

        Layer.getGlobalQuads().draw(bufferBuilder.buildOrThrow());
    }

    public static void drawBoxFilled(PoseStack stack, Vec3 vec, Color c) {
        drawBoxFilled(stack, AABB.unitCubeFromLowerCorner(vec), c);
    }

    public static void drawBoxFilled(PoseStack stack, BlockPos pos, Color c) {
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape(mc.level, pos);
        VoxelShape renderShape = shape.isEmpty() ? Shapes.block() : shape;

        renderShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawBoxFilled(stack, new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c);
        });
    }

    public static void drawBoxFilled(PoseStack stack, Entity entity, Color c) {
        Vec3 interpPos = getInterpolatedPos(entity);
        AABB box = entity.getBoundingBox().move(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawBoxFilled(stack, box, c);
    }

    public static void drawBoxFilled(PoseStack stack, Entity entity, AABB box, Color c) {
        Vec3 vec = getInterpolatedPos(entity);
        AABB renderedBox = box.move(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawBoxFilled(stack, renderedBox, c);
    }

    public static void drawTracer(PoseStack stack, AABB box, double yOffset, Color c, double pixelWidth) {
        double targetX = box.minX + (box.maxX - box.minX) / 2.0;
        double targetY = box.minY + yOffset;
        double targetZ = box.minZ + (box.maxZ - box.minZ) / 2.0;

        drawTracer(stack, new Vec3(targetX, targetY, targetZ), c, pixelWidth);
    }

    public static void drawTracer(PoseStack stack, AABB box, Color c, double pixelWidth) {
        double centerYOffset = (box.maxY - box.minY) / 2.0;
        drawTracer(stack, box, centerYOffset, c, pixelWidth);
    }

    public static void drawTracer(PoseStack stack, Vec3 target, Color c, double pixelWidth) {
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.position();

        Camera cam = mc.gameRenderer.getMainCamera();
        float pitch = cam.xRot() * (float) (Math.PI / 180);
        float yaw = -cam.yRot() * (float) (Math.PI / 180);
        float cosYaw = Mth.cos(yaw);
        float sinYaw = Mth.sin(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = Mth.sin(pitch);
        Vec3 rotationVec = new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3 startPos = cameraPos.add(rotationVec.scale(0.1));

        draw3DLine(stack, startPos, target, c, pixelWidth);
    }

    public static void drawTracer(PoseStack stack, BlockPos pos, Color c, double pixelWidth) {
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape(mc.level, pos);

        AABB b = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();

        double targetX = pos.getX() + b.minX + (b.maxX - b.minX) / 2.0;
        double targetY = pos.getY() + b.minY + (b.maxY - b.minY) / 2.0;
        double targetZ = pos.getZ() + b.minZ + (b.maxZ - b.minZ) / 2.0;

        drawTracer(stack, new Vec3(targetX, targetY, targetZ), c, pixelWidth);
    }

    public static void drawTracer(PoseStack stack, Entity entity, Color c, double pixelWidth) {
        Vec3 interpPos = getInterpolatedPos(entity);

        double eyeY = interpPos.y + entity.getEyeHeight(entity.getPose());

        drawTracer(stack, new Vec3(interpPos.x, eyeY, interpPos.z), c, pixelWidth);
    }

    public static void drawESP(PoseStack stack, AABB box, Color c, ESPMode mode, boolean drawTracer) {
        if (mode == ESPMode.OUTLINE) drawBox(stack, box, c, 1);
        else if (mode == ESPMode.FILL) drawBoxFilled(stack, box, c);
        else if (mode == ESPMode.BOTH) {
            Color filledColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * 0.2f));
            drawBoxFilled(stack, box, filledColor);
            drawBox(stack, box, c, 1);
        }

        if (drawTracer) drawTracer(stack, box, c, 2.0);
    }

    public static void drawESP(PoseStack stack, Vec3 vec, Color c, ESPMode mode, boolean drawTracer) {
        drawESP(stack, AABB.unitCubeFromLowerCorner(vec), c, mode, drawTracer);
    }

    public static void drawESP(PoseStack stack, BlockPos pos, Color c, ESPMode mode, boolean drawTracer) {
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape(mc.level, pos);
        VoxelShape renderShape = shape.isEmpty() ? Shapes.block() : shape;

        renderShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawESP(stack, new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c, mode, drawTracer);
        });
    }

    public static void drawESP(PoseStack stack, Entity entity, Color c, ESPMode mode, boolean drawTracer) {
        Vec3 interpPos = getInterpolatedPos(entity);
        AABB box = entity.getBoundingBox().move(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawESP(stack, box, c, mode, drawTracer);
    }

    public static void drawESP(PoseStack stack, Entity entity, AABB box, Color c, ESPMode mode, boolean drawTracer) {
        Vec3 vec = getInterpolatedPos(entity);
        AABB renderedBox = box.move(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawESP(stack, renderedBox, c, mode, drawTracer);
    }
}
