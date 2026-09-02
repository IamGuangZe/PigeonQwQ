package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$CompileTask")
public abstract class MixinRebuildTask {
    @Shadow
    @Final
    protected RenderSectionRegion region;

    @Inject(method = "doTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;"))
    private void onSectionCompiler(SectionBufferBuilderPack buffers, CallbackInfoReturnable<?> cir, @Local SectionPos chunkSectionPos) {
        int minX = chunkSectionPos.minBlockX();
        int minY = chunkSectionPos.minBlockY();
        int minZ = chunkSectionPos.minBlockZ();
        int maxX = chunkSectionPos.maxBlockX();
        int maxY = chunkSectionPos.maxBlockY();
        int maxZ = chunkSectionPos.maxBlockZ();
        RenderEvent.RenderBlockEvent renderBlockEvent = new RenderEvent.RenderBlockEvent();

        for (BlockPos tempPos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = this.region.getBlockState(tempPos);

            if (state.isAir()) continue;
            // ChatUtil.sendDebugMessage("MixinRebuildTask", String.valueOf(block));

            renderBlockEvent.set(tempPos, state);
            Pigeon.EVENT_BUS.post(renderBlockEvent).now();
        }
    }
}
