package owo.pigeon.mixin.mixins;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.Render.BedESP;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ModuleUtil;

import java.util.List;

@Mixin(BlockRenderManager.class)
public class MixinBlockRenderManager {
    @Inject(method = "renderBlock", at = @At("HEAD"))
    public void onRenderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<BlockModelPart> parts, CallbackInfo ci) {
        System.out.print("aaaaaa");
        ChatUtil.sendIfHudReadyMessage("aaaaaa");
        ChatUtil.sendDebugMessage("MixinBlockRenderManager","renderBlock");

        Block block = state.getBlock();
        BlockPos blockPos = new BlockPos(pos);

        if (ModuleUtil.isEnable(BedESP.class) && block instanceof BedBlock) BedESP.beds.add(blockPos);
    }
}
