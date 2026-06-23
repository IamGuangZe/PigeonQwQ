package owo.pigeon.modules.impl.skyblock.dungeon;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;

import static owo.pigeon.Pigeon.mc;
import static owo.pigeon.utils.WorldUtil.setBlock;

public class PillarsReplace extends Module {
    public PillarsReplace() {
        super("PillarsReplace", Category.DUNGEON);
    }

    private static final BlockPos[] PILLARS = {
            new BlockPos(46, 169, 41), //lime
            new BlockPos(46, 169, 65), //yellow
            new BlockPos(100, 169, 65), //purple
            new BlockPos(100, 169, 41) //red
    };

    private static final BlockState[] PILLAR_COLORS = {
            Blocks.LIME_STAINED_GLASS.defaultBlockState(),
            Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
            Blocks.PURPLE_STAINED_GLASS.defaultBlockState(),
            Blocks.RED_STAINED_GLASS.defaultBlockState()
    };

    @Handler
    public void onTick(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (!DungeonUtil.isInBoss(DungeonUtil.Floor.F7) && !DungeonUtil.isInBoss(DungeonUtil.Floor.M7)) return;
        if (DungeonUtil.getFloor7Stage() != 2) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < PILLARS.length; i++) {
            BlockPos pillar = PILLARS[i];
            for (int dx = pillar.getX() - 3; dx <= pillar.getX() + 3; dx++) {
                for (int dy = pillar.getY(); dy <= pillar.getY() + 37; dy++) {
                    for (int dz = pillar.getZ() - 3; dz <= pillar.getZ() + 3; dz++) {
                        pos.set(dx, dy, dz);
                        BlockState state = mc.level.getBlockState(pos);
                        if (state.is(Blocks.DIORITE) || state.is(Blocks.POLISHED_DIORITE)) {
                            setBlock(pos, PILLAR_COLORS[i]);
                        }
                    }
                }
            }
        }
    }
}
