package owo.pigeon.mixin.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import owo.pigeon.modules.impl.render.BedESP;
import owo.pigeon.modules.impl.render.BlockESP;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

@Pseudo
@Mixin(value = ChunkBuilderMeshingTask.class,remap = false)
public class MixinChunkBuilderMeshingTask {
    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBlockStep(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, Profiler profiler, BuiltSectionInfo.Builder renderData, ChunkOcclusionDataBuilder occluder, ChunkBuildBuffers buffers, BlockRenderCache cache, LevelSlice slice, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos.Mutable blockPos, BlockPos.Mutable modelOffset, boolean sortEnabled, TranslucentGeometryCollector collector, BlockRenderer blockRenderer, int y, int z, int x, BlockState blockState) {
        if (blockState.isAir()) return;

        Block block = blockState.getBlock();
        ChatUtil.sendDebugMessage("MixinChunkBuilderMeshingTask", String.valueOf(block));

        if (ModuleUtil.isEnable(BedESP.class) && block instanceof BedBlock) {
            BedESP.beds.add(new BlockPos(x, y, z).toImmutable());
        }

        if (ModuleUtil.isEnable(BlockESP.class) && block == ModuleUtil.getModule(BlockESP.class).block.getValue()) {
            BlockESP.blocks.add(new BlockPos(x, y, z).toImmutable());
        }
    }
}
