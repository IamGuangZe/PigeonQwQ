package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            targetPos = null;
            waitingRed = false;
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        if (targetPos == null || !targetPos.equals(pos)) {
            targetPos = pos;
            waitingRed = false;
        }

        if (state.is(Blocks.BROWN_MUSHROOM)) {
            waitingRed = true;
            return;
        }

        if (waitingRed && state.is(Blocks.RED_MUSHROOM)) {
            PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
            waitingRed = false;
        }
    }
}
