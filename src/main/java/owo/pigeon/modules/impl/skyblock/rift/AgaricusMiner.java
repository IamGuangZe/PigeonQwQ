package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class AgaricusMiner extends Module {
    public AgaricusMiner() {
        super("AgaricusMiner", Category.RIFT);
    }


    private BlockPos targetPos;
    private boolean waitingRed;

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            targetPos = null;
            waitingRed = false;
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);

        if (targetPos == null || !targetPos.equals(pos)) {
            targetPos = pos;
            waitingRed = false;
        }

        if (state.isOf(Blocks.BROWN_MUSHROOM)) {
            waitingRed = true;
            return;
        }

        if (waitingRed && state.isOf(Blocks.RED_MUSHROOM)) {
            PlayerUtil.LeftClick(PlayerUtil.LeftClickMode.MOUSE);
            waitingRed = false;
        }
    }
}
