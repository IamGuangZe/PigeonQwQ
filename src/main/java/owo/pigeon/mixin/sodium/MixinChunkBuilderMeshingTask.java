package owo.pigeon.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.render.BedESP;
import owo.pigeon.modules.impl.render.BlockESP;
import owo.pigeon.modules.impl.render.ChestESP;
import owo.pigeon.modules.impl.skyblock.misc.SkyblockESP;
import owo.pigeon.utils.ModuleUtil;

@Pseudo
@Mixin(value = ChunkBuilderMeshingTask.class,remap = false)
public class MixinChunkBuilderMeshingTask {
    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
    private void onBlockStep(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(name = "y") int y, @Local(name = "z") int z, @Local(name = "x") int x, @Local(name = "blockState") BlockState blockState) {
        if (blockState.isAir()) return;
        // ChatUtil.sendDebugMessage("MixinChunkBuilderMeshingTask", String.valueOf(block));

        if (ModuleUtil.isEnable(BedESP.class) && blockState.isIn(BlockTags.BEDS)) {
            BedESP.beds.add(new BlockPos(x, y, z).toImmutable());
        }

        if (ModuleUtil.isEnable(BlockESP.class) && blockState.isOf(ModuleUtil.getModule(BlockESP.class).block.getValue())) {
            BlockESP.blocks.add(new BlockPos(x, y, z).toImmutable());
        }

        if (ModuleUtil.isEnable(ChestESP.class)) {
            if (blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST)) {
                ChestESP.chests.add(new BlockPos(x, y, z).toImmutable());
            } else if (ModuleUtil.getModule(ChestESP.class).enderChest.getValue() && blockState.isOf(Blocks.ENDER_CHEST)) {
                ChestESP.chests.add(new BlockPos(x, y, z).toImmutable());
            }
        }

        if (ModuleUtil.isEnable(SkyblockESP.class)) {
            if (blockState.isOf(Blocks.LAVA) && x > 513 && z > 513 && (x > 559 || z > 559) && y > 64) {
                SkyblockESP.wormLavas.add(new BlockPos(x,y,z).toImmutable());
            } else if (blockState.isOf(Blocks.BLUE_STAINED_GLASS)) {
                SkyblockESP.batcaveBlocks.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.LIME_STAINED_GLASS) || blockState.isOf(Blocks.LIME_STAINED_GLASS_PANE)) {
                SkyblockESP.jades.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.ORANGE_STAINED_GLASS) || blockState.isOf(Blocks.ORANGE_STAINED_GLASS_PANE)) {
                SkyblockESP.ambers.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS) || blockState.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) {
                SkyblockESP.sapphires.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.PURPLE_STAINED_GLASS) || blockState.isOf(Blocks.PURPLE_STAINED_GLASS_PANE)) {
                SkyblockESP.amethysts.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.RED_STAINED_GLASS) || blockState.isOf(Blocks.RED_STAINED_GLASS_PANE)) {
                SkyblockESP.rubys.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.MAGENTA_STAINED_GLASS) || blockState.isOf(Blocks.MAGENTA_STAINED_GLASS_PANE)) {
                SkyblockESP.jaspers.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.YELLOW_STAINED_GLASS) || blockState.isOf(Blocks.YELLOW_STAINED_GLASS_PANE)) {
                SkyblockESP.topazs.add(new BlockPos(x, y, z).toImmutable());
            } else if (blockState.isOf(Blocks.POLISHED_DIORITE)) {
                SkyblockESP.titaniums.add(new BlockPos(x, y, z).toImmutable());
            }
        }
    }
}
