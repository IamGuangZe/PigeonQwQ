package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static owo.pigeon.Pigeon.mc;

public class ChestESP extends Module {
    public ChestESP() {
        super("ChestESP", Category.RENDER);
    }

    public EnableSetting enderChest = setting("ender-chest", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public ColorSetting color = setting("color", new Color(0x5F00AAFF, true), v -> true);

    public static CopyOnWriteArraySet<BlockPos> chests = new CopyOnWriteArraySet<>();

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck()) return;

        for (BlockPos pos : chests) {
            if (mc.world.getBlockState(pos).getBlock() instanceof ChestBlock) {
                RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
            } else if (enderChest.getValue() && mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock) {
                RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
            } else {
                chests.remove(pos);
            }
        }
    }
}
