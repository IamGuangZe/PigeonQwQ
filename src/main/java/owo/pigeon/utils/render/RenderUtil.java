package owo.pigeon.utils.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import owo.pigeon.utils.ColorUtil;

import java.awt.*;
import java.util.Optional;
import java.util.OptionalDouble;

import static owo.pigeon.Pigeon.mc;

public class RenderUtil {
    public enum ESPMode {
        OUTLINE, FILL, BOTH
    }

    public static final Matrix4f projection = new Matrix4f();

    private static final ByteBufferBuilder CPU_BUFFER = new ByteBufferBuilder(65536);

    private static BufferBuilder begin(PrimitiveTopology topology, VertexFormat format) {
        RenderSystem.assertOnRenderThread();
        CPU_BUFFER.discard();
        return new BufferBuilder(CPU_BUFFER, topology, format);
    }

    private static void submitMesh(PoseStack stack, PrimitiveTopology topology, VertexFormat format, RenderPipeline pipeline, BufferBuilder builder) {
        try (MeshData meshData = builder.buildOrThrow()) {
            GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "pigeonqwq_mesh", 32, meshData.vertexBuffer());
            try {
                GpuBufferSlice dynamicTransform = RenderSystem.getDynamicUniforms().writeTransform(stack.last().pose());
                RenderTarget mainRenderTarget = mc.gameRenderer.mainRenderTarget();
                GpuTextureView colorTexture = mainRenderTarget.getColorTextureView();
                GpuTextureView depthTexture = mainRenderTarget.getDepthTextureView();
                int indexCount = meshData.drawState().indexCount();
                RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(topology);
                GpuBuffer indexGpuBuffer = indexBuffer.getBuffer(indexCount);
                IndexType indexType = indexBuffer.type();
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "pigeonqwq_3d", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty())) {
                    renderPass.setPipeline(pipeline);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setVertexBuffer(0, vertexBuffer.slice());
                    renderPass.setIndexBuffer(indexGpuBuffer, indexType);
                    renderPass.setUniform("DynamicTransforms", dynamicTransform);
                    renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
                }
            } finally {
                vertexBuffer.close();
            }
        }
    }

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

    public static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);

        context.fill(x, y + height - 1, x + width, y + height, color);

        context.fill(x, y, x + 1, y + height, color);

        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawGradientBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int[] gradient) {
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

        BufferBuilder bufferBuilder = begin(PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        bufferBuilder.addVertex(x1, y1, z1).setColor(c.getRGB()).setNormal(nx, ny, nz).setLineWidth((float) pixelWidth);
        bufferBuilder.addVertex(x2, y2, z2).setColor(c.getRGB()).setNormal(nx, ny, nz).setLineWidth((float) pixelWidth);
        bufferBuilder.addVertex(x1, y1, z1).setColor(c.getRGB()).setNormal(nx, ny, nz).setLineWidth((float) pixelWidth);
        bufferBuilder.addVertex(x2, y2, z2).setColor(c.getRGB()).setNormal(nx, ny, nz).setLineWidth((float) pixelWidth);

        submitMesh(stack, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, Layer.getGlobalLines(), bufferBuilder);
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

        BufferBuilder bufferBuilder = begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(x1, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(x2, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(x2, y1, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(x1, y1, 0.0F).setColor(g, h, j, f);

        submitMesh(matrix, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR, Layer.getGlobalQuads(), bufferBuilder);
    }

    public static void drawBox(PoseStack stack, AABB box, Color c, double lineWidth) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.position().x());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.position().y());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.position().z());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.position().x());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.position().y());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.position().z());

        BufferBuilder bufferBuilder = begin(PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        int color = c.getRGB();
        float w = (float) lineWidth;
        VoxelShape shape = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) ->
                addLine(bufferBuilder, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, color, w));


        submitMesh(stack, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, Layer.getGlobalLines(), bufferBuilder);
    }

    private static void addLine(BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, int color, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6F) len = 1.0F;
        builder.addVertex(x1, y1, z1).setColor(color).setNormal(dx / len, dy / len, dz / len).setLineWidth(width);
        builder.addVertex(x2, y2, z2).setColor(color).setNormal(dx / len, dy / len, dz / len).setLineWidth(width);
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

        BufferBuilder bufferBuilder = begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, minY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(minX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, minZ).setColor(c.getRGB());

        bufferBuilder.addVertex(minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, minY, minZ).setColor(c.getRGB());

        bufferBuilder.addVertex(maxX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, minY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(minX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(maxX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, maxY, maxZ).setColor(c.getRGB());

        bufferBuilder.addVertex(minX, minY, minZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, minY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, maxY, maxZ).setColor(c.getRGB());
        bufferBuilder.addVertex(minX, maxY, minZ).setColor(c.getRGB());

        submitMesh(stack, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR, Layer.getGlobalQuads(), bufferBuilder);
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

        Camera cam = mc.gameRenderer.mainCamera();
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

    public static void renderBlockModel(PoseStack stack, BlockState state, BlockPos pos, float alpha) {
        Vec3 camera = mc.getEntityRenderDispatcher().camera.position();
        RenderType renderType = RenderTypes.translucentMovingBlock();
        VertexFormat format = renderType.format();
        PrimitiveTopology topology = renderType.primitiveTopology();
        BufferBuilder bufferBuilder = begin(topology, format);

        stack.pushPose();
        stack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        VertexConsumer consumer = new AlphaVertexConsumer(bufferBuilder, alpha);
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(state);
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(true, false, mc.getBlockColors());
        long blockSeed = state.getSeed(pos);
        BlockQuadOutput output = (x, y, z, quad, instance) -> {
            stack.pushPose();
            stack.translate(x, y, z);
            consumer.putBakedQuad(stack.last(), quad, instance);
            stack.popPose();
        };
        blockRenderer.tesselateBlock(output, 0.0F, 0.0F, 0.0F, mc.level, pos, state, model, blockSeed);
        stack.popPose();

        try (MeshData meshData = bufferBuilder.buildOrThrow()) {
            GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "pigeonqwq_block_model", 32, meshData.vertexBuffer());
            try {
                int indexCount = meshData.drawState().indexCount();
                RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(topology);
                GpuBuffer indexGpuBuffer = indexBuffer.getBuffer(indexCount);
                IndexType indexType = indexBuffer.type();
                Matrix4fStack modelView = RenderSystem.getModelViewStack();
                modelView.pushMatrix();
                modelView.identity();
                PreparedRenderType prepared = renderType.prepare();
                prepared.drawFromBuffer(vertexBuffer, indexGpuBuffer, indexType, 0, 0, indexCount);
                modelView.popMatrix();
            } finally {
                vertexBuffer.close();
            }
        }
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, int alpha) implements VertexConsumer {
        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this(delegate, ((int) (alpha * 255.0F)) & 0xFF);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int a) {
            return delegate.setColor(red, green, blue, alpha);
        }

        @Override
        public VertexConsumer setColor(int rgba) {
            return delegate.setColor((rgba & 0x00FFFFFF) | alpha << 24);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return delegate.setNormal(x, y, z);
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return delegate.setLineWidth(width);
        }
    }
}
