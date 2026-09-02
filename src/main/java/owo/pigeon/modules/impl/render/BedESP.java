package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

public class BedESP extends Module {
    public BedESP() {
        super("BedESP", Category.RENDER);
    }

    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public ColorSetting color = setting("color", new Color(0x22FF1111, true), v -> true);

    private final Set<BlockPos> beds = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.levelExtractor != null) {
            mc.levelExtractor.allChanged();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        beds.removeIf(pos -> !mc.level.getBlockState(pos).is(BlockTags.BEDS));

        for (BlockPos pos : beds) {
            RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        if (event.getState().is(BlockTags.BEDS)) {
            beds.add(event.getPos().immutable());
        }
    }
}
