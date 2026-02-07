package owo.pigeon.modules.impl.Render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.BlockSetting;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static owo.pigeon.Pigeonqwq.mc;

public class BlockESP extends Module {
    public BlockESP() {
        super("BlockESP", Category.RENDER);
    }

    public BlockSetting block = setting("block", Blocks.DRAGON_EGG, v->true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.FILL, v -> true);
    public ColorSetting color = setting("color", new Color(0x22FF1111, true), v -> true);

    public static CopyOnWriteArraySet<BlockPos> blocks = new CopyOnWriteArraySet<>();

    private Block lastBlock;

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
            lastBlock = block.getValue();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (lastBlock != block.getValue() && mc.worldRenderer != null) {
            mc.worldRenderer.reload();
            lastBlock = block.getValue();
        }

        for (BlockPos pos : blocks) {
            if (mc.world.getBlockState(pos).getBlock() == block.getValue()) {
                RenderUtil.drawESP(event.getMatrix(),pos,color.getValue(), mode.getValue());
            } else {
                blocks.remove(pos);
            }
        }
    }
}
