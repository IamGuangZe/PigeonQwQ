package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.BlockSetting;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

public class BlockESP extends Module {
    public BlockESP() {
        super("BlockESP", Category.RENDER);
    }

    public BlockSetting block = setting("block", Blocks.DRAGON_EGG, v -> true);
    public IntSetting limit = setting("limit", -1, -1, 100, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public ColorSetting color = setting("color", new Color(0x22FF1111, true), v -> true);

    private final Set<BlockPos> blocks = ConcurrentHashMap.newKeySet();

    private Block lastBlock;

    @Override
    public void onEnable() {
        if (mc.worldRenderer == null) return;
        lastBlock = block.getValue();
        mc.worldRenderer.reload();
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        Block targetBlock = block.getValue();

        if (lastBlock != targetBlock && mc.worldRenderer != null) {
            blocks.clear();
            lastBlock = targetBlock;
            mc.worldRenderer.reload();
        }

        blocks.removeIf(pos -> !mc.world.getBlockState(pos).isOf(targetBlock));

        if (limit.getValue() == -1) {
            for (BlockPos pos : blocks) {
                RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
            }
        } else {
            List<BlockPos> sorted = new ArrayList<>(blocks);
            sorted.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ())));

            int count = Math.min(limit.getValue(), sorted.size());
            for (int i = 0; i < count; i++) {
                RenderUtil.drawESP(event.getMatrix(), sorted.get(i), color.getValue(), mode.getValue(), false);
            }
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        if (event.getState().isOf(block.getValue())) {
            blocks.add(event.getPos().toImmutable());
        }
    }
}
