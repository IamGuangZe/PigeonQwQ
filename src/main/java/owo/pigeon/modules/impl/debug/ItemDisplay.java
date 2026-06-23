package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class ItemDisplay extends Module {
    public ItemDisplay() {
        super("ItemDisplay", Category.DEBUG);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Display.ItemDisplay)) continue;

            AABB expandedBox = entity.getBoundingBox().inflate(0.2);
            RenderUtil.drawESP(event.getMatrix(), entity, expandedBox, Color.GREEN,
                    RenderUtil.ESPMode.OUTLINE, false);

            BlockPos blockPos = entity.blockPosition();
            RenderUtil.drawESP(event.getMatrix(), blockPos, Color.BLUE,
                    RenderUtil.ESPMode.OUTLINE, false);
        }
    }
}
