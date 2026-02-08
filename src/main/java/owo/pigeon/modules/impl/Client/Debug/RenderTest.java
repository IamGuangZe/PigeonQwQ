package owo.pigeon.modules.impl.Client.Debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.Render.RenderUtil;
import owo.pigeon.utils.Render.TextRendererUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class RenderTest extends Module {
    public RenderTest() {
        super("RenderTest", Category.CLIENT);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        DrawContext context = event.getContext();
        TextRendererUtil.drawString(context, "line 1: with color input &a[&&a]", 100, 100, 0xFF000000);
        TextRendererUtil.drawString(context, "line 2: without color input &a[&&a]", 100, 100 + TextRendererUtil.getLineHeight());

        List<String> mutiText = new ArrayList<>();
        mutiText.add("line 3: input &a[&&a]");
        mutiText.add("line 4: muti lines &a[&&a]");

        TextRendererUtil.drawStringList(context, mutiText, 100, 100 + TextRendererUtil.getLineHeight() * 2);

        ChatUtil.sendDebugMessage(this.name, "drawString");
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();
        for (Entity entity : mc.world.getEntities()) {
            RenderUtil.drawBox(stack, entity, Color.CYAN, 2.0);
        }

        ChatUtil.sendDebugMessage(this.name, "drawBox");
    }
}
