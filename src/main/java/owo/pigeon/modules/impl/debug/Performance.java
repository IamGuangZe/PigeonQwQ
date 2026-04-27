package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.DrawContext;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.render.TextRendererUtil;

import static owo.pigeon.Pigeon.mc;

public class Performance extends Module {

    public Performance() {
        super("Performance", Category.DEBUG);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        if (mc.options.hudHidden) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        DrawContext context = event.getContext();
        int lineHeight = TextRendererUtil.getLineHeight();
        int x = 2;
        int y = mc.getWindow().getScaledHeight() - lineHeight - 2;

        String tpsText = String.format("%.1f", owo.pigeon.utils.world.ServerUtil.getTps());
        String pingText = owo.pigeon.utils.world.ServerUtil.getAveragePing() + "ms";
        String display = "TPS: " + tpsText + "  Ping: " + pingText;

        TextRendererUtil.drawString(context, display, x, y, 0xFFFFFFFF);
    }
}
