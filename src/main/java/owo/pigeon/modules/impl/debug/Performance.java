package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.render.FontUtil;

import static owo.pigeon.Pigeon.mc;

public class Performance extends Module {

    public Performance() {
        super("Performance", Category.DEBUG);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        if (mc.options.hideGui) return;
        if (mc.player == null || mc.getConnection() == null) return;

        GuiGraphicsExtractor context = event.getContext();
        int lineHeight = FontUtil.getLineHeight();
        int x = 2;
        int y = mc.getWindow().getGuiScaledHeight() - lineHeight - 2;

        String tpsText = String.format("%.1f", owo.pigeon.utils.world.ServerUtil.getTps());
        String pingText = owo.pigeon.utils.world.ServerUtil.getAveragePing() + "ms";
        String display = "TPS: " + tpsText + "  Ping: " + pingText;

        FontUtil.drawString(context, display, x, y, 0xFFFFFFFF);
    }
}
