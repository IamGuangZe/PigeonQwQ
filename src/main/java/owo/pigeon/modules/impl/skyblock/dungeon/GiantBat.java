package owo.pigeon.modules.impl.skyblock.dungeon;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BatEntity;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class GiantBat extends Module {
    public GiantBat() {
        super("GiantBat", Category.DUNGEON);
    }

    public FloatSetting scale = setting("scale", 2.0f, 1.0f, 5.0f, "x", v -> true);
    public EnableSetting showHitbox = setting("show-hitbox", false, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.OUTLINE, v -> showHitbox.getValue());
    public ColorSetting color = setting("color", new Color(0xAA00FF00, true), v -> showHitbox.getValue());

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (!showHitbox.getValue()) return;
        if (!DungeonUtil.isInDungeon()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof BatEntity bat)) continue;
            if (bat.getMaxHealth() != 100f && bat.getMaxHealth() != 200f) continue;
            RenderUtil.drawESP(event.getMatrix(), entity, color.getValue(), mode.getValue(), false);
        }
    }
}
