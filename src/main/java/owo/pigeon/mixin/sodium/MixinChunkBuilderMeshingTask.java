package owo.pigeon.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;

@Pseudo
@Mixin(value = ChunkBuilderMeshingTask.class,remap = false)
public class MixinChunkBuilderMeshingTask {
    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
    private void onBlockStep(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(name = "y") int y, @Local(name = "z") int z, @Local(name = "x") int x, @Local(name = "blockState") BlockState blockState) {
        if (blockState.isAir()) return;
        // ChatUtil.sendDebugMessage("MixinChunkBuilderMeshingTask", String.valueOf(block));

        Pigeon.EVENT_BUS.post(new RenderEvent.RenderBlockEvent(new BlockPos(x, y, z), blockState)).now();
    }
}
