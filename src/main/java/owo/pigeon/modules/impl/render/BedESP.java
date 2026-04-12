package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
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

    public static Set<BlockPos> beds = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        for (BlockPos pos : beds) {
            if (mc.world.getBlockState(pos).isIn(BlockTags.BEDS)) {
                RenderUtil.drawESP(event.getMatrix(),pos,color.getValue(), mode.getValue(),false);
            } else {
                beds.remove(pos);
            }
        }
    }
}
