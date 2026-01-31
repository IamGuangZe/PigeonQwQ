package owo.pigeon.modules.impl.Skyblock.Rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class AgaricusMiner extends Module {
    public AgaricusMiner() {
        super("AgaricusMiner", Category.SKYBLOCK);
    }


    private BlockPos targetPos;
    private boolean waitingRed;

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            targetPos = null;
            waitingRed = false;
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        Block block = mc.world.getBlockState(pos).getBlock();

        if (targetPos == null || !targetPos.equals(pos)) {
            targetPos = pos;
            waitingRed = false;
        }

        if (block == Blocks.BROWN_MUSHROOM) {
            waitingRed = true;
            return;
        }

        if (waitingRed && block == Blocks.RED_MUSHROOM) {
            PlayerUtil.LeftClick(PlayerUtil.LeftClickMode.MOUSE);
            waitingRed = false;
        }
    }
}
