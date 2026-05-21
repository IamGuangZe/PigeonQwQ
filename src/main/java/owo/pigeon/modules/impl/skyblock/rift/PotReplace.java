package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.function.Supplier;

import static owo.pigeon.Pigeon.mc;

public class PotReplace extends Module {
    public PotReplace() {
        super("PotReplace", Category.RIFT);
    }

    public enum ReplaceBlock {
        DAYLIGHT_DETECTOR(() -> Blocks.DAYLIGHT_DETECTOR.getDefaultState().with(Properties.INVERTED, false)),
        SNOW_LAYERS(() -> Blocks.SNOW.getDefaultState().with(Properties.LAYERS, 4));

        private final Supplier<BlockState> stateSupplier;

        ReplaceBlock(Supplier<BlockState> stateSupplier) {
            this.stateSupplier = stateSupplier;
        }

        public BlockState getState() {
            return stateSupplier.get();
        }
    }

    public ModeSetting<ReplaceBlock> replaceBlock = setting("replace-block", ReplaceBlock.SNOW_LAYERS, v -> true);

    @Handler
    public void onReceivePacketPost(PacketEvent.ReceivePacketEvent.Post event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            if (packet.getState().isIn(BlockTags.FLOWER_POTS)) {
                BlockPos pos = packet.getPos();
                mc.execute(() -> {
                    WorldUtil.setBlock(pos, replaceBlock.getValue().getState());
                    ChatUtil.sendDebugMessage(this.name, "Post-Replace at " + pos);
                });
            }
        }

        if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates((pos, state) -> {
                if (state.isIn(BlockTags.FLOWER_POTS)) {
                    mc.execute(() -> {
                        WorldUtil.setBlock(pos, replaceBlock.getValue().getState());
                        ChatUtil.sendDebugMessage(this.name, "Post-Multi-Replace at " + pos);
                    });
                }
            });
        }
    }
}
