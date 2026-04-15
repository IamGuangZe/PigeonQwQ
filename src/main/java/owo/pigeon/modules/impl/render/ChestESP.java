package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

public class ChestESP extends Module {
    public ChestESP() {
        super("ChestESP", Category.RENDER);
    }

    public EnableSetting enderChest = setting("ender-chest", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public ColorSetting color = setting("color", new Color(0x5F00AAFF, true), v -> true);

    public static Set<BlockPos> chests = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        chests.removeIf(pos -> {
            BlockState state = mc.world.getBlockState(pos);
            boolean valid = state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)
                    || (enderChest.getValue() && state.isOf(Blocks.ENDER_CHEST));
            return !valid;
        });

        for (BlockPos pos : chests) {
            RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        BlockState state = event.getState();
        if (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) {
            ChestESP.chests.add(event.getPos().toImmutable());
        } else if (enderChest.getValue() && state.isOf(Blocks.ENDER_CHEST)) {
            ChestESP.chests.add(event.getPos().toImmutable());
        }
    }
}
