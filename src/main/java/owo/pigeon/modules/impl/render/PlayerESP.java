package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.player.AbstractClientPlayer;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class PlayerESP extends Module {
    public PlayerESP() {
        super("PlayerESP", Category.RENDER);
    }

    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public EnableSetting tracer = setting("tracer", false, v -> true);
    public ColorSetting color = setting("color", new Color(0x5FFFFFFF, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        for (AbstractClientPlayer player : mc.level.players()) {
            if (player != mc.player && !PlayerUtil.isBot(player))
                RenderUtil.drawESP(event.getMatrix(), player, color.getValue(), mode.getValue(), tracer.getValue());
        }
    }
}
