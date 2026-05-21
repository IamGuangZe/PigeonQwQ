package owo.pigeon.utils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Matrix4f;
import owo.pigeon.utils.ColorUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class RenderUtil {
    public enum ESPMode {
        OUTLINE, FILL, BOTH
    }

    public static final Matrix4f projection = new Matrix4f();

    public static Vec3d getInterpolatedPos(Entity entity) {
        float delta = mc.getRenderTickCounter().getTickProgress(true);
        double x = MathHelper.lerp(delta, entity.lastRenderX, entity.getX());
        double y = MathHelper.lerp(delta, entity.lastRenderY, entity.getY());
        double z = MathHelper.lerp(delta, entity.lastRenderZ, entity.getZ());
        return new Vec3d(x, y, z);
    }

    public static void rect(MatrixStack stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(MatrixStack stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);

        context.fill(x, y + height - 1, x + width, y + height, color);

        context.fill(x, y, x + 1, y + height, color);

        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawGradientBorder(DrawContext context, int x, int y, int width, int height, int[] gradient) {
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

    public static void draw3DLine(MatrixStack stack, Vec3d start, Vec3d end, Color c, double pixelWidth) {
        Vec3d camPos = mc.getEntityRenderDispatcher().camera.getCameraPos();

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

        MatrixStack.Entry entry = stack.peek();
        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);

        bufferBuilder.vertex(entry, x1, y1, z1).color(c.getRGB()).normal(entry, nx, ny, nz).lineWidth((float) pixelWidth);
        bufferBuilder.vertex(entry, x2, y2, z2).color(c.getRGB()).normal(entry, nx, ny, nz).lineWidth((float) pixelWidth);

        Layer.getGlobalLines(pixelWidth).draw(bufferBuilder.end());
    }

    protected static void drawHorizontalLine(MatrixStack matrices, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected static void drawVerticalLine(MatrixStack matrices, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + 1, x + 1, y2, color);
    }

    protected static void drawHorizontalLine(MatrixStack matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    protected static void drawVerticalLine(MatrixStack matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(MatrixStack matrix, float x1, float y1, float x2, float y2, int color) {
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

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y2, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y2, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y1, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y1, 0.0F).color(g, h, j, f);

        Layer.getGlobalQuads().draw(bufferBuilder.end());
    }

    public static void drawBox(MatrixStack stack, Box box, Color c, double lineWidth) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getCameraPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getCameraPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getCameraPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getCameraPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getCameraPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getCameraPos().getZ());

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);

        VoxelShape shape = VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
        VertexRendering.drawOutline(stack, bufferBuilder, shape, 0, 0, 0,
                c.getRGB(), (float) lineWidth);

        Layer.getGlobalLines(lineWidth).draw(bufferBuilder.end());
    }

    public static void drawBox(MatrixStack stack, Vec3d vec, Color c, double lineWidth) {
        drawBox(stack, Box.from(vec), c, lineWidth);
    }

    public static void drawBox(MatrixStack stack, BlockPos pos, Color c, double lineWidth) {
        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        VoxelShape renderShape = shape.isEmpty() ? VoxelShapes.fullCube() : shape;

        renderShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawBox(stack, new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c, lineWidth);
        });
    }

    public static void drawBox(MatrixStack stack, Entity entity, Color c, double lineWidth) {
        Vec3d interpPos = getInterpolatedPos(entity);
        Box box = entity.getBoundingBox().offset(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawBox(stack, box, c, lineWidth);
    }

    public static void drawBox(MatrixStack stack, Entity entity, Box box, Color c, double lineWidth) {
        Vec3d vec = getInterpolatedPos(entity);
        Box renderedBox = box.offset(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawBox(stack, renderedBox, c, lineWidth);
    }

    public static void drawBoxFilled(MatrixStack stack, Box box, Color c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getCameraPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getCameraPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getCameraPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getCameraPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getCameraPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getCameraPos().getZ());

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());

        Layer.getGlobalQuads().draw(bufferBuilder.end());
    }

    public static void drawBoxFilled(MatrixStack stack, Vec3d vec, Color c) {
        drawBoxFilled(stack, Box.from(vec), c);
    }

    public static void drawBoxFilled(MatrixStack stack, BlockPos pos, Color c) {
        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        VoxelShape renderShape = shape.isEmpty() ? VoxelShapes.fullCube() : shape;

        renderShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawBoxFilled(stack, new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c);
        });
    }

    public static void drawBoxFilled(MatrixStack stack, Entity entity, Color c) {
        Vec3d interpPos = getInterpolatedPos(entity);
        Box box = entity.getBoundingBox().offset(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawBoxFilled(stack, box, c);
    }

    public static void drawBoxFilled(MatrixStack stack, Entity entity, Box box, Color c) {
        Vec3d vec = getInterpolatedPos(entity);
        Box renderedBox = box.offset(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawBoxFilled(stack, renderedBox, c);
    }

    public static void drawTracer(MatrixStack stack, Box box, double yOffset, Color c, double pixelWidth) {
        double targetX = box.minX + (box.maxX - box.minX) / 2.0;
        double targetY = box.minY + yOffset;
        double targetZ = box.minZ + (box.maxZ - box.minZ) / 2.0;

        drawTracer(stack, new Vec3d(targetX, targetY, targetZ), c, pixelWidth);
    }

    public static void drawTracer(MatrixStack stack, Box box, Color c, double pixelWidth) {
        double centerYOffset = (box.maxY - box.minY) / 2.0;
        drawTracer(stack, box, centerYOffset, c, pixelWidth);
    }

    public static void drawTracer(MatrixStack stack, Vec3d target, Color c, double pixelWidth) {
        Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getCameraPos();

        Camera cam = mc.gameRenderer.getCamera();
        float pitch = cam.getPitch() * (float) (Math.PI / 180);
        float yaw = -cam.getYaw() * (float) (Math.PI / 180);
        float cosYaw = MathHelper.cos(yaw);
        float sinYaw = MathHelper.sin(yaw);
        float cosPitch = MathHelper.cos(pitch);
        float sinPitch = MathHelper.sin(pitch);
        Vec3d rotationVec = new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3d startPos = cameraPos.add(rotationVec.multiply(0.1));

        draw3DLine(stack, startPos, target, c, pixelWidth);
    }

    public static void drawTracer(MatrixStack stack, BlockPos pos, Color c, double pixelWidth) {
        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);

        Box b = shape.isEmpty() ? new Box(0, 0, 0, 1, 1, 1) : shape.getBoundingBox();

        double targetX = pos.getX() + b.minX + (b.maxX - b.minX) / 2.0;
        double targetY = pos.getY() + b.minY + (b.maxY - b.minY) / 2.0;
        double targetZ = pos.getZ() + b.minZ + (b.maxZ - b.minZ) / 2.0;

        drawTracer(stack, new Vec3d(targetX, targetY, targetZ), c, pixelWidth);
    }

    public static void drawTracer(MatrixStack stack, Entity entity, Color c, double pixelWidth) {
        Vec3d interpPos = getInterpolatedPos(entity);

        double eyeY = interpPos.y + entity.getEyeHeight(entity.getPose());

        drawTracer(stack, new Vec3d(interpPos.x, eyeY, interpPos.z), c, pixelWidth);
    }

    public static void drawESP(MatrixStack stack, Box box, Color c, ESPMode mode, boolean drawTracer) {
        if (mode == ESPMode.OUTLINE) drawBox(stack, box, c, 1);
        else if (mode == ESPMode.FILL) drawBoxFilled(stack, box, c);
        else if (mode == ESPMode.BOTH) {
            Color filledColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * 0.2f));
            drawBoxFilled(stack, box, filledColor);
            drawBox(stack, box, c, 1);
        }

        if (drawTracer) drawTracer(stack, box, c, 2.0);
    }

    public static void drawESP(MatrixStack stack, Vec3d vec, Color c, ESPMode mode, boolean drawTracer) {
        drawESP(stack, Box.from(vec), c, mode, false);

        if (drawTracer) drawTracer(stack, vec, c, 2.0);
    }

    public static void drawESP(MatrixStack stack, BlockPos pos, Color c, ESPMode mode, boolean drawTracer) {
        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        VoxelShape renderShape = shape.isEmpty() ? VoxelShapes.fullCube() : shape;

        renderShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            drawESP(stack, new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            ), c, mode, false);
        });

        if (drawTracer) drawTracer(stack, pos, c, 2.0);
    }

    public static void drawESP(MatrixStack stack, Entity entity, Color c, ESPMode mode, boolean drawTracer) {
        Vec3d interpPos = getInterpolatedPos(entity);
        Box box = entity.getBoundingBox().offset(interpPos.x - entity.getX(), interpPos.y - entity.getY(), interpPos.z - entity.getZ());
        drawESP(stack, box, c, mode, false);

        if (drawTracer) drawTracer(stack, entity, c, 2.0);
    }

    public static void drawESP(MatrixStack stack, Entity entity, Box box, Color c, ESPMode mode, boolean drawTracer) {
        Vec3d vec = getInterpolatedPos(entity);
        Box renderedBox = box.offset(vec.x - entity.getX(), vec.y - entity.getY(), vec.z - entity.getZ());
        drawESP(stack, renderedBox, c, mode, false);

        if (drawTracer) drawTracer(stack, entity, c, 2.0);
    }
}
