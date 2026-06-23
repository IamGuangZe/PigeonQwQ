package owo.pigeon.mixin.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;

import static owo.pigeon.Pigeon.mc;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevel(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(mc.gameRenderer.getMainCamera().xRot()));
        stack.mulPose(Axis.YP.rotationDegrees(mc.gameRenderer.getMainCamera().yRot() + 180f));

        Pigeon.EVENT_BUS.post(new RenderEvent.Render3DEvent(stack, tickCounter.getGameTimeDeltaPartialTick(true))).now();

        stack.popPose();
    }
}
