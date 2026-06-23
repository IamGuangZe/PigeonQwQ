package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    private final Set<BlockPos> chests = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        chests.removeIf(pos -> {
            BlockState state = mc.level.getBlockState(pos);
            boolean valid = state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
                    || (enderChest.getValue() && state.is(Blocks.ENDER_CHEST));
            return !valid;
        });

        for (BlockPos pos : chests) {
            RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        BlockState state = event.getState();
        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)) {
            chests.add(event.getPos().immutable());
        } else if (enderChest.getValue() && state.is(Blocks.ENDER_CHEST)) {
            chests.add(event.getPos().immutable());
        }
    }
}
