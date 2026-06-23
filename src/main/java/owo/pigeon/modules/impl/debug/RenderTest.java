package owo.pigeon.modules.impl.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.render.RenderUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class RenderTest extends Module {
    public RenderTest() {
        super("RenderTest", Category.DEBUG);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        GuiGraphics context = event.getContext();
        TextRendererUtil.drawString(context, "line 1: with color input &a[&&a]", 100, 100, 0xFF000000);
        TextRendererUtil.drawString(context, "line 2: without color input &a[&&a]", 100, 100 + TextRendererUtil.getLineHeight());

        List<String> mutiText = new ArrayList<>();
        mutiText.add("line 3: input &a[&&a]");
        mutiText.add("line 4: muti lines &a[&&a]");

        TextRendererUtil.drawStringList(context, mutiText, 100, 100 + TextRendererUtil.getLineHeight() * 2);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        PoseStack stack = event.getMatrix();
        for (Entity entity : mc.level.entitiesForRendering()) {
            RenderUtil.drawBox(stack, entity, Color.CYAN, 2.0);
            RenderUtil.drawTracer(stack, entity, Color.GREEN, 2.0);
        }
    }
}
