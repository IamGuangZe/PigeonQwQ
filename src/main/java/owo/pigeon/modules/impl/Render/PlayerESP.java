package owo.pigeon.modules.impl.Render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeonqwq.mc;

public class PlayerESP extends Module {
    public PlayerESP() {
        super("PlayerESP", Category.RENDER);
    }

    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.FILL, v -> true);
    public ColorSetting color = setting("color", new Color(0x5FFFFFFF, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && PlayerUtil.hasUUID(player))
                RenderUtil.drawESP(event.getMatrix(),player,color.getValue(),mode.getValue());
        }
    }
}
