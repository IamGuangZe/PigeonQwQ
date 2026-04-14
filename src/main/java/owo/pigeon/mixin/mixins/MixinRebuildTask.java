package owo.pigeon.mixin.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import owo.pigeon.modules.impl.render.BedESP;
import owo.pigeon.modules.impl.render.BlockESP;
import owo.pigeon.modules.impl.render.ChestESP;
import owo.pigeon.modules.impl.skyblock.misc.SkyblockESP;
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

            if (state.isAir()) continue;
            // ChatUtil.sendDebugMessage("MixinRebuildTask", String.valueOf(block));

            if (ModuleUtil.isEnable(BedESP.class) && state.isIn(BlockTags.BEDS)) {
                BedESP.beds.add(tempPos.toImmutable());
            }

            if (ModuleUtil.isEnable(BlockESP.class) && state.isOf(ModuleUtil.getModule(BlockESP.class).block.getValue())) {
                BlockESP.blocks.add(tempPos.toImmutable());
            }

            if (ModuleUtil.isEnable(ChestESP.class)) {
                if (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) {
                    ChestESP.chests.add(tempPos.toImmutable());
                } else if (ModuleUtil.getModule(ChestESP.class).enderChest.getValue() && state.isOf(Blocks.ENDER_CHEST)) {
                    ChestESP.chests.add(tempPos.toImmutable());
                }
            }

            if (ModuleUtil.isEnable(SkyblockESP.class)) {
                if (state.isOf(Blocks.LAVA) && tempPos.getX() > 513 && tempPos.getZ() > 513 && (tempPos.getX() > 559 || tempPos.getZ() > 559) && tempPos.getY() > 64) {
                    SkyblockESP.wormLavas.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.BLUE_STAINED_GLASS)) {
                    SkyblockESP.batcaveBlocks.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.LIME_STAINED_GLASS) || state.isOf(Blocks.LIME_STAINED_GLASS_PANE)) {
                    SkyblockESP.jades.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.ORANGE_STAINED_GLASS) || state.isOf(Blocks.ORANGE_STAINED_GLASS_PANE)) {
                    SkyblockESP.ambers.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS) || state.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) {
                    SkyblockESP.sapphires.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.PURPLE_STAINED_GLASS) || state.isOf(Blocks.PURPLE_STAINED_GLASS_PANE)) {
                    SkyblockESP.amethysts.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.RED_STAINED_GLASS) || state.isOf(Blocks.RED_STAINED_GLASS_PANE)) {
                    SkyblockESP.rubys.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.MAGENTA_STAINED_GLASS) || state.isOf(Blocks.MAGENTA_STAINED_GLASS_PANE)) {
                    SkyblockESP.jaspers.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.YELLOW_STAINED_GLASS) || state.isOf(Blocks.YELLOW_STAINED_GLASS_PANE)) {
                    SkyblockESP.topazs.add(tempPos.toImmutable());
                } else if (state.isOf(Blocks.POLISHED_DIORITE)) {
                    SkyblockESP.titaniums.add(tempPos.toImmutable());
                }
            }
        }
    }
}
