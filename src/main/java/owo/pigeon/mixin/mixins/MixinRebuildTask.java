package owo.pigeon.mixin.mixins;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import owo.pigeon.modules.impl.Render.BedESP;
import owo.pigeon.modules.impl.Render.BlockESP;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ModuleUtil;

import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public abstract class MixinRebuildTask {
    @Shadow
    @Final
    protected ChunkRendererRegion region;

    @Inject(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/SectionBuilder;build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Lcom/mojang/blaze3d/systems/VertexSorter;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onSectionBuild(BlockBufferAllocatorStorage buffers,
                                CallbackInfoReturnable<CompletableFuture<?>> cir,
                                long l,
                                ChunkSectionPos chunkSectionPos) {
        int minX = chunkSectionPos.getMinX();
        int minY = chunkSectionPos.getMinY();
        int minZ = chunkSectionPos.getMinZ();
        int maxX = chunkSectionPos.getMaxX();
        int maxY = chunkSectionPos.getMaxY();
        int maxZ = chunkSectionPos.getMaxZ();

        for (BlockPos tempPos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = this.region.getBlockState(tempPos);
            Block block = state.getBlock();

            if (state.isAir()) continue;
            ChatUtil.sendDebugMessage("MixinRebuildTask", String.valueOf(state.getBlock()));

            if (ModuleUtil.isEnable(BedESP.class) && block instanceof BedBlock) {
                BedESP.beds.add(tempPos.toImmutable());
            }

            if (ModuleUtil.isEnable(BlockESP.class) && block == ModuleUtil.getModule(BlockESP.class).block.getValue()) {
                BlockESP.blocks.add(tempPos.toImmutable());
            }
        }
    }
}
