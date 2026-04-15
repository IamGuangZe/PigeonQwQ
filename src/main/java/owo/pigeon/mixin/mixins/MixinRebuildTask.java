package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;

import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public abstract class MixinRebuildTask {
    @Shadow
    @Final
    protected ChunkRendererRegion region;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/SectionBuilder;build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Lcom/mojang/blaze3d/systems/VertexSorter;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;"))
    private void onSectionBuild(BlockBufferAllocatorStorage buffers, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local ChunkSectionPos chunkSectionPos) {
        int minX = chunkSectionPos.getMinX();
        int minY = chunkSectionPos.getMinY();
        int minZ = chunkSectionPos.getMinZ();
        int maxX = chunkSectionPos.getMaxX();
        int maxY = chunkSectionPos.getMaxY();
        int maxZ = chunkSectionPos.getMaxZ();
        RenderEvent.RenderBlockEvent renderBlockEvent = new RenderEvent.RenderBlockEvent();

        for (BlockPos tempPos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = this.region.getBlockState(tempPos);

            if (state.isAir()) continue;
            // ChatUtil.sendDebugMessage("MixinRebuildTask", String.valueOf(block));

            renderBlockEvent.set(tempPos, state);
            Pigeon.EVENT_BUS.post(renderBlockEvent).now();
        }
    }
}
